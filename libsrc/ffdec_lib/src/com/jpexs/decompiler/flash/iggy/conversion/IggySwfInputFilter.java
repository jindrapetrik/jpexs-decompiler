/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterModifier;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Isolated SWF projection for the CLI's warning-and-skip conversion policy.
 */
final class IggySwfInputFilter {

    private final SWF swf;
    private final Consumer<String> warnings;
    private final Map<Tag, Timelined> owners = new IdentityHashMap<>();
    private final Map<Tag, String> locations = new IdentityHashMap<>();
    private final Map<Tag, Set<Integer>> characters = new IdentityHashMap<>();
    private final Map<Tag, Set<String>> classes = new IdentityHashMap<>();
    private final Map<Integer, Set<String>> classNames = new HashMap<>();
    private final Set<Integer> omittedIds = new HashSet<>();
    private final Set<String> omittedClasses = new HashSet<>();

    IggySwfInputFilter(SWF source, IggyFile.Variant variant, Consumer<String> warnings) throws IOException {
        if (source == null) {
            throw new IOException("Missing source SWF");
        }
        this.warnings = warnings;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        source.saveTo(bytes);
        try {
            swf = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false, false, source.getCharset(), source.getSwfProfile());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while preparing Iggy conversion", ex);
        }
        collect(swf, "main");
        for (Tag tag : IggyDefinitionConvertor.tags(swf.getTags())) {
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag symbols = (SymbolClassTag) tag;
                for (int i = 0; i < Math.min(symbols.tags.size(), symbols.names.size()); i++) {
                    classNames.computeIfAbsent(symbols.tags.get(i), key -> new HashSet<>()).add(symbols.names.get(i));
                }
            }
            if (tag instanceof ImportTag) {
                for (Map.Entry<Integer, String> entry : ((ImportTag) tag).getAssets().entrySet()) {
                    classNames.computeIfAbsent(entry.getKey(), key -> new HashSet<>()).add(entry.getValue());
                }
            }
        }
        for (Tag tag : new ArrayList<>(IggyDefinitionConvertor.tags(swf.getTags()))) {
            if (tag instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
                if (place.getVisible() != null) {
                    omitAttribute(tag, "visible");
                    place.setPlaceFlagHasVisible(false);
                }
                if (place.getBackgroundColor() != null) {
                    omitAttribute(tag, "opaqueBackground");
                    place.setPlaceFlagOpaqueBackground(false);
                }
            }
            boolean unsupported = !SwfToIggyConvertor.supportsNewTag(tag);
            boolean legacy = variant == IggyFile.Variant.X86_LEGACY && (tag instanceof ABCContainerTag
                    || tag instanceof ImportTag || tag instanceof SymbolClassTag
                    || tag instanceof FileAttributesTag && ((FileAttributesTag) tag).actionScript3);
            if (unsupported || legacy) {
                discard(tag, legacy ? "not supported by 32 legacy" : "no supported native Iggy representation");
            }
        }
        pruneReferences();
    }

    SWF getSwf() {
        return swf;
    }

    private void collect(Timelined owner, String path) {
        int index = 0;
        for (Tag tag : owner.getTags()) {
            owners.put(tag, owner);
            locations.put(tag, path + "/tag" + index++);
            Set<Integer> ids = new HashSet<>();
            Set<String> names = new HashSet<>();
            // Keep containers; unsupported children are removed independently.
            if (!(tag instanceof DefineSpriteTag)) {
                tag.getNeededCharacters(ids, names, swf);
                if (tag instanceof CharacterModifier && !(tag instanceof CharacterTag)) {
                    ids.add(((CharacterModifier) tag).getCharacterId());
                }
            }
            characters.put(tag, ids);
            classes.put(tag, names);
            if (tag instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) tag;
                collect(sprite, path + "/sprite" + sprite.spriteId);
            }
        }
    }

    void skip(Tag tag, String reason) throws IOException {
        if (!owners.containsKey(tag)) {
            throw new IOException("Cannot locate unsupported SWF tag", new IggyUnsupportedTagException(tag, reason));
        }
        discard(tag, reason);
        pruneReferences();
    }

    private void warn(Tag tag, String reason) {
        warnings.accept("Skipping " + tag.getName() + " at " + locations.get(tag) + ": " + reason);
    }

    private void omitAttribute(Tag tag, String attribute) {
        warnings.accept("Ignoring unsupported " + attribute + " attribute of " + tag.getName() + " at " + locations.get(tag));
        tag.setModified(true);
    }

    private void omitId(int id) {
        omittedIds.add(id);
        Set<String> names = classNames.get(id);
        if (names != null) {
            omittedClasses.addAll(names);
        }
    }

    private void discard(Tag tag, String reason) {
        warn(tag, reason);
        if (tag instanceof CharacterTag) {
            omitId(((CharacterTag) tag).getCharacterId());
        }
        if (tag instanceof ImportTag) {
            for (int id : ((ImportTag) tag).getAssets().keySet()) {
                omitId(id);
            }
        }
        if (tag instanceof SymbolClassTag) {
            omittedClasses.addAll(((SymbolClassTag) tag).names);
        }
        owners.remove(tag).removeTag(tag);
    }

    private void pruneReferences() {
        boolean changed;
        do {
            changed = false;
            for (Tag tag : new ArrayList<>(IggyDefinitionConvertor.tags(swf.getTags()))) {
                if (tag instanceof SymbolClassTag) {
                    SymbolClassTag symbols = (SymbolClassTag) tag;
                    for (int i = Math.min(symbols.tags.size(), symbols.names.size()) - 1; i >= 0; i--) {
                        if (omittedIds.contains(symbols.tags.get(i))) {
                            warn(tag, "omitted SymbolClass binding for character " + symbols.tags.get(i));
                            symbols.tags.remove(i);
                            symbols.names.remove(i);
                            symbols.setModified(true);
                        }
                    }
                    continue;
                }
                Set<Integer> ids = new HashSet<>(characters.get(tag));
                ids.retainAll(omittedIds);
                Set<String> names = new HashSet<>(classes.get(tag));
                names.retainAll(omittedClasses);
                if (!ids.isEmpty() || !names.isEmpty()) {
                    discard(tag, "references an omitted definition " + (!ids.isEmpty() ? ids : names));
                    changed = true;
                }
            }
        } while (changed);
        swf.updateCharacters();
    }
}
