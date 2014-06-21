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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class DefineExternalGradient extends Tag {

    public static final int ID = 1003;
    public static final int BITMAP_FORMAT_DEFAULT = 0;
    public static final int BITMAP_FORMAT_TGA = 1;
    public static final int BITMAP_FORMAT_DDS = 2;
    public int gradientId;
    public int bitmapsFormat;
    public int gradientSize;
    public String fileName;

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
            sos.writeUI16(gradientId);
            sos.writeUI16(bitmapsFormat);
            sos.writeUI16(gradientSize);
            byte fileNameBytes[] = fileName.getBytes();
            sos.writeUI8(fileNameBytes.length);
            sos.write(fileNameBytes);
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
    public DefineExternalGradient(SWFInputStream sis, long pos, int length) throws IOException {
        super(sis.getSwf(), ID, "DefineExternalGradient", pos, length);
        gradientId = sis.readUI16("gradientId");
        bitmapsFormat = sis.readUI16("bitmapsFormat");
        gradientSize = sis.readUI16("gradientSize");
        int fileNameLen = sis.readUI8("fileNameLen");
        fileName = new String(sis.readBytesEx(fileNameLen, "fileName"));
    }
}
