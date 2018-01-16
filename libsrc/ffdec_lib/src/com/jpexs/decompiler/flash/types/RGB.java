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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Color;
import java.io.Serializable;

/**
 * Represents 24-bit red, green, blue value
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

    public RGB(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public RGB() {
    }

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

    public Color toColor() {
        return new Color(red, green, blue);
    }

    public int toInt() {
        return toInt(red, green, blue);
    }

    public static int toInt(int red, int green, int blue) {
        return (0xFF << 24)
                | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8)
                | (blue & 0xFF);
    }

    public RGB(Color color) {
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
    }

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
}
