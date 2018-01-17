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
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 8)
public class CSMTextSettingsTag extends Tag implements CharacterIdTag {

    public static final int ID = 74;

    public static final String NAME = "CSMTextSettings";

    @SWFType(BasicType.UI16)
    public int textID;

    @SWFType(value = BasicType.UB, count = 2)
    public int useFlashType;

    @SWFType(value = BasicType.UB, count = 3)
    public int gridFit;

    @Reserved
    @SWFType(value = BasicType.UB, count = 3)
    public int reserved;

    @SWFType(value = BasicType.FLOAT)  //F32 = FLOAT
    public float thickness;

    @SWFType(value = BasicType.FLOAT)  //F32 = FLOAT
    public float sharpness;

    @Reserved
    @SWFType(BasicType.UI8)
    public int reserved2;

    /**
     * Constructor
     *
     * @param swf
     */
    public CSMTextSettingsTag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public CSMTextSettingsTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        textID = sis.readUI16("textID");
        useFlashType = (int) sis.readUB(2, "useFlashType");
        gridFit = (int) sis.readUB(3, "gridFit");
        reserved = (int) sis.readUB(3, "reserved");
        thickness = sis.readFLOAT("thickness"); //F32 = FLOAT
        sharpness = sis.readFLOAT("sharpness"); //F32 = FLOAT
        reserved2 = sis.readUI8("reserved2"); //reserved
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(textID);
        sos.writeUB(2, useFlashType);
        sos.writeUB(3, gridFit);
        sos.writeUB(3, reserved);
        sos.writeFLOAT(thickness); //F32 = FLOAT
        sos.writeFLOAT(sharpness); //F32 = FLOAT
        sos.writeUI8(reserved2);
    }

    @Override
    public int getCharacterId() {
        return textID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.textID = characterId;
    }
}
