/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DefineFontNameTag extends Tag {

    @SWFType(BasicType.UI16)
    public int fontId;
    public String fontName;
    public String fontCopyright;
    public static final int ID = 88;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineFontNameTag(SWF swf) {
        super(swf, ID, "DefineFontName", null);
        fontName = "New Font Name";
        fontCopyright = "Copyright text";
    }

    public DefineFontNameTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineFontName", data);
        fontId = sis.readUI16("fontId");
        fontName = sis.readString("fontName");
        fontCopyright = sis.readString("fontCopyright");
    }

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
            sos.writeUI16(fontId);
            sos.writeString(fontName);
            sos.writeString(fontCopyright);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }
}
