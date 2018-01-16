/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
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
        this.glyphCode = sis.readUI16("glyphCode");
        this.advanceX = sis.readSI16("advanceX");
        this.globalOffset = sis.readUI32("globalOffset");
    }

    public void write(GFxOutputStream sos) throws IOException {
        sos.writeUI16(glyphCode);
        sos.writeSI16(advanceX);
        sos.writeUI32(globalOffset);
    }
}
