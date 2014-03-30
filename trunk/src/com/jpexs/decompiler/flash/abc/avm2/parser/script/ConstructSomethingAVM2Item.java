/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructPropIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ConstructSomethingAVM2Item extends CallAVM2Item {

    public ConstructSomethingAVM2Item(GraphTargetItem name, List<GraphTargetItem> arguments) {
        super(name, arguments);
    }

    @Override
    public GraphTargetItem returnType() {
        return name.returnType();
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {

        if (name instanceof TypeItem) {
            TypeItem prop = (TypeItem) name;
            int type_index = ((AVM2SourceGenerator) generator).resolveType(name.toString());
            return toSourceMerge(localData, generator,
                    new AVM2Instruction(0, new FindPropertyStrictIns(), new int[]{type_index, arguments.size()}, new byte[0]), arguments,
                    new AVM2Instruction(0, new ConstructPropIns(), new int[]{type_index, arguments.size()}, new byte[0])
            );
        }
        if (name instanceof NameAVM2Item) {
            //TODO
        }
        return new ArrayList<>();
    }

}
