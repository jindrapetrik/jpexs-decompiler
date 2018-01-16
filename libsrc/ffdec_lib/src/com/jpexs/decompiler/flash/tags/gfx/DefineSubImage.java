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
public class DefineSubImage extends Tag {

    public static final int ID = 1008;

    public static final String NAME = "DefineSubImage";

    public int characterId;

    public int imageCharacterId;

    public int x1;

    public int y1;

    public int x2;

    public int y2;

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterId);
        sos.writeUI16(imageCharacterId);
        sos.writeUI16(x1);
        sos.writeUI16(y1);
        sos.writeUI16(x2);
        sos.writeUI16(y2);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineSubImage(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterId = sis.readUI16("characterId");
        imageCharacterId = sis.readUI16("imageCharacterId");
        x1 = sis.readUI16("x1");
        y1 = sis.readUI16("y1");
        x2 = sis.readUI16("x2");
        y2 = sis.readUI16("y2");
    }
}
