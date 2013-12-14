/*
 *  Copyright (C) 2013 JPEXS
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
package com.jpexs.decompiler.flash.abc;

import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class ClassPath {

    public String packageStr;
    public String className;

    public ClassPath(String packageStr, String className) {
        this.packageStr = packageStr;
        this.className = className;
    }

    @Override
    public String toString() {
        return (packageStr == null || packageStr.isEmpty()) ? className : packageStr + "." + className;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.packageStr);
        hash = 37 * hash + Objects.hashCode(this.className);
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
        final ClassPath other = (ClassPath) obj;
        if (!Objects.equals(this.packageStr, other.packageStr)) {
            return false;
        }
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        return true;
    }
}
