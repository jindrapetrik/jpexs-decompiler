/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;

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
    public int matrixX = 3;

    /**
     * Vertical matrix size
     */
    @SWFType(BasicType.UI8)
    public int matrixY = 3;

    /**
     * Divisor applied to the matrix values
     */
    @SWFType(BasicType.FLOAT)
    public float divisor = 1f;

    /**
     * Bias applied to the matrix values
     */
    @SWFType(BasicType.FLOAT)
    public float bias = 0f;

    /**
     * Matrix values
     */
    @SWFType(BasicType.FLOAT)
    public float[] matrix = new float[9];

    /**
     * Default color for pixels outside the image
     */
    public RGBA defaultColor = new RGBA(Color.BLACK);

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
    public SerializableImage apply(SerializableImage src, double zoom, int srcX, int srcY, int srcW, int srcH) {
        return Filtering.convolution(src, matrix, matrixX, matrixY, divisor, bias, defaultColor.toColor(), clamp, preserveAlpha, srcX, srcY, srcW, srcH);
    }

    @Override
    public double getDeltaX() {
        return ((matrixX-1)>>1) + 1;
    }

    @Override
    public double getDeltaY() {
        return ((matrixY-1)>>1) + 1;
    }
}
