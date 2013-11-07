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

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.TernarOpItem;

public class NameValuePair extends AVM2Item {

    public GraphTargetItem name;
    //public GraphTargetItem value;

    public NameValuePair(GraphTargetItem name, GraphTargetItem value) {
        super(name.src, NOPRECEDENCE);
        this.name = name;
        this.value = value;
    }

    @Override
    protected GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        name.toString(writer, localData);
        writer.append(":");
        if (value instanceof TernarOpItem) { //Ternar operator contains ":"
            writer.append("(");
            value.toString(writer, localData);
            writer.append(")");
        } else {
            value.toString(writer, localData);
        }
        return writer;
    }
}
