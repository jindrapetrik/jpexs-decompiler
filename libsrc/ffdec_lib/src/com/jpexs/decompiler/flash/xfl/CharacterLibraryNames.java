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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.NameCharacterTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Restores the Library names stored in Generator templates. */
final class CharacterLibraryNames {

    private CharacterLibraryNames() {
    }

    static Set<String> initialize(ReadOnlyTagList tags, Set<CharacterTag> characters,
            Map<CharacterTag, String> names, List<CharacterTag> nonLibraryShapes) {
        Map<CharacterTag, String> requested = new LinkedHashMap<>();
        collect(tags, requested);
        requested.replaceAll((character, name) -> safePath(name));
        requested.values().removeIf(String::isEmpty);
        Set<String> used = new HashSet<>();
        for (CharacterTag character : characters) {
            if (!requested.containsKey(character)) {
                String kind = character instanceof ImageTag ? "Bitmap" : character instanceof SoundTag ? "Sound"
                        : character instanceof FontTag ? "Font" : "Symbol";
                used.add(names.getOrDefault(character, kind + " " + character.getCharacterId()).toLowerCase(Locale.ROOT));
            }
        }
        for (Map.Entry<CharacterTag, String> entry : requested.entrySet()) {
            CharacterTag character = entry.getKey();
            if (!characters.contains(character)) {
                continue;
            }
            String base = entry.getValue();
            String name = base;
            for (int suffix = 2; !used.add(name.toLowerCase(Locale.ROOT)); suffix++) {
                name = base + " " + suffix;
            }
            names.put(character, name);
            // A named shape belongs in the Library even if it is used only once.
            nonLibraryShapes.remove(character);
        }
        Set<String> folders = new LinkedHashSet<>();
        for (String name : names.values()) {
            for (int slash = name.indexOf('/'); slash >= 0; slash = name.indexOf('/', slash + 1)) {
                folders.add(name.substring(0, slash));
            }
        }
        return folders;
    }

    private static void collect(ReadOnlyTagList tags, Map<CharacterTag, String> requested) {
        for (Tag tag : tags) {
            if (tag instanceof NameCharacterTag) {
                NameCharacterTag name = (NameCharacterTag) tag;
                CharacterTag character = tag.getSwf().getCharacter(name.characterId);
                if (character != null && !name.name.isEmpty()) {
                    requested.put(character, name.name);
                }
            } else if (tag instanceof DefineSpriteTag) {
                collect(((DefineSpriteTag) tag).getTags(), requested);
            }
        }
    }

    private static String safePath(String name) {
        List<String> parts = new ArrayList<>();
        for (String part : name.replace('\\', '/').split("/")) {
            part = part.replaceAll("[\\x00-\\x1f<>:\"|?*]", "_").replaceAll("[. ]+$", "_");
            if (part.isEmpty()) {
                continue;
            }
            if (part.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?")) {
                part = "_" + part;
            }
            parts.add(part);
        }
        return String.join("/", parts);
    }
}
