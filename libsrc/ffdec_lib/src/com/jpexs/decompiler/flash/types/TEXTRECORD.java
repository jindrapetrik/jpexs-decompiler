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

import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TEXTRECORD implements Serializable {

    public boolean styleFlagsHasFont;

    public boolean styleFlagsHasColor;

    public boolean styleFlagsHasYOffset;

    public boolean styleFlagsHasXOffset;

    @Conditional("styleFlagsHasFont")
    @SWFType(BasicType.UI16)
    public int fontId;

    @Conditional(value = "styleFlagsHasColor", tags = {DefineTextTag.ID})
    public RGB textColor;

    @Conditional(value = "styleFlagsHasColor", tags = {DefineText2Tag.ID})
    public RGBA textColorA;

    @Conditional("styleFlagsHasXOffset")
    @SWFType(BasicType.SI16)
    public int xOffset;

    @Conditional("styleFlagsHasYOffset")
    @SWFType(BasicType.SI16)
    public int yOffset;

    @Conditional("styleFlagsHasFont")
    @SWFType(BasicType.UI16)
    public int textHeight;

    @SWFArray(countField = "glyphCount")
    public List<GLYPHENTRY> glyphEntries;

    public String getText(FontTag font) {
        StringBuilder ret = new StringBuilder();
        for (GLYPHENTRY ge : glyphEntries) {
            ret.append(font.glyphToChar(ge.glyphIndex));
        }
        return ret.toString();
    }

    public int getTotalAdvance() {
        int width = 0;
        for (GLYPHENTRY ge : glyphEntries) {
            width += ge.glyphAdvance;
        }
        return width;
    }
}
