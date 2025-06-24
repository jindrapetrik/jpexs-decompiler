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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import java.io.Serializable;
import java.util.Set;

/**
 * Fill style array.
 *
 * @author JPEXS
 */
public class FILLSTYLEARRAY implements NeedsCharacters, Serializable {

    /**
     * Fill styles
     */
    @SWFArray(value = "fillStyle")
    public FILLSTYLE[] fillStyles;

    public int getMinShapeNum() {
        int result = 1;
        for (FILLSTYLE fs : fillStyles) {
            int sn = fs.getMinShapeNum();
            if (sn > result) {
                result = sn;
            }
        }
        return result;
    }
    
    public FILLSTYLEARRAY toShapeNum(int targetShapeNum) {
        FILLSTYLEARRAY result = new FILLSTYLEARRAY();
        result.fillStyles = new FILLSTYLE[fillStyles.length];
        for (int i = 0; i < fillStyles.length; i++) {
            result.fillStyles[i] = fillStyles[i].toShapeNum(targetShapeNum);
        }
        return result;
    }
    
    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        for (FILLSTYLE fs : fillStyles) {
            fs.getNeededCharacters(needed, swf);
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

    /**
     * Converts to MORPHFILLSTYLEARRAY.
     * @return MORPHFILLSTYLEARRAY
     */
    public MORPHFILLSTYLEARRAY toMorphFillStyleArray() {
        MORPHFILLSTYLEARRAY morphFillStyleArray = new MORPHFILLSTYLEARRAY();
        morphFillStyleArray.fillStyles = new MORPHFILLSTYLE[fillStyles.length];
        for (int i = 0; i < fillStyles.length; i++) {
            morphFillStyleArray.fillStyles[i] = fillStyles[i].toMorphStyle();
        }
        return morphFillStyleArray;
    }
}
