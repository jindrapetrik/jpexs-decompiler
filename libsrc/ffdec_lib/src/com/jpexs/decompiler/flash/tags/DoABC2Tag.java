/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecialType;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 * Defines a series of ActionScript 3 bytecodes to be executed
 *
 * @author JPEXS
 */
@SWFVersion(from = 9)
public class DoABC2Tag extends Tag implements ABCContainerTag {

    public static final int ID = 82;

    public static final String NAME = "DoABC2";

    /**
     * ActionScript 3 bytecodes
     */
    @HideInRawEdit
    @SWFField
    private ABC abc;

    /**
     * A 32-bit flags value, which may contain the following bits set:
     * kDoAbcLazyInitializeFlag = 1: Indicates that the ABC block should not be
     * executed immediately, but only parsed. A later finddef may cause its
     * scripts to execute.
     */
    @SWFType(BasicType.UI32)
    public long flags;

    /**
     * The name assigned to the bytecode.
     */
    public String name;

    /**
     * Constructor
     *
     * @param swf
     */
    public DoABC2Tag(SWF swf) {
        super(swf, ID, NAME, null);
        name = "New DoABC";
        abc = new ABC(this);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DoABC2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        flags = sis.readUI32("flags");
        name = sis.readString("name");

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
        sos.writeUI32(flags);
        sos.writeString(name);
        abc.saveToStream(sos);
    }

    @Override
    public ABC getABC() {
        return abc;
    }

    @Override
    public String getName() {
        return super.getName() + (!name.isEmpty() ? " (" + name + ")" : "");
    }

    @Override
    public int compareTo(ABCContainerTag o) {
        if (o instanceof DoABC2Tag) {
            DoABC2Tag n = (DoABC2Tag) o;
            int lastCmp = name.compareTo(n.name);
            return (lastCmp != 0 ? lastCmp
                    : name.compareTo(n.name));
        }
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
