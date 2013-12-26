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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class DefineExternalImage2 extends Tag {

    public static final int ID = 1009;
    public long characterId;
    public int bitmapFormat;
    public int targetWidth;
    public int targetHeight;
    public String exportName;
    public String fileName;
    public byte[] extraData; //?
    public static final int BITMAP_FORMAT_DEFAULT = 0;
    public static final int BITMAP_FORMAT_TGA = 1;
    public static final int BITMAP_FORMAT_DDS = 2;

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
            sos.writeUI32(characterId);
            sos.writeUI16(bitmapFormat);
            sos.writeUI16(targetWidth);
            sos.writeUI16(targetHeight);
            byte exportNameBytes[] = exportName.getBytes();
            sos.writeUI8(exportNameBytes.length);
            sos.write(exportNameBytes);
            byte fileNameBytes[] = fileName.getBytes();
            sos.writeUI8(fileNameBytes.length);
            sos.write(fileNameBytes);
            if (extraData != null) {
                sos.write(extraData);
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
    public DefineExternalImage2(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineExternalImage2", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterId = sis.readUI32();
        bitmapFormat = sis.readUI16();
        targetWidth = sis.readUI16();
        targetHeight = sis.readUI16();
        int exportNameLen = sis.readUI8();
        exportName = new String(sis.readBytesEx(exportNameLen));
        int fileNameLen = sis.readUI8();
        fileName = new String(sis.readBytesEx(fileNameLen));
        if (sis.available() > 0) { //there is usually one zero byte, bod knows why
            extraData = sis.readBytesEx(sis.available());
        }
    }
}
