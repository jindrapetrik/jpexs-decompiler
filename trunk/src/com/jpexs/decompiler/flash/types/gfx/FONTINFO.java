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

/**
 *
 * @author JPEXS
 */
public class FONTINFO {

    public int fontId;
    public GLYPHIDX glyphIndices[];

    public FONTINFO(int fontId, int numGlyphs, GLYPHIDX[] glyphIndices) {
        this.fontId = fontId;
        this.glyphIndices = glyphIndices;
    }

    public FONTINFO(GFxInputStream sis) throws IOException {
        fontId = sis.readUI16();
        int numGlyphs = sis.readUI16();
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
