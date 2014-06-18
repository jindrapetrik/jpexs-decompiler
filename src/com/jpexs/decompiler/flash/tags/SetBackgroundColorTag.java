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
import com.jpexs.decompiler.flash.SWFLimitedInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.RGB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SetBackgroundColorTag extends Tag {

    public RGB backgroundColor;
    public static final int ID = 9;

    public SetBackgroundColorTag(SWFLimitedInputStream sis, long pos, int length) throws IOException {
        super(sis.swf, ID, "SetBackgroundColor", pos, length);
        backgroundColor = sis.readRGB();
    }

    public SetBackgroundColorTag(SWF swf, RGB backgroundColor) {
        super(swf, ID, "SetBackgroundColor", 0, 0);
        this.backgroundColor = backgroundColor;
    }

    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, getVersion());
        try {
            sos.writeRGB(backgroundColor);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }
}
