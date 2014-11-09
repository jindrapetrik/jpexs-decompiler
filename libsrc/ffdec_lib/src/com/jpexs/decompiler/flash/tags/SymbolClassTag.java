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
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SymbolClassTag extends Tag {

    @SWFType(value = BasicType.UI16)
    @SWFArray(value = "tag", countField = "numSymbols")
    public int[] tags;
    @SWFArray(value = "name", countField = "numSymbols")
    public String[] names;
    public static final int ID = 76;

    /**
     * Constructor
     *
     * @param swf
     */
    public SymbolClassTag(SWF swf) {
        super(swf, ID, "SymbolClass", null);
        tags = new int[0];
        names = new String[0];
    }

    public SymbolClassTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "SymbolClass", data);
        int numSymbols = sis.readUI16("numSymbols");
        tags = new int[numSymbols];
        names = new String[numSymbols];
        for (int ii = 0; ii < numSymbols; ii++) {
            int tagID = sis.readUI16("tagID");
            String className = sis.readString("className");
            tags[ii] = tagID;
            names[ii] = className;
        }
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
            int numSymbols = tags.length;
            sos.writeUI16(numSymbols);
            for (int ii = 0; ii < numSymbols; ii++) {
                sos.writeUI16(tags[ii]);
                sos.writeString(names[ii]);
            }
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }
}
