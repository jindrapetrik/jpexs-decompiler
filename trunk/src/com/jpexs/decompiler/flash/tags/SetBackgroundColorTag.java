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
import com.jpexs.decompiler.flash.types.RGB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SetBackgroundColorTag extends Tag {

    public RGB backgroundColor;
    public static final int ID = 9;

    public SetBackgroundColorTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "SetBackgroundColor", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        backgroundColor = sis.readRGB();
    }

    public SetBackgroundColorTag(SWF swf, RGB backgroundColor) {
        super(swf, ID, "SetBackgroundColor", new byte[0], 0);
        this.backgroundColor = backgroundColor;
    }

    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeRGB(backgroundColor);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }
}
