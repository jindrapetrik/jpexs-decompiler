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
public class TEXGLYPH {

    public float uvBoundsLeft;
    public float uvBoundsTop;
    public float uvBoundsRight;
    public float uvBoundsBottom;
    public float uvOriginX;
    public float uvOriginY;

    public TEXGLYPH(float uvBoundsLeft, float uvBoundsTop, float uvBoundsRight, float uvBoundsBottom, float uvOriginX, float uvOriginY) {
        this.uvBoundsLeft = uvBoundsLeft;
        this.uvBoundsTop = uvBoundsTop;
        this.uvBoundsRight = uvBoundsRight;
        this.uvBoundsBottom = uvBoundsBottom;
        this.uvOriginX = uvOriginX;
        this.uvOriginY = uvOriginY;
    }

    public TEXGLYPH(GFxInputStream sis) throws IOException {
        this.uvBoundsLeft = sis.readFLOAT();
        this.uvBoundsTop = sis.readFLOAT();
        this.uvBoundsRight = sis.readFLOAT();
        this.uvBoundsBottom = sis.readFLOAT();
        this.uvOriginX = sis.readFLOAT();
        this.uvOriginY = sis.readFLOAT();
    }

    public void write(SWFOutputStream sos) throws IOException {
        sos.writeFLOAT(uvBoundsLeft);
        sos.writeFLOAT(uvBoundsTop);
        sos.writeFLOAT(uvBoundsRight);
        sos.writeFLOAT(uvBoundsBottom);
        sos.writeFLOAT(uvOriginX);
        sos.writeFLOAT(uvOriginY);
    }
}
