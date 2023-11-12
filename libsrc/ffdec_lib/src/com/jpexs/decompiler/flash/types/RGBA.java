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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Color;
import java.io.Serializable;

/**
 * Represents 32-bit red, green, blue and alpha value
 *
 * @author JPEXS
 */
public class RGBA extends RGB implements Serializable {

    /**
     * Alpha value defining opacity
     */
    @SWFType(BasicType.UI8)
    public int alpha;

    public float getAlphaFloat() {
        return ((float) alpha) / 255.0f;
    }

    public String toHexARGB() {
        String ra = Integer.toHexString(alpha);
        if (ra.length() < 2) {
            ra = "0" + ra;
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
        return "#" + ra + rh + gh + bh;
    }

    public RGBA(int red, int green, int blue, int alpha) {
        super(red, green, blue);
        this.alpha = alpha;
    }

    public RGBA(Color color) {
        super(color);
        alpha = color.getAlpha();
    }

    public RGBA(int rgb) {
        super(rgb);
        alpha = (rgb >> 24) & 0xFF;
    }
    
    public RGBA(RGB color) {
        super(color);
        if (color instanceof RGBA) {
            alpha = ((RGBA) color).alpha;
        } else {
            alpha = 255;
        }        
    }

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
        if (obj instanceof RGB) {
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
