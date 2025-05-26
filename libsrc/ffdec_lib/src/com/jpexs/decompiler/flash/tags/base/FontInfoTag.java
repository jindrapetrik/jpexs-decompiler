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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.util.List;
import java.util.Map;

/**
 * Base class for font info tags.
 *
 * @author JPEXS
 */
public abstract class FontInfoTag extends Tag implements CharacterIdTag {

    /**
     * Font ID
     */
    @SWFType(BasicType.UI16)
    public int fontID;

    /**
     * Constructor.
     *
     * @param swf SWF
     * @param id Tag ID
     * @param name Tag name
     * @param data Tag data
     */
    public FontInfoTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    /**
     * Gets the font character codes.
     * @return List of font character codes
     */
    public abstract List<Integer> getCodeTable();

    /**
     * Adds a character to the font.
     * @param index Index
     * @param character Character
     */
    public abstract void addFontCharacter(int index, int character);

    /**
     * Removes a character from the font.
     * @param index Index
     */
    public abstract void removeFontCharacter(int index);

    @Override
    public int getCharacterId() {
        return fontID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.fontID = characterId;
    }

    /**
     * Gets the font name.
     * @return Font name
     */
    public abstract String getFontName();

    /**
     * Gets the bold font flag.
     * @return Bold font flag
     */
    public abstract boolean getFontFlagsBold();

    /**
     * Sets the bold font flag.
     * @param value Bold font flag
     */
    public abstract void setFontFlagsBold(boolean value);

    /**
     * Gets the italic font flag.
     * @return Italic font flag
     */
    public abstract boolean getFontFlagsItalic();

    /**
     * Sets the italic font flag.
     * @param value Italic font flag
     */
    public abstract void setFontFlagsItalic(boolean value);

    /**
     * Checks if the font is Shift-JIS encoded.
     * @return True if the font is Shift-JIS encoded
     */
    public abstract boolean isShiftJIS();

    /**
     * Checks if the font is ANSI encoded.
     * @return True if the font is ANSI encoded
     */
    public abstract boolean isAnsi();

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        ret.put("fid", "" + fontID);
        return ret;
    }
}
