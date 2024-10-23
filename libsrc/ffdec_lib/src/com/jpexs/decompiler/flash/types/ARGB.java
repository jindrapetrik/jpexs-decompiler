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

import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Color;
import java.io.Serializable;

/**
 * Represents 32-bit alpha, red, green and blue value.
 *
 * @author JPEXS
 */
public class ARGB implements Serializable {

    /**
     * Alpha value defining opacity
     */
    @SWFType(BasicType.UI8)
    public int alpha;

    /**
     * Red color value
     */
    @SWFType(BasicType.UI8)
    public int red;

    /**
     * Green color value
     */
    @SWFType(BasicType.UI8)
    public int green;

    /**
     * Blue color value
     */
    @SWFType(BasicType.UI8)
    public int blue;

    @Override
    public String toString() {
        return "[ARGB a=" + alpha + ",r=" + red + ",g=" + green + ",b=" + blue + "]";
    }

    /**
     * Converts this to color.
     * @return Color
     */
    public Color toColor() {
        return new Color(red, green, blue, alpha);
    }

    /**
     * Converts this to integer.
     * @return Integer
     */
    public int toInt() {
        return RGBA.toInt(red, green, blue, alpha);
    }

    /**
     * Constructor.
     */
    public ARGB() {

    }
}
