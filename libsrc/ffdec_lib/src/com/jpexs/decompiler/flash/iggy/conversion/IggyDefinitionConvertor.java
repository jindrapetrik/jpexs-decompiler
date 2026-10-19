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
import com.jpexs.decompiler.flash.iggy.IggyObjectData;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggySymbolClass;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterModifier;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reconcile SWF definitions with the native dictionary, including additions and
 * removals.
 */
public final class IggyDefinitionConvertor {

    private IggyDefinitionConvertor() {
    }

    /**
     * Each native object has one class-name and runtime-class slot.
     */
    static SWF expandSymbolAliases(IggySwf movie, SWF source) throws IOException {
        if (source == null) {
            throw new IOException("Missing source SWF");
        }
        Set<Integer> eligible = new HashSet<>();
        for (Tag tag : tags(source.getTags())) {
            if (tag instanceof CharacterTag && SwfToIggyConvertor.supportsNewTag(tag)) {
                eligible.add(((CharacterTag) tag).getCharacterId());
            }
        }
        Set<Integer> bound = new HashSet<>();
        boolean aliases = false;
        for (Tag tag : source.getTags()) {
            if (tag instanceof SymbolClassTag) {
                for (int id : ((SymbolClassTag) tag).tags) {
                    if (id != 0 && !bound.add(id) && eligible.contains(id)) {
                        aliases = true;
                    }
                }
            }
        }
        if (!aliases) {
            return source;
        }
        // Warning retries and strict updates both operate on an isolated SWF.
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        source.saveTo(bytes);
        SWF copy;
        try {
            copy = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false, false,
                    source.getCharset(), source.getSwfProfile());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while preparing Iggy SymbolClass aliases", ex);
        }
        List<Tag> all = tags(copy.getTags());
        Map<Integer, CharacterTag> definitions = new HashMap<>();
        Set<Integer> used = new HashSet<>();
        used.add(0);
        for (Tag tag : all) {
            if (tag instanceof CharacterTag) {
                CharacterTag definition = (CharacterTag) tag;
                used.add(definition.getCharacterId());
                definitions.put(definition.getCharacterId(), definition);
            }
            if (tag instanceof ImportTag) {
                used.addAll(((ImportTag) tag).getAssets().keySet());
            }
            if (tag instanceof SymbolClassTag) {
                used.addAll(((SymbolClassTag) tag).tags);
            }
        }
        Map<String, Integer> previous = new HashMap<>();
        Set<Integer> occupied = new HashSet<>(used);
        if (movie != null) {
            for (IggySwf.DictionaryEntry entry : movie.getDictionary()) {
                if (!entry.isNull()) {
                    occupied.add(entry.getId());
                }
            }
            for (IggySymbolClass symbol : movie.getSymbolClasses()) {
                previous.put(symbol.getName(), (int) symbol.getCharacterId());
            }
        }
        bound.clear();
        int nextId = 1;
        for (Tag tag : all) {
            if (!(tag instanceof SymbolClassTag) || tag.getTimelined() != copy) {
                continue;
            }
            SymbolClassTag symbols = (SymbolClassTag) tag;
            for (int i = 0; i < symbols.tags.size(); i++) {
                int id = symbols.tags.get(i);
                if (bound.add(id) || !eligible.contains(id)) {
                    continue;
                }
                CharacterTag original = definitions.get(id);
                Integer reuse = i < symbols.names.size() ? previous.get(symbols.names.get(i)) : null;
                int aliasId;
                if (reuse != null && !used.contains(reuse) && reuse > 0 && reuse < movie.getDictionary().size()
                        && movie.getDictionary().get(reuse).getType() == type(original)) {
                    aliasId = reuse;
                } else {
                    while (nextId <= 65535 && occupied.contains(nextId)) {
                        nextId++;
                    }
                    if (nextId > 65535) {
                        throw new IOException("No free Iggy character ID for SymbolClass alias " + id);
                    }
                    aliasId = nextId++;
                }
                CharacterTag alias = cloneDefinition(copy, original);
                alias.setCharacterId(aliasId);
                alias.setModified(true);
                copy.addTag(copy.indexOfTag(symbols), alias);
                // Character modifiers (for example font info and scaling grids)
                // describe the aliased definition as well as the original.
                for (Tag modifier : all) {
                    if (modifier instanceof CharacterModifier && !(modifier instanceof CharacterTag)
                            && ((CharacterModifier) modifier).getCharacterId() == id) {
                        Tag duplicate = cloneTag(modifier);
                        ((CharacterModifier) duplicate).setCharacterId(aliasId);
                        duplicate.setModified(true);
                        copy.addTag(copy.indexOfTag(symbols), duplicate);
                    }
                }
                symbols.tags.set(i, aliasId);
                symbols.setModified(true);
                used.add(aliasId);
                occupied.add(aliasId);
            }
        }
        copy.updateCharacters();
        copy.assignClassesToSymbols();
        return copy;
    }

    private static Tag cloneTag(Tag tag) throws IOException {
        try {
            return tag.cloneTag();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while copying an Iggy SymbolClass definition", ex);
        }
    }

    private static CharacterTag cloneDefinition(SWF swf, CharacterTag definition) throws IOException {
        if (!(definition instanceof DefineSpriteTag)) {
            return (CharacterTag) cloneTag(definition);
        }
        DefineSpriteTag original = (DefineSpriteTag) definition;
        DefineSpriteTag alias = new DefineSpriteTag(swf);
        alias.frameCount = original.frameCount;
        alias.hasEndTag = original.hasEndTag;
        // Nested declarations are global dictionary entries, not per-instance
        // state. Keep them once in the original sprite and copy only commands.
        for (Tag command : original.getTags()) {
            if (IggyTimelineConvertor.command(command)) {
                alias.addTag(cloneTag(command));
            }
        }
        return alias;
    }

    public static List<Tag> tags(Iterable<Tag> input) {
        List<Tag> result = new ArrayList<>();
        for (Tag tag : input) {
            result.add(tag);
            if (tag instanceof DefineSpriteTag) {
                result.addAll(tags(((DefineSpriteTag) tag).getTags()));
            }
        }
        return result;
    }

    static int type(CharacterTag tag) throws IggyUnsupportedTagException {
        if (tag instanceof DefineSpriteTag) {
            return 3;
        }
        if (tag instanceof FontTag) {
            return 22;
        }
        if (tag instanceof DefineEditTextTag) {
            return 6;
        }
        if (tag instanceof ShapeTag) {
            return 1;
        }
        if (tag instanceof MorphShapeTag) {
            return 2;
        }
        if (tag instanceof ImageTag) {
            return 4;
        }
        if (tag instanceof StaticTextTag) {
            return 5;
        }
        if (tag instanceof ButtonTag) {
            return 7;
        }
        if (tag instanceof DefineBinaryDataTag) {
            return 20;
        }
        if (tag instanceof DefineSoundTag) {
            return 21;
        }
        throw new IggyUnsupportedTagException(tag, "Unsupported Iggy character definition: " + tag.getName());
    }

    private static Map<Integer, CharacterTag> validateDefinitions(IggySwf movie, SWF swf) throws IOException {
        List<Tag> all = tags(swf.getTags());
        Map<Integer, CharacterTag> definitions = new LinkedHashMap<>();
        Set<Integer> available = new HashSet<>();
        Map<String, Integer> classes = new HashMap<>();
        for (Tag tag : all) {
            if (tag instanceof CharacterTag) {
                CharacterTag character = (CharacterTag) tag;
                int id = character.getCharacterId();
                if (id <= 0 || id > 65535 || definitions.put(id, character) != null) {
                    throw new IOException("Invalid or duplicate Iggy character ID " + id);
                }
                int kind = type(character);
                IggySwf.DictionaryEntry before = id < movie.getDictionary().size() ? movie.getDictionary().get(id) : null;
                if (before != null && !before.isNull() && kind != before.getType()
                        && (kind == 3 || kind == 6 || kind == 22 || before.getType() == 3 || before.getType() == 6 || before.getType() == 22)) {
                    throw new IOException("Iggy character ID is occupied by another definition type: " + id);
                }
                available.add(id);
            }
            if (tag instanceof ImportTag) {
                available.addAll(((ImportTag) tag).getAssets().keySet());
                for (Map.Entry<Integer, String> asset : ((ImportTag) tag).getAssets().entrySet()) {
                    classes.put(asset.getValue(), asset.getKey());
                }
            }
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag symbols = (SymbolClassTag) tag;
                for (int i = 0; i < symbols.tags.size(); i++) {
                    if (i < symbols.names.size()) {
                        classes.put(symbols.names.get(i), symbols.tags.get(i));
                    }
                }
            }
        }
        Set<Integer> removed = new HashSet<>();
        for (IggySwf.DictionaryEntry entry : movie.getDictionary()) {
            int kind = entry.getType();
            if (entry.getId() > 0 && !entry.isNull() && !available.contains(entry.getId())) {
                if (kind == 1 || kind == 2 || kind == 3 || kind == 4 || kind == 5 || kind == 6
                        || kind == 7 || kind == 20 || kind == 21 || kind == 22) {
                    removed.add(entry.getId());
                } else {
                    // Unknown definitions stay in the native graph even though SWF cannot expose them.
                    available.add(entry.getId());
                }
            }
        }
        Map<String, Integer> removedClasses = new HashMap<>();
        for (IggySymbolClass symbol : movie.getOriginalSymbolClasses()) {
            int id = (int) symbol.getCharacterId();
            if (id != 0 && !available.contains(id) && !classes.containsKey(symbol.getName())) {
                removedClasses.put(symbol.getName(), id);
            }
        }
        for (int id : removed) {
            long field = movie.getDictionary().get(id).getAddress() + (movie.is64() ? 16 : 8);
            long value = movie.is64() ? movie.getTimeline().bytes(field, 8).getLong()
                    : movie.getTimeline().bytes(field, 4).getInt();
            if (value != 0 && value != 1) {
                String className = movie.getTimeline().string(movie.getTimeline().reference(field));
                if (className != null && !className.isEmpty() && !classes.containsKey(className)) {
                    removedClasses.put(className, id);
                }
            }
        }
        for (Tag tag : all) {
            Set<Integer> needed = new HashSet<>();
            Set<String> names = new HashSet<>();
            tag.getNeededCharacters(needed, names, swf);
            if (tag instanceof CharacterModifier && !(tag instanceof CharacterTag)) {
                needed.add(((CharacterModifier) tag).getCharacterId());
            }
            needed.remove(0);
            needed.retainAll(removed);
            if (!needed.isEmpty()) {
                throw new IOException("Missing or removed Iggy character is still referenced: " + needed.iterator().next());
            }
            names.retainAll(removedClasses.keySet());
            if (!names.isEmpty()) {
                throw new IOException("Removed Iggy class is still referenced: " + names.iterator().next());
            }
        }
        return definitions;
    }

    public static void validate(IggySwf movie, SWF swf) throws IOException {
        validateDefinitions(movie, swf);
    }

    public static void update(IggySwf movie, SWF swf) throws IOException {
        Map<Integer, CharacterTag> definitions = validateDefinitions(movie, swf);
        movie.setActiveDefinitions(definitions.keySet());
        for (Map.Entry<Integer, CharacterTag> entry : definitions.entrySet()) {
            int id = entry.getKey();
            if (!(entry.getValue() instanceof DefineSpriteTag) || movie.getSpriteTimelines().containsKey(id)) {
                continue;
            }
            IggyTimelineBuffer b = new IggyTimelineBuffer(movie.is64());
            long at = IggyAssetConvertor.header(b, id, 3, movie.is64() ? 72 : 36);
            for (int offset : movie.is64() ? new int[]{32, 48, 56, 64} : new int[]{16, 24, 28, 32}) {
                b.pointer(at + offset, -1);
            }
            movie.replaceObject(id, new IggyObjectData(b.data.getAllBytes(), new ArrayList<>(b.fields.values()),
                    b.characters, at, movie.is64() ? 72 : 36, 3));
        }
    }
}
