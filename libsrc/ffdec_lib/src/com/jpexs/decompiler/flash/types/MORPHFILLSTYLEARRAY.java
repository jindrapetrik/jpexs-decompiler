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
import java.io.Serializable;
import java.util.Set;

/**
 * Morph fill style array.
 *
 * @author JPEXS
 */
public class MORPHFILLSTYLEARRAY implements NeedsCharacters, Serializable {

    /**
     * Fill styles
     */
    public MORPHFILLSTYLE[] fillStyles;

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        for (MORPHFILLSTYLE fs : fillStyles) {
            fs.getNeededCharacters(needed, swf);
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        for (MORPHFILLSTYLE fs : fillStyles) {
            modified |= fs.replaceCharacter(oldCharacterId, newCharacterId);
        }
        return modified;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        for (MORPHFILLSTYLE fs : fillStyles) {
            modified |= fs.removeCharacter(characterId);
        }
        return modified;
    }

    /**
     * Gets fill styles at given ratio.
     * @param ratio Ratio
     * @return Fill styles at given ratio
     */
    public FILLSTYLEARRAY getFillStylesAt(int ratio) {
        FILLSTYLEARRAY ret = new FILLSTYLEARRAY();
        ret.fillStyles = new FILLSTYLE[fillStyles.length];
        for (int m = 0; m < fillStyles.length; m++) {
            ret.fillStyles[m] = fillStyles[m].getFillStyleAt(ratio);
        }
        return ret;
    }

    /**
     * Gets start fill styles.
     * @return Start fill styles
     */
    public FILLSTYLEARRAY getStartFillStyles() {
        FILLSTYLEARRAY ret = new FILLSTYLEARRAY();
        ret.fillStyles = new FILLSTYLE[fillStyles.length];
        for (int m = 0; m < fillStyles.length; m++) {
            ret.fillStyles[m] = fillStyles[m].getStartFillStyle();
        }
        return ret;
    }

    /**
     * Gets end fill styles.
     * @return End fill styles
     */
    public FILLSTYLEARRAY getEndFillStyles() {
        FILLSTYLEARRAY ret = new FILLSTYLEARRAY();
        ret.fillStyles = new FILLSTYLE[fillStyles.length];
        for (int m = 0; m < fillStyles.length; m++) {
            ret.fillStyles[m] = fillStyles[m].getEndFillStyle();
        }
        return ret;
    }
}
