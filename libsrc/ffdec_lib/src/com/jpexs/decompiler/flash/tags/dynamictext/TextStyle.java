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

import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.RGBA;

/**
 * Text style.
 *
 * @author JPEXS
 */
public final class TextStyle implements Cloneable {

    /**
     * Font tag
     */
    public FontTag font;

    /**
     * Font face
     */
    public String fontFace;

    /**
     * Font height
     */
    public int fontHeight;

    /**
     * Font leading
     */
    public int fontLeading;

    /**
     * Bold
     */
    public boolean bold;

    /**
     * Italic
     */
    public boolean italic;

    /**
     * Underlined
     */
    public boolean underlined;

    /**
     * Text color
     */
    public RGBA textColor;

    /**
     * Letter spacing
     */
    public double letterSpacing;

    /**
     * Kerning
     */
    public boolean kerning;

    /**
     * Left margin
     */
    public double leftMargin;

    @Override
    public TextStyle clone() {
        try {
            TextStyle result = (TextStyle) super.clone();
            return result;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }
}
