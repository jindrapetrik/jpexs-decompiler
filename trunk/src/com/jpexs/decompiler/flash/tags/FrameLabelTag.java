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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FrameLabelTag extends Tag {

    private final String name;
    private boolean namedAnchor = false;
    public static final int ID = 43;

    public String getLabelName() {
        return name;
    }

    public boolean isNamedAnchor() {
        return namedAnchor;
    }

    public FrameLabelTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "FrameLabel", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        name = sis.readString();
        if (sis.available() > 0) {
            if (sis.readUI8() == 1) {
                namedAnchor = true;
            }
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
            sos.writeString(name);
            if (namedAnchor) {
                sos.writeUI8(1);
            }
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }
}
