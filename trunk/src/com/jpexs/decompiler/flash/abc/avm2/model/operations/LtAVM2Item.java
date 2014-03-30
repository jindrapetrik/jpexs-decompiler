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
package com.jpexs.decompiler.flash.abc.avm2.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.LessThanIns;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import java.util.List;

public class LtAVM2Item extends BinaryOpItem implements LogicalOpItem {

    public LtAVM2Item(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, PRECEDENCE_RELATIONAL, leftSide, rightSide, "<");
    }

    @Override
    public GraphTargetItem invert() {
        return new GeAVM2Item(src, leftSide, rightSide);
    }

    @Override
    public Object getResult() {
        return EcmaScript.compare(leftSide.getResult(), rightSide.getResult());
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, leftSide, rightSide,
                new AVM2Instruction(0, new LessThanIns(), new int[]{}, new byte[0])
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("Boolean");
    }
}
