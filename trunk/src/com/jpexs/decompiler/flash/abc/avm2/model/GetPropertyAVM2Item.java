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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

public class GetPropertyAVM2Item extends AVM2Item {

    public GraphTargetItem object;
    public GraphTargetItem propertyName;

    public GetPropertyAVM2Item(AVM2Instruction instruction, GraphTargetItem object, GraphTargetItem propertyName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return formatProperty(writer, object, propertyName, localData);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, object,
                new AVM2Instruction(0, new GetPropertyIns(), new int[]{((AVM2SourceGenerator) generator).propertyName(propertyName)}, new byte[0])
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
