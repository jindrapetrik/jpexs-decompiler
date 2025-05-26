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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Color;
import java.io.Serializable;

/**
 * 24-bit red, green, blue value.
 *
 * @author JPEXS
 */
public class RGB implements Serializable {

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

    /**
     * Constructor.
     * @param color Color to copy
     */
    public RGB(RGB color) {
        this.red = color.red;
        this.green = color.green;
        this.blue = color.blue;
    }

    /**
     * Constructor.
     * @param red Red color value
     * @param green Green color value
     * @param blue Blue color value
     */
    public RGB(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Constructor.
     */
    public RGB() {
    }

    /**
     * Converts this RGB to hex string.
     * @return Hex string
     */
    public String toHexRGB() {
        String rh = Integer.toHexString(red);
        if (rh.length() < 2) {
            rh = "0" + rh;
        }
        String gh = Integer.toHexString(green);
        if (gh.length() < 2) {
            gh = "0" + gh;
        }
        String bh = Integer.toHexString(blue);
        if (bh.length() < 2) {
            bh = "0" + bh;
        }
        return "#" + rh + gh + bh;
    }

    /**
     * Converts this RGB to Color.
     * @return Color
     */
    public Color toColor() {
        return new Color(red, green, blue);
    }

    /**
     * Converts this RGB to int.
     * @return int
     */
    public int toInt() {
        return toInt(red, green, blue);
    }

    /**
     * Converts RGB to int.
     * @param red Red color value
     * @param green Green color value
     * @param blue Blue color value
     * @return int
     */
    public static int toInt(int red, int green, int blue) {
        return (0xFF << 24)
                | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8)
                | (blue & 0xFF);
    }

    /**
     * Constructor.
     * @param color Color
     */
    public RGB(Color color) {
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
    }

    /**
     * Constructor.
     * @param rgb RGB value
     */
    public RGB(int rgb) {
        red = (rgb >> 16) & 0xFF;
        green = (rgb >> 8) & 0xFF;
        blue = rgb & 0xFF;
    }

    @Override
    public String toString() {
        if (Configuration.useHexColorFormat.get()) {
            return "[RGB " + toHexRGB() + "]";
        } else {
            return "[RGB red:" + red + ", green:" + green + ", blue:" + blue + "]";
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.red;
        hash = 97 * hash + this.green;
        hash = 97 * hash + this.blue;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RGB other = (RGB) obj;
        if (this.red != other.red) {
            return false;
        }
        if (this.green != other.green) {
            return false;
        }
        return this.blue == other.blue;
    }

}
