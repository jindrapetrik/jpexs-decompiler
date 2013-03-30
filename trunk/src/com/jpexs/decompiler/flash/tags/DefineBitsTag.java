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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DefineBitsTag extends CharacterTag {

    public int characterID;
    public byte jpegData[];

    public DefineBitsTag(byte[] data, int version, long pos) throws IOException {
        super(6, "DefineBits", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterID = sis.readUI16();
        jpegData = sis.readBytes(sis.available());
    }

    public byte[] getFullImageData(JPEGTablesTag jtt) {
        if ((jtt != null)) {
            ByteArrayOutputStream baos = null;

            try {
                baos = new ByteArrayOutputStream();
                byte jttdata[] = jtt.getData(10);
                if (jttdata.length != 0) {
                    baos.write(jttdata, SWF.hasErrorHeader(jttdata) ? 4 : 0, jttdata.length - (SWF.hasErrorHeader(jttdata) ? 6 : 2));
                    baos.write(jpegData, SWF.hasErrorHeader(jpegData) ? 6 : 2, jpegData.length - (SWF.hasErrorHeader(jttdata) ? 6 : 2));
                } else {
                    baos.write(jpegData, 0, jpegData.length);
                }

            } finally {
                if (baos != null) {
                    try {
                        baos.close();
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
            return baos.toByteArray();
        }
        return null;
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
            sos.writeUI16(characterID);
            sos.write(jpegData);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    @Override
    public int getCharacterID() {
        return characterID;
    }
}
