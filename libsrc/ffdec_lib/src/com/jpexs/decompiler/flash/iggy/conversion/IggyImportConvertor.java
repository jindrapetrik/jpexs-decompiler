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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.IggyImport;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggySymbolClass;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ImportAssets2Tag;
import com.jpexs.decompiler.flash.tags.ImportAssetsTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Library dependencies and their local character bindings.
 */
public final class IggyImportConvertor {

    private IggyImportConvertor() {
    }

    public static void exportImports(SWF swf, IggySwf movie, Set<Integer> exportedIds) {
        for (IggyImport library : movie.getImports()) {
            ImportAssets2Tag tag = new ImportAssets2Tag(swf);
            tag.url = library.getLibrary();
            for (IggySymbolClass symbol : library.getSymbols()) {
                int id = (int) symbol.getCharacterId();
                tag.tags.add(id);
                tag.names.add(symbol.getName());
                exportedIds.add(id);
            }
            // Empty imports still declare library / AS3 dependencies.
            tag.setModified(true);
            swf.addTag(tag);
        }
    }

    public static void updateImports(IggySwf movie, SWF swf) throws IOException {
        List<IggyImport> libraries = new ArrayList<>();
        Set<Integer> imported = new HashSet<>();
        for (Tag tag : swf.getTags()) {
            String url;
            List<Integer> ids;
            List<String> names;
            if (tag instanceof ImportAssets2Tag) {
                ImportAssets2Tag source = (ImportAssets2Tag) tag;
                // Neither download policy nor a digest is stored in Iggy's table.
                if (source.downloadNow != 1 || source.hasDigest != 0) {
                    throw new IggyUnsupportedTagException(tag, "Iggy imports require downloadNow=1 and hasDigest=0");
                }
                url = source.url;
                ids = source.tags;
                names = source.names;
            } else if (tag instanceof ImportAssetsTag) {
                ImportAssetsTag source = (ImportAssetsTag) tag;
                url = source.url;
                ids = source.tags;
                names = source.names;
            } else {
                continue;
            }
            if (ids == null || names == null || ids.size() != names.size() || ids.size() > 65535) {
                throw new IOException("Invalid Iggy import symbol list");
            }
            List<IggySymbolClass> symbols = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                Integer id = ids.get(i);
                if (id == null || id <= 0 || id > 65535 || !imported.add(id)) {
                    throw new IOException("Invalid or duplicate Iggy import character ID " + id);
                }
                symbols.add(new IggySymbolClass(0, names.get(i), id, 0));
            }
            libraries.add(new IggyImport(url, symbols));
        }
        Set<Integer> removed = new HashSet<>();
        for (IggyImport library : movie.getOriginalImports()) {
            for (IggySymbolClass symbol : library.getSymbols()) {
                int id = (int) symbol.getCharacterId();
                if (!imported.contains(id)) {
                    removed.add(id);
                }
            }
        }
        validateTags(swf, swf.getTags(), imported, removed, false);
        movie.setImports(libraries);
    }

    private static void validateTags(SWF swf, Iterable<Tag> tags, Set<Integer> imported,
            Set<Integer> removed, boolean nested) throws IOException {
        for (Tag tag : tags) {
            if (nested && tag instanceof ImportTag) {
                throw new IOException("Iggy library imports must be on the root timeline");
            }
            if (tag instanceof CharacterTag && imported.contains(((CharacterTag) tag).getCharacterId())) {
                throw new IOException("Iggy import character ID collides with a local definition: "
                        + ((CharacterTag) tag).getCharacterId());
            }
            if (!removed.isEmpty()) {
                Set<Integer> needed = new HashSet<>();
                tag.getNeededCharacters(needed, new HashSet<String>(), swf);
                needed.retainAll(removed);
                if (!needed.isEmpty()) {
                    throw new IOException("Removed Iggy import is still referenced: " + needed.iterator().next());
                }
            }
            if (tag instanceof DefineSpriteTag) {
                validateTags(swf, ((DefineSpriteTag) tag).getTags(), imported, removed, true);
            }
        }
    }
}
