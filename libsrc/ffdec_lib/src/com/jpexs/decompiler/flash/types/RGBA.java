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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Color;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 32-bit red, green, blue and alpha value.
 *
 * @author JPEXS
 */
public class RGBA extends RGB implements Serializable {

    /**
     * Alpha value defining opacity
     */
    @SWFType(BasicType.UI8)
    public int alpha;

    /**
     * Get alpha value as float from 0.0 to 1.0
     * @return Alpha value as float from 0.0 to 1.0
     */
    public float getAlphaFloat() {
        return ((float) alpha) / 255.0f;
    }

    /**
     * Convert to hex string in format #AARRGGBB
     * @return Hex string in format #AARRGGBB
     */
    public String toHexARGB() {
        String ah = Integer.toHexString(alpha);
        if (ah.length() < 2) {
            ah = "0" + ah;
        }
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
        return "#" + ah + rh + gh + bh;
    }

    /**
     * Constructor.
     * @param red Red value
     * @param green Green value
     * @param blue Blue value
     * @param alpha Alpha value
     */
    public RGBA(int red, int green, int blue, int alpha) {
        super(red, green, blue);
        this.alpha = alpha;
    }

    /**
     * Constructor.
     * @param color Color
     */
    public RGBA(Color color) {
        super(color);
        alpha = color.getAlpha();
    }

    /**
     * Constructor.
     * @param rgb RGB value
     */
    public RGBA(int rgb) {
        super(rgb);
        alpha = (rgb >> 24) & 0xFF;
    }

    /**
     * Constructor.
     * @param color Color
     */
    public RGBA(RGB color) {
        super(color);
        if (color instanceof RGBA) {
            alpha = ((RGBA) color).alpha;
        } else {
            alpha = 255;
        }
    }
    
    /**
     * Converts hex ARGB to RGBA color
     * @param hex Hex color in format #aarrggbb
     * @return RGBA color
     */
    public static RGBA fromHexARGB(String hex) {
        Pattern hexPat = Pattern.compile("^#(?<a>[0-9a-fA-F]{2})(?<r>[0-9a-fA-F]{2})(?<g>[0-9a-fA-F]{2})(?<b>[0-9a-fA-F]{2})$");
        Matcher m = hexPat.matcher(hex);
        if (!m.matches()) {
            throw new NumberFormatException("Not a valid color code with alpha");
        }
        int a = Integer.parseInt(m.group("a"), 16);
        int r = Integer.parseInt(m.group("r"), 16);
        int g = Integer.parseInt(m.group("g"), 16);
        int b = Integer.parseInt(m.group("b"), 16);
        
        return new RGBA(r, g, b, a);
    }

    /**
     * Constructor.
     */
    public RGBA() {
    }

    @Override
    public Color toColor() {
        return new Color(red, green, blue, alpha);
    }

    @Override
    public int toInt() {
        return toInt(red, green, blue, alpha);
    }

    /**
     * Converts red, green, blue and alpha values to 32-bit integer.
     * @param red Red value
     * @param green Green value
     * @param blue Blue value
     * @param alpha Alpha value
     * @return 32-bit integer
     */
    public static int toInt(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24)
                | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8)
                | (blue & 0xFF);
    }

    @Override
    public String toString() {
        if (Configuration.useHexColorFormat.get()) {
            return "[RGB " + toHexRGB() + "]";
        } else {
            return "[RGB red:" + red + ", green:" + green + ", blue:" + blue + ", alpha:" + alpha + "]";
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.red;
        hash = 97 * hash + this.green;
        hash = 97 * hash + this.blue;
        hash = 97 * hash + this.alpha;
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
        if ((obj instanceof RGB) && (!(obj instanceof RGBA))) {
            final RGB other = (RGBA) obj;
            if (this.alpha != 255) {
                return false;
            }
            if (this.red != other.red) {
                return false;
            }
            if (this.green != other.green) {
                return false;
            }
            if (this.blue != other.blue) {
                return false;
            }
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RGBA other = (RGBA) obj;
        if (this.red != other.red) {
            return false;
        }
        if (this.green != other.green) {
            return false;
        }
        if (this.blue != other.blue) {
            return false;
        }
        return this.alpha == other.alpha;
    }        
}
