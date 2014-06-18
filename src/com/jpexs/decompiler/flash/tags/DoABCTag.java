/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.SWFLimitedInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.types.annotations.Internal;
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
    @Internal
    private final ABC abc;
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
     * @param headerData
     * @param data Data bytes
     * @param pos
     * @throws IOException
     */
    public DoABCTag(SWFLimitedInputStream sis, long pos, int length) throws IOException {
        super(sis.swf, ID, "DoABC", pos, length);
        abc = new ABC(sis.getBaseStream(), swf, this);
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
        }
        return new byte[0];
    }

    @Override
    public int compareTo(ABCContainerTag o) {
        return 0;
    }
}
