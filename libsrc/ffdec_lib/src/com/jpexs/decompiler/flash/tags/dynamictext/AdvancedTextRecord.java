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
package com.jpexs.decompiler.flash.tags.dynamictext;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import java.util.List;

/**
 * Text record with font class.
 * @author JPEXS
 */
public class AdvancedTextRecord extends TEXTRECORD {
    public String fontClass = null;      
    
    public List<Integer> htmlSourcePositions = null;
    
    @Override
    public FontTag getFont(SWF swf) {
        if (fontClass != null) {
            return swf.getFontByClass(fontClass);
        }
        return super.getFont(swf);
    }        
}
