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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SymbolClassTag extends Tag {

    @SWFType(value=BasicType.UI16)
    @SWFArray(value="tag",countField = "numSymbols")
    public int[] tags;
    @SWFArray(value="name",countField = "numSymbols")
    public String[] names;
    public static final int ID = 76;

    public SymbolClassTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "SymbolClass", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        int numSymbols = sis.readUI16();
        tags = new int[numSymbols];
        names = new String[numSymbols];
        for (int ii = 0; ii < numSymbols; ii++) {
            int tagID = sis.readUI16();
            String className = sis.readString();
            tags[ii] = tagID;
            names[ii] = className;
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            int numSymbols = tags.length;
            sos.writeUI16(numSymbols);
            for (int ii = 0; ii < numSymbols; ii++) {
                sos.writeUI16(tags[ii]);
                sos.writeString(names[ii]);
            }
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }
}
