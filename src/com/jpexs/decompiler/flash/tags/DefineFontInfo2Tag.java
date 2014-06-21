/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author JPEXS
 */
public class DefineFontInfo2Tag extends Tag {

    @SWFType(BasicType.UI16)
    public int fontID;
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
    public static final int ID = 62;

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(fontID);
            byte[] fontNameBytes = Utf8Helper.getBytes(fontName);
            sos.writeUI8(fontNameBytes.length);
            sos.write(fontNameBytes);
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
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param length
     * @param pos
     * @throws IOException
     */
    public DefineFontInfo2Tag(SWFInputStream sis, long pos, int length) throws IOException {
        super(sis.getSwf(), ID, "DefineFontInfo2", pos, length);
        fontID = sis.readUI16("fontID");
        int fontNameLen = sis.readUI8("fontNameLen");
        if (swf.version >= 6) {
            fontName = new String(sis.readBytesEx(fontNameLen, "fontName"), Utf8Helper.charset);
        } else {
            fontName = new String(sis.readBytesEx(fontNameLen, "fontName"));
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
}
