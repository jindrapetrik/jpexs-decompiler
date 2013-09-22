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
public class KerningPairType {

    public int char1;
    public int char2;
    public int advance;

    public KerningPairType(int char1, int char2, int advance) {
        this.char1 = char1;
        this.char2 = char2;
        this.advance = advance;
    }

    public KerningPairType(GFxInputStream sis) throws IOException {
        this.char1 = sis.readUI16();
        this.char2 = sis.readUI16();
        this.advance = sis.readSI16();
    }

    public void write(SWFOutputStream sos) throws IOException {
        sos.writeUI16(char1);
        sos.writeUI16(char2);
        sos.writeSI16(advance);
    }
}
