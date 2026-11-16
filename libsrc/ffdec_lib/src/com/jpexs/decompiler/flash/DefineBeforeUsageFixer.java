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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterModifier;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class fixes SWF that all CharacterTags (and attached CharacterIdTags) 
 * are placed before their usage.
 * @author JPEXS
 */
public class DefineBeforeUsageFixer {
    
    public boolean fixDefineBeforeUsage(SWF swf) {
        ReadOnlyTagList tags = swf.getTags();
        Set<Integer> walkedCharacters = new HashSet<>();
        boolean changed = false;
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);            
            Set<Integer> needed = new LinkedHashSet<>();
            Set<String> neededClasses = new LinkedHashSet<>();
            t.getNeededCharactersDeep(needed, neededClasses);
            for (int chId : needed) {
                if (walkedCharacters.contains(chId)) {
                    continue;
                }
                walkedCharacters.add(chId);
                CharacterTag ch = swf.getCharacter(chId);
                if (ch != null) {
                    int defineIndex = tags.indexOf(ch);
                    if (i < defineIndex) {
                        moveCharacter(swf, t, ch);
                        i = tags.indexOf(t);
                        changed = true;
                    }
                }                
            }            
        }
        return changed;
    }
    
    private void moveCharacter(SWF swf, Tag usage, CharacterTag character) {
        List<Tag> definitions = new ArrayList<>();
        definitions.add(character);
        for (Tag tag : swf.getTags()) {
            if (tag instanceof CharacterModifier
                    && ((CharacterModifier) tag).getCharacterId() == character.getCharacterId()) {
                definitions.add(tag);
            }
        }
        for (Tag tag : definitions) {
            swf.removeTag(tag);
        }
        int usageIndex = swf.indexOfTag(usage);
        for (Tag tag : definitions) {
            swf.addTag(usageIndex++, tag);
        }
    }        
}
