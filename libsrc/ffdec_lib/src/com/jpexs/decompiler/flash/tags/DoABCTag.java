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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Defines a series of ActionScript 3 bytecodes to be executed
 */
public class DoABCTag extends Tag implements ABCContainerTag {

    /**
     * ActionScript 3 bytecodes
     */
    @HideInRawEdit
    @SWFField
    private ABC abc;

    public static final int ID = 72;

    @Override
    public ABC getABC() {
        return abc;
    }

    @Override
    public String getName() {
        return "DoABC";
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DoABCTag(SWF swf) {
        super(swf, ID, "DoABC", null);
        abc = new ABC(this);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DoABCTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DoABC", data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        ABCInputStream ais = new ABCInputStream(sis.getBaseStream());
        // put it to the dumpview:
        sis.readByteRangeEx(sis.available(), "abcBytes");
        abc = new ABC(ais, swf, this);
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStream os = bos;
            if (Configuration.debugCopy.get()) {
                os = new CopyOutputStream(os, new ByteArrayInputStream(getOriginalData()));
            }
            try (SWFOutputStream sos = new SWFOutputStream(os, getVersion())) {
                abc.saveToStream(sos);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
    }

    @Override
    public int compareTo(ABCContainerTag o) {
        return 0;
    }
}
