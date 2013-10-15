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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.model.LocalData;

public class ThisAVM2Item extends AVM2Item {

    public Multiname className;

    public ThisAVM2Item(Multiname className) {
        super(null, PRECEDENCE_PRIMARY);
        this.className = className;
    }

    @Override
    public String toString() {
        return "this";
    }

    @Override
    protected HilightedTextWriter appendTo(HilightedTextWriter writer, LocalData localData) {
        return writer.append("this");
    }
}
