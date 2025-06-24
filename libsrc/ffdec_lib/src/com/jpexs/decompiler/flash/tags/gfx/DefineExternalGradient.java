/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Map;

/**
 * DefineExternalGradient tag - external gradient.
 *
 * @author JPEXS
 */
public class DefineExternalGradient extends Tag {

    public static final int ID = 1003;

    public static final String NAME = "DefineExternalGradient";

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
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(gradientId);
        sos.writeUI16(bitmapsFormat);
        sos.writeUI16(gradientSize);
        sos.writeNetString(fileName);
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public DefineExternalGradient(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    public DefineExternalGradient(SWF swf) {
        super(swf, ID, NAME, null);
        fileName = "";
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        gradientId = sis.readUI16("gradientId");
        bitmapsFormat = sis.readUI16("bitmapsFormat");
        gradientSize = sis.readUI16("gradientSize");
        fileName = sis.readNetString("fileName");
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        ret.put("gid", "" + getUniqueId());
        return ret;
    }

    @Override
    public String getUniqueId() {
        return "g" + gradientId;
    }

    @Override
    public void getTagInfo(TagInfo tagInfo) {
        super.getTagInfo(tagInfo);

        tagInfo.addInfo("general", "fileName", fileName);
        String bitmapFormatStr = "0x" + Integer.toHexString(bitmapsFormat);
        switch (bitmapsFormat) {
            case BITMAP_FORMAT_DEFAULT:
                bitmapFormatStr = "default (0)";
                break;
            case BITMAP_FORMAT_TGA:
                bitmapFormatStr = "TGA (1)";
                break;
            case BITMAP_FORMAT_DDS:
                bitmapFormatStr = "DDS (2)";
                break;
        }
        tagInfo.addInfo("general", "bitmapsFormat", bitmapFormatStr);
        tagInfo.addInfo("general", "gradientSize", gradientSize);
    }
}
