/*
 *  Copyright (C) 2010-2014 PEXS
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
package com.jpexs.decompiler.flash.types.gfx;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
public class GlyphInfoType implements Serializable {

    public int glyphCode;
    public int advanceX;
    public long globalOffset;

    public GlyphInfoType(int glyphCode, int advance, int glyphPos) {
        this.glyphCode = glyphCode;
        this.advanceX = advance;
        this.globalOffset = glyphPos;
    }

    public GlyphInfoType(GFxInputStream sis) throws IOException {
        this.glyphCode = sis.readUI16();
        this.advanceX = sis.readSI16();
        this.globalOffset = sis.readUI32();
    }

    public void write(GFxOutputStream sos) throws IOException {
        sos.writeUI16(glyphCode);
        sos.writeSI16(advanceX);
        sos.writeUI32(globalOffset);
    }
}
