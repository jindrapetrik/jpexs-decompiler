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
public class TEXGLYPH implements Serializable {

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
        this.uvBoundsLeft = sis.readFLOAT("uvBoundsLeft");
        this.uvBoundsTop = sis.readFLOAT("uvBoundsTop");
        this.uvBoundsRight = sis.readFLOAT("uvBoundsRight");
        this.uvBoundsBottom = sis.readFLOAT("uvBoundsBottom");
        this.uvOriginX = sis.readFLOAT("uvOriginX");
        this.uvOriginY = sis.readFLOAT("uvOriginY");
    }

    public void write(GFxOutputStream sos) throws IOException {
        sos.writeFLOAT(uvBoundsLeft);
        sos.writeFLOAT(uvBoundsTop);
        sos.writeFLOAT(uvBoundsRight);
        sos.writeFLOAT(uvBoundsBottom);
        sos.writeFLOAT(uvOriginX);
        sos.writeFLOAT(uvOriginY);
    }
}
