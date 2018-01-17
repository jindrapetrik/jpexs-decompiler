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

/**
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
     * @throws java.io.IOException
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
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineExternalGradient(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        gradientId = sis.readUI16("gradientId");
        bitmapsFormat = sis.readUI16("bitmapsFormat");
        gradientSize = sis.readUI16("gradientSize");
        fileName = sis.readNetString("fileName");
    }
}
