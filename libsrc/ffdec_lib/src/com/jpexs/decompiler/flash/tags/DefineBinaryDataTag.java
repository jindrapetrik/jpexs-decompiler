/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
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

    @Internal
    public SWF innerSwf;
    
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
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     * @param swf
     */
    public DefineBinaryDataTag(SWF swf) {
        super(swf, ID, "DefineBinaryData", null);
    }

    public DefineBinaryDataTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineBinaryData", data);
        tag = sis.readUI16("tag");
        reserved = sis.readUI32("reserved");
        binaryData = sis.readBytesEx(sis.available(), "binaryData");
    }

    @Override
    public int getCharacterId() {
        return tag;
    }
}
