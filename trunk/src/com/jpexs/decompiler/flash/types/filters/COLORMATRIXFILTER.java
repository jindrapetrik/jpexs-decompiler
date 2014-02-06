/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;

/**
 * Applies a color transformation on the pixels of a display list object
 *
 * @author JPEXS
 */
public class COLORMATRIXFILTER extends FILTER {

    /**
     * Color matrix values
     */
    @SWFType(BasicType.FLOAT)
    public float[] matrix = new float[20];

    /**
     * Constructor
     */
    public COLORMATRIXFILTER() {
        super(6);
    }

    @Override
    public SerializableImage apply(SerializableImage src) {
        float[][] matrix2 = new float[4][4];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                matrix2[y][x] = matrix[y * 5 + x];
            }
        }
        //matrix2[4][4] = 1;
        return Filtering.colorMatrix(src, matrix2);
    }
}
