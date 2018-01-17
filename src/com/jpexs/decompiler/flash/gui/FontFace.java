/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import java.awt.Font;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class FontFace implements Comparable<FontFace> {

    public Font font;

    public FontFace(Font font) {
        this.font = font;
    }

    @Override
    public String toString() {
        String face = font.getFontName();
        String fam = font.getFamily();
        if (face.startsWith(fam)) {
            face = face.substring(fam.length()).trim();
        }
        if (face.startsWith(".")) {
            face = face.substring(1);
        }
        return face;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.font);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FontFace other = (FontFace) obj;
        return Objects.equals(this.font, other.font);
    }

    @Override
    public int compareTo(FontFace o) {
        return font.getFontName().compareTo(o.font.getFontName());
    }
}
