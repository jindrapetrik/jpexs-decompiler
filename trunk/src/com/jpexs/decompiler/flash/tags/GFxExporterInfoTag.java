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
import java.io.ByteArrayInputStream;
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
public class GFxExporterInfoTag extends Tag {

    public static final int ID = 1000;
    //Version (1.10 will be encoded as 0x10A)
    public int version;
    //Version 1.10 (0x10A) and above - flags
    public long flags;
    public int bitmapFormat;
    public byte[] prefix;
    public String swfName;
    public List<Long> codeOffsets;
    public static final int BITMAP_FORMAT_TGA = 1;
    public static final int BITMAP_FORMAT_DDS = 2;
    public static final int FLAG_CONTAINS_GLYPH_TEXTURES = 1;
    public static final int FLAG_GLYPHS_STRIPPED_FROM_DEFINEFONT = 2;
    public static final int FLAG_GRADIENT_IMAGES_EXPORTED = 4;

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
            sos.writeUI16(this.version);
            if (this.version >= 0x10a) {
                sos.writeUI32(flags);
            }
            sos.writeUI16(bitmapFormat);
            sos.writeUI8(prefix.length);
            sos.write(prefix);
            byte swfNameBytes[] = swfName.getBytes();
            sos.writeUI8(swfNameBytes.length);
            sos.write(swfNameBytes);
            if (codeOffsets != null) {
                sos.writeUI16(codeOffsets.size());
                for (long l : codeOffsets) {
                    sos.writeUI32(l);
                }
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
    public GFxExporterInfoTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "ExporterInfo", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        this.version = sis.readUI16();
        if (this.version >= 0x10a) {
            flags = sis.readUI32();
        }
        bitmapFormat = sis.readUI16();
        int prefixLen = sis.readUI8();
        prefix = sis.readBytes(prefixLen);
        int swfNameLen = sis.readUI8();
        swfName = new String(sis.readBytes(swfNameLen));
        if (sis.available() > 0) // (version >= 0x401) //?                
        {
            codeOffsets = new ArrayList<>();
            int numCodeOffsets = sis.readUI16();
            for (int i = 0; i < numCodeOffsets; i++) {
                codeOffsets.add(sis.readUI32());
            }
        }
    }
}
