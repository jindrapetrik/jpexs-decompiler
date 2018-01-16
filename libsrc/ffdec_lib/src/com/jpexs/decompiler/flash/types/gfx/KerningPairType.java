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
public class KerningPairType implements Serializable {

    public int char1;

    public int char2;

    public int advance;

    public KerningPairType(int char1, int char2, int advance) {
        this.char1 = char1;
        this.char2 = char2;
        this.advance = advance;
    }

    public KerningPairType(GFxInputStream sis) throws IOException {
        this.char1 = sis.readUI16("char1");
        this.char2 = sis.readUI16("char2");
        this.advance = sis.readSI16("advance");
    }

    public void write(GFxOutputStream sos) throws IOException {
        sos.writeUI16(char1);
        sos.writeUI16(char2);
        sos.writeSI16(advance);
    }
}
