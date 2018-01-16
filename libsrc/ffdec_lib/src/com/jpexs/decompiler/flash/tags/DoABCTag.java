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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecialType;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 * Defines a series of ActionScript 3 bytecodes to be executed
 *
 * @author JPEXS
 */
@SWFVersion(from = 9)
public class DoABCTag extends Tag implements ABCContainerTag {

    public static final int ID = 72;

    public static final String NAME = "DoABC";

    /**
     * ActionScript 3 bytecodes
     */
    @HideInRawEdit
    @SWFField
    private ABC abc;

    /**
     * Constructor
     *
     * @param swf
     */
    public DoABCTag(SWF swf) {
        super(swf, ID, NAME, null);
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
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        ABCInputStream ais = new ABCInputStream(sis.getBaseStream());

        // put it to the dumpview:
        sis.readByteRangeEx(sis.available(), "abcBytes", DumpInfoSpecialType.ABC_BYTES, null);
        abc = new ABC(ais, swf, this);
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        abc.saveToStream(sos);
    }

    @Override
    public ABC getABC() {
        return abc;
    }

    @Override
    public int compareTo(ABCContainerTag o) {
        return 0;
    }

    @Override
    public void setModified(boolean value) {
        super.setModified(value);
        if (value == false && !isModified()) {
            ABC abc = getABC();
            for (ScriptInfo si : abc.script_info) {
                si.setModified(false);
            }
        }
    }
}
