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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Defines a series of ActionScript 3 bytecodes to be executed
 */
public class DoABCDefineTag extends Tag implements ABCContainerTag {

    @Override
    public ABC getABC() {
        return abc;
    }
    /**
     * ActionScript 3 bytecodes
     */
    @Internal
    private final ABC abc;
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
    public static final int ID = 82;

    @Override
    public String getName() {
        return "DoABCDefine (" + name + ")";
    }

    /**
     * Constructor
     * @param swf
     */
    public DoABCDefineTag(SWF swf) {
        super(swf, ID, "DoABCDefine", null);
        name = "New DoABC";
        abc = new ABC(swf);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DoABCDefineTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DoABCDefine", data);
        flags = sis.readUI32("flags");
        name = sis.readString("name");

        ABCInputStream ais = new ABCInputStream(sis.getBaseStream());
        // put it to the dumpview:
        sis.readBytesEx(sis.available(), "abcBytes");
        abc = new ABC(ais, swf, this);
    }

    @Override
    public void setSwf(SWF swf) {
        super.setSwf(swf);
        abc.swf = swf;
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
                os = new CopyOutputStream(os, new ByteArrayInputStream(getOriginalData().getRangeData()));
            }
            try (SWFOutputStream sos = new SWFOutputStream(os, getVersion())) {
                sos.writeUI32(flags);
                sos.writeString(name);
                abc.saveToStream(sos);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
    }

    @Override
    public int compareTo(ABCContainerTag o) {
        if (o instanceof DoABCDefineTag) {
            DoABCDefineTag n = (DoABCDefineTag) o;
            int lastCmp = name.compareTo(n.name);
            return (lastCmp != 0 ? lastCmp
                    : name.compareTo(n.name));
        }
        return 0;
    }
}
