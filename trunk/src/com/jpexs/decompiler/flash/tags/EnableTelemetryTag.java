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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Optional;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Enable flash profiling information
 *
 * @author JPEXS
 */
public class EnableTelemetryTag extends Tag {

    public static final int ID = 93;

    @SWFType(value = BasicType.UB, count = 16)
    @Reserved
    public int reserved;

    @Optional
    @SWFType(value = BasicType.UI8, count = 32)
    public byte[] passwordHash;

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
            sos.writeUB(16, reserved);
            if (passwordHash != null) {
                sos.write(passwordHash);
            }
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public EnableTelemetryTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        reserved = (int) sis.readUB(16);
        if (sis.available() > 0) {
            passwordHash = sis.readBytesEx(32);
        }
    }
}
