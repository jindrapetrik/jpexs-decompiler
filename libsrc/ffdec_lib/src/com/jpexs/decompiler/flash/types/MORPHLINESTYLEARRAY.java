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

import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import java.io.Serializable;

/**
 * Morph line style array.
 *
 * @author JPEXS
 */
public class MORPHLINESTYLEARRAY implements Serializable {

    /**
     * Line styles
     */
    @Conditional(tags = {DefineMorphShapeTag.ID})
    public MORPHLINESTYLE[] lineStyles;

    /**
     * Line styles 2
     */
    @Conditional(tags = {DefineMorphShape2Tag.ID})
    public MORPHLINESTYLE2[] lineStyles2;

    /**
     * Get line styles at ratio.
     * @param shapeNum Shape number (DefineMorphShape = 1, DefineMorphShape2 = 2)
     * @param ratio Ratio
     * @return Line styles
     */
    public LINESTYLEARRAY getLineStylesAt(int shapeNum, int ratio) {
        LINESTYLEARRAY ret = new LINESTYLEARRAY();
        if (shapeNum == 1) {
            ret.lineStyles = new LINESTYLE[lineStyles.length];
            for (int m = 0; m < lineStyles.length; m++) {
                ret.lineStyles[m] = lineStyles[m].getLineStyleAt(ratio);
            }
        }
        if (shapeNum == 2) {
            ret.lineStyles2 = new LINESTYLE2[lineStyles2.length];
            for (int m = 0; m < lineStyles2.length; m++) {
                ret.lineStyles2[m] = lineStyles2[m].getLineStyle2At(ratio);
            }
        }
        return ret;
    }

    /**
     * Get start line styles.
     * @param shapeNum Shape number (DefineMorphShape = 1, DefineMorphShape2 = 2)
     * @return Start line styles
     */
    public LINESTYLEARRAY getStartLineStyles(int shapeNum) {
        LINESTYLEARRAY ret = new LINESTYLEARRAY();
        if (shapeNum == 1) {
            ret.lineStyles = new LINESTYLE[lineStyles.length];
            for (int m = 0; m < lineStyles.length; m++) {
                ret.lineStyles[m] = lineStyles[m].getStartLineStyle();
            }
        }
        if (shapeNum == 2) {
            ret.lineStyles2 = new LINESTYLE2[lineStyles2.length];
            for (int m = 0; m < lineStyles2.length; m++) {
                ret.lineStyles2[m] = lineStyles2[m].getStartLineStyle2();
            }
        }
        return ret;
    }

    /**
     * Get end line styles.
     * @param shapeNum Shape number (DefineMorphShape = 1, DefineMorphShape2 = 2)
     * @return End line styles
     */
    public LINESTYLEARRAY getEndLineStyles(int shapeNum) {
        LINESTYLEARRAY ret = new LINESTYLEARRAY();
        if (shapeNum == 1) {
            ret.lineStyles = new LINESTYLE[lineStyles.length];
            for (int m = 0; m < lineStyles.length; m++) {
                ret.lineStyles[m] = lineStyles[m].getEndLineStyle();
            }
        }
        if (shapeNum == 2) {
            ret.lineStyles2 = new LINESTYLE2[lineStyles2.length];
            for (int m = 0; m < lineStyles2.length; m++) {
                ret.lineStyles2[m] = lineStyles2[m].getEndLineStyle2();
            }
        }
        return ret;
    }
}
