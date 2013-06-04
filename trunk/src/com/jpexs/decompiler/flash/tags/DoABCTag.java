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

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import java.io.*;

/**
 * Defines a series of ActionScript 3 bytecodes to be executed
 */
public class DoABCTag extends Tag implements ABCContainerTag {

    /**
     * ActionScript 3 bytecodes
     */
    private ABC abc;

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
     * @param data Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public DoABCTag(byte[] data, int version, long pos) throws IOException {
        super(72, "DoABC", data, pos);
        InputStream is = new ByteArrayInputStream(data);
        abc = new ABC(is);
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
            if (Configuration.DEBUG_COPY) {
                os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
            }
            try (SWFOutputStream sos = new SWFOutputStream(os, version)) {
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
