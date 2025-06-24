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

import java.util.ArrayList;
import java.util.List;

/**
 * Same style text record.
 *
 * @author JPEXS
 */
public class SameStyleTextRecord {

    /**
     * Text style
     */
    public TextStyle style;

    /**
     * X offset
     */
    public int xOffset;

    /**
     * Width
     */
    public int width;

    /**
     * Glyph entries
     */
    public List<GlyphCharacter> glyphEntries = new ArrayList<>();

    /**
     * Calculates text widths
     * @return Total width
     */
    public int calculateTextWidths() {

        int width = 0;
        for (GlyphCharacter gc : glyphEntries) {
            width += gc.glyphEntry.glyphAdvance;
        }
        this.width = width;
        return width;
    }
}
