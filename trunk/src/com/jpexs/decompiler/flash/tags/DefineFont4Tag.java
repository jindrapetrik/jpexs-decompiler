/*
 *  Copyright (C) 2010-2013 JPEXS
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
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DefineFont4Tag extends CharacterTag {

    public int fontID;
    public boolean fontFlagsHasFontData;
    public boolean fontFlagsItalic;
    public boolean fontFlagsBold;
    public String fontName;
    public byte fontData[];
    public static final int ID = 91;

    @Override
    public int getCharacterId() {
        return fontID;
    }

    public DefineFont4Tag(byte[] data, int version, long pos) throws IOException {
        super(ID, "DefineFont4", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        fontID = sis.readUI16();
        sis.readUB(5);
        fontFlagsHasFontData = sis.readUB(1) == 1;
        fontFlagsItalic = sis.readUB(1) == 1;
        fontFlagsBold = sis.readUB(1) == 1;
        fontName = sis.readString();
        fontData = sis.readBytes(sis.available());
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            sos.writeUI16(fontID);
            sos.writeUB(5, 0);
            sos.writeUB(1, fontFlagsHasFontData ? 1 : 0);
            sos.writeUB(1, fontFlagsItalic ? 1 : 0);
            sos.writeUB(1, fontFlagsBold ? 1 : 0);
            sos.writeString(fontName);
            sos.write(fontData);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }
}
