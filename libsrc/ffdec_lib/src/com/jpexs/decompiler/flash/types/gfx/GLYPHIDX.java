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
public class GLYPHIDX implements Serializable {

    public int indexInFont;

    public int indexInTexture;

    public GLYPHIDX(int indexInFont, int indexInTexture) {
        this.indexInFont = indexInFont;
        this.indexInTexture = indexInTexture;
    }

    public GLYPHIDX(GFxInputStream sis) throws IOException {
        this.indexInFont = sis.readUI16("indexInFont");
        this.indexInTexture = sis.readUI16("indexInTexture");
    }

    public void write(GFxOutputStream sos) throws IOException {
        sos.writeUI16(indexInFont);
        sos.writeUI16(indexInTexture);
    }
}
