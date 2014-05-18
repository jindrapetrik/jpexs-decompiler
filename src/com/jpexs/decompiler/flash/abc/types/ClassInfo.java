/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import java.util.List;

public class ClassInfo {

    public int cinit_index; //MethodInfo - static initializer
    public Traits static_traits = new Traits();

    public boolean deleted;

    @Override
    public String toString() {
        return "method_index=" + cinit_index + "\r\n" + static_traits.toString();
    }

    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        return "method_index=" + cinit_index + "\r\n" + static_traits.toString(abc, fullyQualifiedNames);
    }
}
