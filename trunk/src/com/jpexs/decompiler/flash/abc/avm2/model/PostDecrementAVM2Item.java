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
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIns;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.AssignmentAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AssignableAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

public class PostDecrementAVM2Item extends AVM2Item implements AssignmentAVM2Item {

    public GraphTargetItem object;

    public PostDecrementAVM2Item(AVM2Instruction instruction, GraphTargetItem object) {
        super(instruction, PRECEDENCE_POSTFIX);
        this.object = object;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        object.toString(writer, localData);
        return writer.append("--");
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        return object.returnType();
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if (object instanceof AssignableAVM2Item) {
            return ((AssignableAVM2Item) object).toSourceChange(localData, generator, null, toSourceMerge(localData, generator,
                    new AVM2Instruction(0, new DecrementIns(), new int[]{}, new byte[0])
            ), true);
        }
        return new ArrayList<>(); //?
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if (object instanceof AssignableAVM2Item) {
            return ((AssignableAVM2Item) object).toSourceChange(localData, generator, null, toSourceMerge(localData, generator,
                    new AVM2Instruction(0, new DecrementIns(), new int[]{}, new byte[0])
            ), false);
        }
        return new ArrayList<>(); //?
    }
}
