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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ZONERECORD;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 8)
public class DefineFontAlignZonesTag extends Tag implements CharacterIdTag {

    public static final int ID = 73;

    public static final String NAME = "DefineFontAlignZones";

    @SWFType(BasicType.UI16)
    public int fontID;

    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = 0, text = "Thin")
    @EnumValue(value = 1, text = "Medium")
    @EnumValue(value = 2, text = "Thick")
    public int CSMTableHint;

    @Reserved
    @SWFType(value = BasicType.UB, count = 6)
    public int reserved;

    @SWFArray(value = "zone", countField = "glyphCount")
    public List<ZONERECORD> zoneTable;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineFontAlignZonesTag(SWF swf) {
        super(swf, ID, NAME, null);
        zoneTable = new ArrayList<>();
    }

    public DefineFontAlignZonesTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
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
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(fontID);
        sos.writeUB(2, CSMTableHint);
        sos.writeUB(6, reserved);
        for (ZONERECORD z : zoneTable) {
            sos.writeZONERECORD(z);
        }
    }

    @Override
    public int getCharacterId() {
        return fontID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.fontID = characterId;
    }
}
