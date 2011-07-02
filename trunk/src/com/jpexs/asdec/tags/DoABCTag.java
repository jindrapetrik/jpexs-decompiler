/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.tags;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.CopyOutputStream;

import java.io.*;

/**
 * Defines a series of ActionScript 3 bytecodes to be executed
 */
public class DoABCTag extends Tag {
    /**
     * ActionScript 3 bytecodes
     */
    public ABC abc;
    /**
     * A 32-bit flags value, which may
     * contain the following bits set:
     * kDoAbcLazyInitializeFlag = 1:
     * Indicates that the ABC block
     * should not be executed
     * immediately, but only parsed. A
     * later finddef may cause its
     * scripts to execute.
     */
    public long flags;
    /**
     * The name assigned to the bytecode.
     */
    public String name;

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "DoABC (" + name + ")";
    }

    /**
     * Constructor
     *
     * @param data    Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public DoABCTag(byte[] data, int version) {
        super(82, data);       
        try {
            InputStream is = new ByteArrayInputStream(data);
            SWFInputStream sis = new SWFInputStream(is, version);
            flags = sis.readUI32();
            name = sis.readString();
            abc = new ABC(is);

        } catch (IOException e) {

        }
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
            if (Main.DEBUG_COPY) {
                os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
            }
            SWFOutputStream sos = new SWFOutputStream(os, version);
            sos.writeUI32(flags);
            sos.writeString(name);
            abc.saveToStream(sos);
            sos.close();
            return bos.toByteArray();
        } catch (IOException e) {

        }
        return new byte[0];
    }
}
