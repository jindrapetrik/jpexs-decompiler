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
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class FontFamily implements Comparable<FontFamily> {

    public String familyEn;

    public String family;

    public FontFamily(Font font) {
        this(font.getFamily(Locale.ENGLISH), font.getFamily());
    }

    public FontFamily(String familyEn, String family) {
        this.familyEn = familyEn;
        this.family = family;
    }

    @Override
    public String toString() {
        return family;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.familyEn);
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
        final FontFamily other = (FontFamily) obj;
        return Objects.equals(this.familyEn, other.familyEn);
    }

    @Override
    public int compareTo(FontFamily o) {
        return family.compareTo(o.family);
    }
}
