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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

public class ConstructPropAVM2Item extends AVM2Item {

    public GraphTargetItem object;
    public FullMultinameAVM2Item propertyName;
    public List<GraphTargetItem> args;

    public ConstructPropAVM2Item(AVM2Instruction instruction, GraphTargetItem object, FullMultinameAVM2Item propertyName, List<GraphTargetItem> args) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
        this.args = args;
    }

    @Override
    protected GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("new ");
        int idx = writer.getLength();
        object.toString(writer, localData);
        if (idx < writer.getLength()) {
            writer.append(".");
        }
        propertyName.toString(writer, localData);
        writer.append("(");
        for (int a = 0; a < args.size(); a++) {
            if (a > 0) {
                writer.append(",");
            }
            args.get(a).toString(writer, localData);
        }
        return writer.append(")");
    }
}
