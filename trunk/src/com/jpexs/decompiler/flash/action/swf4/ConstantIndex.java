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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.util.ArrayList;
import java.util.List;

public class ConstantIndex {

    public int index;
    public List<String> constantPool;

    public ConstantIndex(int index) {
        this.index = index;
        this.constantPool = new ArrayList<String>();
    }

    public ConstantIndex(int index, List<String> constantPool) {
        this.index = index;
        this.constantPool = constantPool;
    }

    @Override
    public String toString() {
        if (Main.RESOLVE_CONSTANTS) {
            if (constantPool != null) {
                if (index < constantPool.size()) {
                    return "\"" + Helper.escapeString(constantPool.get(index)) + "\"";
                }
            }
        }
        return "constant" + index;
    }
}
