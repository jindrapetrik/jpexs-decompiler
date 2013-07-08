/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWFInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DefineFontNameTag extends Tag {

    public int fontId;
    public String fontName;
    public String fontCopyright;
    public static final int ID = 88;

    public DefineFontNameTag(byte[] data, int version, long pos) throws IOException {
        super(ID, "DefineFontName", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        fontId = sis.readUI16();
        fontName = sis.readString();
        fontCopyright = sis.readString();
    }
}
