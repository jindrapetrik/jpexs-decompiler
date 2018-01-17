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

import com.jpexs.decompiler.flash.helpers.FontHelper;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Font;
import java.io.Serializable;

/**
 * Represents 24-bit red, green, blue value
 *
 * @author JPEXS
 */
public class KERNINGRECORD implements Serializable {

    @SWFType(value = BasicType.UI8, alternateValue = BasicType.UI16, alternateCondition = "fontFlagsWideCodes")
    public int fontKerningCode1;

    @SWFType(value = BasicType.UI8, alternateValue = BasicType.UI16, alternateCondition = "fontFlagsWideCodes")
    public int fontKerningCode2;

    @SWFType(BasicType.SI16)
    public int fontKerningAdjustment;

    @Override
    public String toString() {
        return "[KERNINGRECORD fontKerningCode1=" + fontKerningCode1 + ", fontKerningCode2=" + fontKerningCode2 + ", fontKerningAdjustment=" + fontKerningAdjustment + "]";
    }

    public KERNINGRECORD(int fontKerningCode1, int fontKerningCode2, int fontKerningAdjustment) {
        this.fontKerningCode1 = fontKerningCode1;
        this.fontKerningCode2 = fontKerningCode2;
        this.fontKerningAdjustment = fontKerningAdjustment;
    }

    public KERNINGRECORD() {
    }

    public KERNINGRECORD(Font font, char char1, char char2) {
        fontKerningCode1 = char1;
        fontKerningCode2 = char2;
        fontKerningAdjustment = FontHelper.getFontCharsKerning(font, char1, char2);
    }
}
