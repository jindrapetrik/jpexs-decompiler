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
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Sets the index of an object within the tab order.
 *
 * @author JPEXS
 */
public class SetTabIndexTag extends Tag {

    /**
     * Depth of character
     */
    @SWFType(BasicType.UI16)
    public int depth;
    /**
     * Tab order value
     */
    @SWFType(BasicType.UI16)
    public int tabIndex;
    public static final int ID = 66;

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
            sos.writeUI16(depth);
            sos.writeUI16(tabIndex);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     * @param headerData
     * @param length
     * @param data Data bytes
     * @param pos
     * @throws IOException
     */
    public SetTabIndexTag(SWFLimitedInputStream sis, long pos, int length) throws IOException {
        super(sis.swf, ID, "SetTabIndex", pos, length);
        depth = sis.readUI16();
        tabIndex = sis.readUI16();
    }
}
