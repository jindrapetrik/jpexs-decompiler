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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.List;

/**
 * Text record.
 *
 * @author JPEXS
 */
public class TEXTRECORD implements Serializable {

    /**
     * Has font style flag
     */
    public boolean styleFlagsHasFont;

    /**
     * Has color style flag
     */
    public boolean styleFlagsHasColor;

    /**
     * Has Y offset style flag
     */
    public boolean styleFlagsHasYOffset;

    /**
     * Has X offset style flag
     */
    public boolean styleFlagsHasXOffset;

    /**
     * Font id
     */
    @Conditional("styleFlagsHasFont")
    @SWFType(BasicType.UI16)
    public int fontId;

    /**
     * Text color
     */
    @Conditional(value = "styleFlagsHasColor", tags = {DefineTextTag.ID})
    public RGB textColor;

    /**
     * Text color with alpha
     */
    @Conditional(value = "styleFlagsHasColor", tags = {DefineText2Tag.ID})
    public RGBA textColorA;

    /**
     * X offset
     */
    @Conditional("styleFlagsHasXOffset")
    @SWFType(BasicType.SI16)
    public int xOffset;

    /**
     * Y offset
     */
    @Conditional("styleFlagsHasYOffset")
    @SWFType(BasicType.SI16)
    public int yOffset;

    /**
     * Text height
     */
    @Conditional("styleFlagsHasFont")
    @SWFType(BasicType.UI16)
    public int textHeight;

    /**
     * Glyph entries
     */
    @SWFArray(countField = "glyphCount")
    public List<GLYPHENTRY> glyphEntries;

    /**
     * Get text from glyph entries.
     * @param font Font tag
     * @return Text
     */
    public String getText(FontTag font) {
        StringBuilder ret = new StringBuilder();
        for (GLYPHENTRY ge : glyphEntries) {
            ret.append(font.glyphToChar(ge.glyphIndex));
        }
        return ret.toString();
    }

    /**
     * Get total advance of all glyph entries.
     * @return Total advance
     */
    public int getTotalAdvance() {
        int width = 0;
        for (GLYPHENTRY ge : glyphEntries) {
            width += ge.glyphAdvance;
        }
        return width;
    }
    
    public FontTag getFont(SWF swf) {
        if (fontId == -1) {
            return null;
        }
        return swf.getFont(fontId);
    }
}
