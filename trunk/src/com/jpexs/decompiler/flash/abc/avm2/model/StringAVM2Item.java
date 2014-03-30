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
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushStringIns;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.List;
import java.util.Set;

public class StringAVM2Item extends AVM2Item {

    public String value;

    public StringAVM2Item(AVM2Instruction instruction, String value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        return writer.append("\"" + Helper.escapeString(value) + "\"");
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return true;
    }

    @Override
    public Object getResult() {
        return value;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator,
                new AVM2Instruction(0, new PushStringIns(), new int[]{((AVM2SourceGenerator) generator).str(value)}, new byte[0])
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.STRING;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
