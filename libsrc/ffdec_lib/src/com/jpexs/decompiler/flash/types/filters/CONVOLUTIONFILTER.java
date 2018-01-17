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
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;

/**
 * Two-dimensional discrete convolution filter.
 *
 * @author JPEXS
 */
public class CONVOLUTIONFILTER extends FILTER {

    /**
     * Horizontal matrix size
     */
    @SWFType(BasicType.UI8)
    public int matrixX;

    /**
     * Vertical matrix size
     */
    @SWFType(BasicType.UI8)
    public int matrixY;

    /**
     * Divisor applied to the matrix values
     */
    @SWFType(BasicType.FLOAT)
    public float divisor;

    /**
     * Bias applied to the matrix values
     */
    @SWFType(BasicType.FLOAT)
    public float bias;

    /**
     * Matrix values
     */
    @SWFType(BasicType.FLOAT)
    public float[][] matrix = new float[0][0];

    /**
     * Default color for pixels outside the image
     */
    public RGBA defaultColor;

    @Reserved
    @SWFType(value = BasicType.UB, count = 6)
    public int reserved;

    /**
     * Clamp mode
     */
    public boolean clamp;

    /**
     * Preserve the alpha
     */
    public boolean preserveAlpha;

    /**
     * Constructor
     */
    public CONVOLUTIONFILTER() {
        super(5);
    }

    @Override
    public SerializableImage apply(SerializableImage src) {
        int height = matrix.length;
        int width = matrix[0].length;
        float[] matrix2 = new float[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                matrix2[y * width + x] = matrix[x][y] * divisor + bias;
            }
        }
        return Filtering.convolution(src, matrix2, width, height);
    }

    @Override
    public double getDeltaX() {
        return 0;
    }

    @Override
    public double getDeltaY() {
        return 0;
    }
}
