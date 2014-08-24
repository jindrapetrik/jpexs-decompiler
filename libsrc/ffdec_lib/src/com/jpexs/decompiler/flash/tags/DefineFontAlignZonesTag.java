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
 * License along with this library. */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ZONERECORD;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DefineFontAlignZonesTag extends Tag {

    @SWFType(BasicType.UI16)
    public int fontID;
    @SWFType(value = BasicType.UB, count = 2)
    public int CSMTableHint;
    @Reserved
    @SWFType(value = BasicType.UB, count = 6)
    public int reserved;
    @SWFArray(value = "zone", countField = "glyphCount")
    public List<ZONERECORD> zoneTable;
    public static final int ID = 73;

    public DefineFontAlignZonesTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineFontAlignZones", data);
        fontID = sis.readUI16("fontID");
        CSMTableHint = (int) sis.readUB(2, "CSMTableHint");
        reserved = (int) sis.readUB(6, "reserved");
        zoneTable = new ArrayList<>();
        while (sis.available() > 0) {
            ZONERECORD zr = sis.readZONERECORD("record");
            zoneTable.add(zr);
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
            sos.writeUI16(fontID);
            sos.writeUB(2, CSMTableHint);
            sos.writeUB(6, reserved);
            for (ZONERECORD z : zoneTable) {
                sos.writeZONERECORD(z);
            }
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }
}
