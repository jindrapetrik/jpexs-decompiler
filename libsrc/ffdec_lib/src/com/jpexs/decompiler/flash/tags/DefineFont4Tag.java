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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 10)
public class DefineFont4Tag extends CharacterTag {

    public static final int ID = 91;

    public static final String NAME = "DefineFont4";

    @SWFType(BasicType.UI16)
    public int fontID;

    @Reserved
    @SWFType(value = BasicType.UB, count = 5)
    public int reserved;

    public boolean fontFlagsHasFontData;

    public boolean fontFlagsItalic;

    public boolean fontFlagsBold;

    public String fontName;

    public ByteArrayRange fontData;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineFont4Tag(SWF swf) {
        super(swf, ID, NAME, null);
        fontID = swf.getNextCharacterId();
        fontName = "New font";
        fontData = ByteArrayRange.EMPTY;
    }

    public DefineFont4Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        fontID = sis.readUI16("fontID");
        reserved = (int) sis.readUB(5, "reserved");
        fontFlagsHasFontData = sis.readUB(1, "fontFlagsHasFontData") == 1;
        fontFlagsItalic = sis.readUB(1, "fontFlagsItalic") == 1;
        fontFlagsBold = sis.readUB(1, "fontFlagsBold") == 1;
        fontName = sis.readString("fontName");
        fontData = sis.readByteRangeEx(sis.available(), "fontData");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(fontID);
        sos.writeUB(5, reserved);
        sos.writeUB(1, fontFlagsHasFontData ? 1 : 0);
        sos.writeUB(1, fontFlagsItalic ? 1 : 0);
        sos.writeUB(1, fontFlagsBold ? 1 : 0);
        sos.writeString(fontName);
        sos.write(fontData);
    }

    @Override
    public int getCharacterId() {
        return fontID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.fontID = characterId;
    }
}
