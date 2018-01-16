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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ExporterInfo extends Tag {

    public static final int ID = 1000;

    public static final String NAME = "ExporterInfo";

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
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(this.version);
        if (this.version >= 0x10a) {
            sos.writeUI32(flags);
        }
        sos.writeUI16(bitmapFormat);
        sos.writeUI8(prefix.length);
        sos.write(prefix);
        sos.writeNetString(swfName);
        if (codeOffsets != null) {
            sos.writeUI16(codeOffsets.size());
            for (long l : codeOffsets) {
                sos.writeUI32(l);
            }
        }
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public ExporterInfo(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        this.version = sis.readUI16("version");
        if (this.version >= 0x10a) {
            flags = sis.readUI32("flags");
        }
        bitmapFormat = sis.readUI16("bitmapFormat");
        int prefixLen = sis.readUI8("prefixLen");
        prefix = sis.readBytesEx(prefixLen, "prefix");
        swfName = sis.readNetString("swfName");
        if (sis.available() > 0) // (version >= 0x401) //?
        {
            codeOffsets = new ArrayList<>();
            int numCodeOffsets = sis.readUI16("numCodeOffsets");
            for (int i = 0; i < numCodeOffsets; i++) {
                codeOffsets.add(sis.readUI32("codeOffset"));
            }
        }
    }
}
