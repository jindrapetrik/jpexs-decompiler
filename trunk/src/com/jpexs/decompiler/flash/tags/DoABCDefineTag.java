/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private final ABC abc;
    /**
     * A 32-bit flags value, which may contain the following bits set:
     * kDoAbcLazyInitializeFlag = 1: Indicates that the ABC block should not be
     * executed immediately, but only parsed. A later finddef may cause its
     * scripts to execute.
     */
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
     *
     * @param swf
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public DoABCDefineTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DoABCDefine", data, pos);
        InputStream is = new ByteArrayInputStream(data);
        SWFInputStream sis = new SWFInputStream(is, version);
        flags = sis.readUI32();
        name = sis.readString();
        abc = new ABC(is, swf);
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStream os = bos;
            if (Configuration.debugCopy.get()) {
                os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
            }
            try (SWFOutputStream sos = new SWFOutputStream(os, version)) {
                sos.writeUI32(flags);
                sos.writeString(name);
                abc.saveToStream(sos);
            }
            return bos.toByteArray();
        } catch (IOException e) {
        }
        return new byte[0];
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
