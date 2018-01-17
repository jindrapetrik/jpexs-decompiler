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
public class FONTINFO implements Serializable {

    public int fontId;

    public GLYPHIDX[] glyphIndices;

    public FONTINFO(int fontId, int numGlyphs, GLYPHIDX[] glyphIndices) {
        this.fontId = fontId;
        this.glyphIndices = glyphIndices;
    }

    public FONTINFO(GFxInputStream sis) throws IOException {
        fontId = sis.readUI16("fontId");
        int numGlyphs = sis.readUI16("numGlyphs");
        glyphIndices = new GLYPHIDX[numGlyphs];
        for (int i = 0; i < numGlyphs; i++) {
            glyphIndices[i] = new GLYPHIDX(sis);
        }
    }

    public void write(GFxOutputStream sos) throws IOException {
        sos.writeUI16(fontId);
        sos.writeUI16(glyphIndices.length);
        for (int i = 0; i < glyphIndices.length; i++) {
            glyphIndices[i].write(sos);
        }
    }
}
