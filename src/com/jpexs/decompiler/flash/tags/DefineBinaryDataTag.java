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
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DefineBinaryDataTag extends CharacterTag {

    @SWFType(BasicType.UI16)
    public int tag;

    public byte[] binaryData;

    @Reserved
    @SWFType(BasicType.UI32)
    public long reserved;

    public static final int ID = 87;

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
            sos.writeUI16(tag);
            sos.writeUI32(reserved);
            sos.write(binaryData);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    public DefineBinaryDataTag(SWFInputStream sis, long pos, int length) throws IOException {
        super(sis.getSwf(), ID, "DefineBinaryData", pos, length);
        tag = sis.readUI16("tag");
        reserved = sis.readUI32("reserved");
        binaryData = sis.readBytesEx(sis.available(),"binaryData");
    }

    @Override
    public int getCharacterId() {
        return tag;
    }
}
