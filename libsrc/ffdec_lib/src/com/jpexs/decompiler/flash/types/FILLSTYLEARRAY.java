/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class FILLSTYLEARRAY implements NeedsCharacters, Serializable {

    @SWFArray(value = "fillStyle")
    public FILLSTYLE[] fillStyles;

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        for (FILLSTYLE fs : fillStyles) {
            fs.getNeededCharacters(needed);
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        for (FILLSTYLE fs : fillStyles) {
            modified |= fs.replaceCharacter(oldCharacterId, newCharacterId);
        }
        return modified;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        for (FILLSTYLE fs : fillStyles) {
            modified |= fs.removeCharacter(characterId);
        }
        return modified;
    }
}
