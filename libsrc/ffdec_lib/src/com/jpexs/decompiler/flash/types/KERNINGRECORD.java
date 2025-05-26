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

import com.jpexs.decompiler.flash.helpers.FontHelper;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Font;
import java.io.Serializable;

/**
 * Kerning record.
 *
 * @author JPEXS
 */
public class KERNINGRECORD implements Serializable {

    /**
     * Font code 1
     */
    @SWFType(value = BasicType.UI8, alternateValue = BasicType.UI16, alternateCondition = "fontFlagsWideCodes")
    public int fontKerningCode1;

    /**
     * Font code 2
     */
    @SWFType(value = BasicType.UI8, alternateValue = BasicType.UI16, alternateCondition = "fontFlagsWideCodes")
    public int fontKerningCode2;

    /**
     * Kerning adjustment
     */
    @SWFType(BasicType.SI16)
    public int fontKerningAdjustment;

    @Override
    public String toString() {
        return "[KERNINGRECORD fontKerningCode1=" + fontKerningCode1 + ", fontKerningCode2=" + fontKerningCode2 + ", fontKerningAdjustment=" + fontKerningAdjustment + "]";
    }

    /**
     * Constructor.
     * @param fontKerningCode1 Font code 1
     * @param fontKerningCode2 Font code 2
     * @param fontKerningAdjustment Kerning adjustment
     */
    public KERNINGRECORD(int fontKerningCode1, int fontKerningCode2, int fontKerningAdjustment) {
        this.fontKerningCode1 = fontKerningCode1;
        this.fontKerningCode2 = fontKerningCode2;
        this.fontKerningAdjustment = fontKerningAdjustment;
    }

    /**
     * Constructor.
     */
    public KERNINGRECORD() {
    }

    /**
     * Constructor.
     * @param font Font
     * @param char1 Font code 1
     * @param char2 Font code 2
     */
    public KERNINGRECORD(Font font, char char1, char char2) {
        fontKerningCode1 = char1;
        fontKerningCode2 = char2;
        fontKerningAdjustment = FontHelper.getFontCharsKerning(font, char1, char2);
    }
}
