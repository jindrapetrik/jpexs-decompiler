/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.tags.FreeCharacterTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterModifier;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
            t.getNeededCharactersDeep(needed);            
            for (int chId : needed) {
                if (walkedCharacters.contains(chId)) {
                    continue;
                }
                walkedCharacters.add(chId);
                CharacterTag ch = swf.getCharacter(chId);
                if (ch != null) {
                    int defineIndex = tags.indexOf(ch);
                    int usageIndex = i;
                    if (usageIndex < defineIndex) {
                        i += moveCharacter(swf, i, chId);
                        changed = true;
                    }
                }                
            }            
        }
        return changed;
    }
    
    private int moveCharacter(SWF swf, int usageIndex, int characterId) {
        int i = 0;
        for (int j = 0; j < swf.getTags().size(); j++) {
            Tag t2 = swf.getTags().get(j);
            if (t2 instanceof CharacterModifier) {
                CharacterModifier chit = (CharacterModifier) t2;                
                if (chit.getCharacterId() == characterId) {                    
                    swf.removeTag(j);
                    swf.addTag(usageIndex + i, (Tag) chit);
                    i++;
                }
            }
        }
        return i;
    }
}
