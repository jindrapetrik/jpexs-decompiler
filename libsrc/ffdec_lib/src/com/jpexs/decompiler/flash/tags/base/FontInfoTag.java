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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class FontInfoTag extends Tag implements CharacterIdTag {

    @SWFType(BasicType.UI16)
    public int fontID;

    public FontInfoTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract List<Integer> getCodeTable();

    public abstract void addFontCharacter(int index, int character);

    public abstract void removeFontCharacter(int index);

    @Override
    public int getCharacterId() {
        return fontID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.fontID = characterId;
    }

    public abstract String getFontName();

    public abstract boolean getFontFlagsBold();

    public abstract void setFontFlagsBold(boolean value);

    public abstract boolean getFontFlagsItalic();

    public abstract void setFontFlagsItalic(boolean value);
}
