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
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushShortIns;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Set;

public class IntegerValueAVM2Item extends NumberValueAVM2Item {

    public Long value;

    public IntegerValueAVM2Item(AVM2Instruction instruction, Long value) {
        super(instruction);
        this.value = value;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        return writer.append("" + value);
    }

    @Override
    public Object getResult() {
        return (Double) (double) (long) value;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        AVM2Instruction ins = null;
        if (value >= 0 && value <= 255) {
            ins = new AVM2Instruction(0, new PushByteIns(), new int[]{(int) (long) value}, new byte[0]);
        } else if (value >= 0 && value <= 65535) {
            ins = new AVM2Instruction(0, new PushShortIns(), new int[]{(int) (long) value}, new byte[0]);
        } else {
            ins = new AVM2Instruction(0, new PushIntIns(), new int[]{((AVM2SourceGenerator) generator).abc.constants.getIntId(value, true)}, new byte[0]);
        }

        return toSourceMerge(localData, generator, ins);
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("int");
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
