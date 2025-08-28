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
package com.jpexs.decompiler.flash.types.text.format;

import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Color;

/**
 *
 * @author JPEXS
 */
public class ControlTextFormatRecord implements TextFormatRecord {
    public static final int TYPE_STYLE = 0;
    public static final int TYPE_FONT_ID = 1;
    public static final int TYPE_FONT_HEIGHT = 2;
    public static final int TYPE_COLOR = 3;
    public static final int TYPE_SCRIPT = 4;
    public static final int TYPE_KERNING = 5;
    public static final int TYPE_ALIGN = 8;
    public static final int TYPE_INDENT = 9;
    public static final int TYPE_LEFT_MARGIN = 10;
    public static final int TYPE_RIGHT_MARGIN = 11;
    public static final int TYPE_LINE_SPACE = 12;
    
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_ITALIC = 2;
    
    
    public static final int SCRIPT_NORMAL = 0;
    public static final int SCRIPT_SUPERSCRIPT = 1;
    public static final int SCRIPT_SUBSCRIPT = 2;                
    
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_JUSTIFY = 3;
    
    @EnumValue(value = TYPE_STYLE, text = "style")    
    @EnumValue(value = TYPE_FONT_ID, text = "font id")    
    @EnumValue(value = TYPE_FONT_HEIGHT, text = "font height")
    @EnumValue(value = TYPE_COLOR, text = "color")        
    @EnumValue(value = TYPE_SCRIPT, text = "script")        
    @EnumValue(value = TYPE_KERNING, text = "kerning")        
    @EnumValue(value = TYPE_ALIGN, text = "align")        
    @EnumValue(value = TYPE_INDENT, text = "indent")        
    @EnumValue(value = TYPE_LEFT_MARGIN, text = "left margin")        
    @EnumValue(value = TYPE_RIGHT_MARGIN, text = "right margin")        
    @EnumValue(value = TYPE_LINE_SPACE, text = "line space")        
    public int type;
    
    @Conditional(value = "type", options = {TYPE_STYLE})
    @SWFType(BasicType.UI8)
    @EnumValue(value = STYLE_NORMAL, text = "normal")
    @EnumValue(value = STYLE_BOLD, text = "bold")
    @EnumValue(value = STYLE_ITALIC, text = "italic")
    @EnumValue(value = STYLE_BOLD | STYLE_ITALIC, text = "bold + italic")
    public int style;
    
    @Conditional(value = "type", options = {TYPE_FONT_ID})
    @SWFType(BasicType.UI16)
    public int fontId;
    
    @Conditional(value = "type", options = {TYPE_FONT_HEIGHT})
    @SWFType(BasicType.UI16)
    public int fontHeight;
    
    @Conditional(value = "type", options = {TYPE_COLOR})
    public RGBA color = new RGBA(Color.BLACK);
    
    @Conditional(value = "type", options = {TYPE_SCRIPT})    
    @SWFType(BasicType.UI8)
    @EnumValue(value = SCRIPT_NORMAL, text = "normal")
    @EnumValue(value = SCRIPT_SUPERSCRIPT, text = "superscript")
    @EnumValue(value = SCRIPT_SUBSCRIPT, text = "subscript")
    public int script;
    
    @Conditional(value = "type", options = {TYPE_KERNING})    
    @SWFType(BasicType.SI16)    
    public int kerning;
    
    @Conditional(value = "type", options = {TYPE_ALIGN})    
    @SWFType(BasicType.UI8)
    @EnumValue(value = ALIGN_LEFT, text = "left")
    @EnumValue(value = ALIGN_CENTER, text = "center")
    @EnumValue(value = ALIGN_RIGHT, text = "right")
    @EnumValue(value = ALIGN_JUSTIFY, text = "justify")
    public int align;
    
    @Conditional(value = "type", options = {TYPE_INDENT}) 
    @SWFType(BasicType.SI16)
    public int indent;
    
    @Conditional(value = "type", options = {TYPE_LEFT_MARGIN}) 
    @SWFType(BasicType.SI16)
    public int leftMargin;
    
    @Conditional(value = "type", options = {TYPE_RIGHT_MARGIN}) 
    @SWFType(BasicType.SI16)
    public int rightMargin;
    
    @Conditional(value = "type", options = {TYPE_LINE_SPACE}) 
    @SWFType(BasicType.SI16)
    public int lineSpace;

    @Override
    public int getFormatRecordId() {
        return 0x80 + type;
    }
}
