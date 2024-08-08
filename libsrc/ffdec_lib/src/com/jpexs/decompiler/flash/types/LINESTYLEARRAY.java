/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import java.io.Serializable;
import java.util.Set;

/**
 * Line style array.
 *
 * @author JPEXS
 */
public class LINESTYLEARRAY implements NeedsCharacters, Serializable {

    /**
     * Line styles
     */
    @SWFArray(value = "lineStyle")
    @Conditional(tags = {DefineShapeTag.ID, DefineShape2Tag.ID, DefineShape3Tag.ID})
    public LINESTYLE[] lineStyles = new LINESTYLE[0];

    /**
     * Line styles 2
     */
    @SWFArray(value = "lineStyle")
    @Conditional(tags = {DefineShape4Tag.ID})
    public LINESTYLE2[] lineStyles2 = new LINESTYLE2[0];

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        if (lineStyles != null) {
            for (ILINESTYLE ls : lineStyles) {
                ls.getNeededCharacters(needed, swf);
            }
        }
        if (lineStyles != null) {
            for (ILINESTYLE ls : lineStyles2) {
                ls.getNeededCharacters(needed, swf);
            }
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        if (lineStyles != null) {
            for (ILINESTYLE ls : lineStyles) {
                modified |= ls.replaceCharacter(oldCharacterId, newCharacterId);
            }
        }
        if (lineStyles2 != null) {
            for (ILINESTYLE ls : lineStyles2) {
                modified |= ls.replaceCharacter(oldCharacterId, newCharacterId);
            }
        }
        return modified;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        if (lineStyles != null) {
            for (ILINESTYLE ls : lineStyles) {
                modified |= ls.removeCharacter(characterId);
            }
        }

        if (lineStyles2 != null) {
            for (ILINESTYLE ls : lineStyles2) {
                modified |= ls.removeCharacter(characterId);
            }
        }
        return modified;
    }

    /**
     * Converts to MORPHLINESTYLEARRAY
     * @return MORPHLINESTYLEARRAY
     */
    public MORPHLINESTYLEARRAY toMorphLineStyleArray() {
        MORPHLINESTYLEARRAY morphLineStyleArray = new MORPHLINESTYLEARRAY();
        if (lineStyles != null) {
            morphLineStyleArray.lineStyles = new MORPHLINESTYLE[lineStyles.length];
            for (int i = 0; i < lineStyles.length; i++) {
                morphLineStyleArray.lineStyles[i] = lineStyles[i].toMorphLineStyle();
            }
        }
        if (lineStyles2 != null) {
            morphLineStyleArray.lineStyles2 = new MORPHLINESTYLE2[lineStyles2.length];
            for (int i = 0; i < lineStyles2.length; i++) {
                morphLineStyleArray.lineStyles2[i] = lineStyles2[i].toMorphLineStyle2();
            }
        }
        return morphLineStyleArray;
    }
}
