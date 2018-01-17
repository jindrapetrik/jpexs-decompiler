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
import com.jpexs.decompiler.flash.tags.base.FontInfoTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 6)
public class DefineFontInfo2Tag extends FontInfoTag {

    public static final int ID = 62;

    public static final String NAME = "DefineFontInfo2";

    public String fontName;

    @Reserved
    @SWFType(value = BasicType.UB, count = 2)
    public int reserved;

    public boolean fontFlagsSmallText;

    public boolean fontFlagsShiftJIS;

    public boolean fontFlagsANSI;

    public boolean fontFlagsItalic;

    public boolean fontFlagsBold;

    public boolean fontFlagsWideCodes; //always 1

    public LANGCODE languageCode;

    @SWFType(BasicType.UI16)
    public List<Integer> codeTable;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineFontInfo2Tag(SWF swf) {
        super(swf, ID, NAME, null);
        fontName = "New Font Info Name";
        languageCode = new LANGCODE();
        codeTable = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineFontInfo2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        fontID = sis.readUI16("fontID");
        if (swf.version >= 6) {
            fontName = sis.readNetString("fontName", Utf8Helper.charset);
        } else {
            fontName = sis.readNetString("fontName");
        }
        reserved = (int) sis.readUB(2, "reserved");
        fontFlagsSmallText = sis.readUB(1, "fontFlagsSmallText") == 1;
        fontFlagsShiftJIS = sis.readUB(1, "fontFlagsShiftJIS") == 1;
        fontFlagsANSI = sis.readUB(1, "fontFlagsANSI") == 1;
        fontFlagsItalic = sis.readUB(1, "fontFlagsItalic") == 1;
        fontFlagsBold = sis.readUB(1, "fontFlagsBold") == 1;
        fontFlagsWideCodes = sis.readUB(1, "fontFlagsWideCodes") == 1; //Always 1
        languageCode = sis.readLANGCODE("languageCode");
        int ctLen = sis.available() / 2;
        codeTable = new ArrayList<>();
        for (int i = 0; i < ctLen; i++) {
            codeTable.add(sis.readUI16("code"));
        }
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
        if (swf.version >= 6) {
            sos.writeNetString(fontName, Utf8Helper.charset);
        } else {
            sos.writeNetString(fontName);
        }
        sos.writeUB(2, reserved);
        sos.writeUB(1, fontFlagsSmallText ? 1 : 0);
        sos.writeUB(1, fontFlagsShiftJIS ? 1 : 0);
        sos.writeUB(1, fontFlagsANSI ? 1 : 0);
        sos.writeUB(1, fontFlagsItalic ? 1 : 0);
        sos.writeUB(1, fontFlagsBold ? 1 : 0);
        sos.writeUB(1, fontFlagsWideCodes ? 1 : 0);
        sos.writeLANGCODE(languageCode);
        for (int c : codeTable) {
            sos.writeUI16(c);
        }
    }

    @Override
    public List<Integer> getCodeTable() {
        return codeTable;
    }

    @Override
    public void addFontCharacter(int index, int character) {
        codeTable.add(index, character);
        setModified(true);
    }

    @Override
    public void removeFontCharacter(int index) {
        codeTable.remove(index);
        setModified(true);
    }

    @Override
    public String getFontName() {
        return fontName;
    }

    @Override
    public boolean getFontFlagsBold() {
        return fontFlagsBold;
    }

    @Override
    public void setFontFlagsBold(boolean value) {
        fontFlagsBold = value;
    }

    @Override
    public boolean getFontFlagsItalic() {
        return fontFlagsItalic;
    }

    @Override
    public void setFontFlagsItalic(boolean value) {
        fontFlagsItalic = value;
    }
}
