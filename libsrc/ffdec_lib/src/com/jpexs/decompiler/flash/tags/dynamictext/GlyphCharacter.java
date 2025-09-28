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

import com.jpexs.decompiler.flash.types.GLYPHENTRY;

/**
 * Glyph character.
 *
 * @author JPEXS
 */
public class GlyphCharacter {

    /**
     * Glyph entry
     */
    public GLYPHENTRY glyphEntry;

    /**
     * Character
     */
    public char character;
    
    /**
     * Position in HTML source
     */
    public int htmlSourcePosition = -1;

    /**
     * Constructor.
     *
     * @param character Character
     * @param glyphEntry Glyph entry
     * @param htmlSourcePosition Position in HTML source
     */
    public GlyphCharacter(char character, GLYPHENTRY glyphEntry, int htmlSourcePosition) {

        this.character = character;
        this.glyphEntry = glyphEntry;
        this.htmlSourcePosition = htmlSourcePosition;
    }
}
