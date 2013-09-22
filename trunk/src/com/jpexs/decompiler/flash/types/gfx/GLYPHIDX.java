/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.gfx;

import com.jpexs.decompiler.flash.SWFOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class GLYPHIDX {

    public int indexInFont;
    public int indexInTexture;

    public GLYPHIDX(int indexInFont, int indexInTexture) {
        this.indexInFont = indexInFont;
        this.indexInTexture = indexInTexture;
    }

    public GLYPHIDX(GFxInputStream sis) throws IOException {
        this.indexInFont = sis.readUI16();
        this.indexInTexture = sis.readUI16();
    }

    public void write(SWFOutputStream sos) throws IOException {
        sos.writeUI16(indexInFont);
        sos.writeUI16(indexInTexture);
    }
}
