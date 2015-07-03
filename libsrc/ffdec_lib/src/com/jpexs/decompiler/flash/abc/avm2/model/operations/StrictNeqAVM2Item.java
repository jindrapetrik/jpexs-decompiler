/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.StrictEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfStrictEqIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfStrictNeIns;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import java.util.List;

public class StrictNeqAVM2Item extends BinaryOpItem implements LogicalOpItem, IfCondition {

    public StrictNeqAVM2Item(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, PRECEDENCE_EQUALITY, leftSide, rightSide, "!==");
    }

    @Override
    public InstructionDefinition getIfDefinition() {
        return new IfStrictNeIns();
    }

    @Override
    public InstructionDefinition getIfNotDefinition() {
        return new IfStrictEqIns();
    }

    @Override
    public GraphTargetItem invert(GraphSourceItem neqSrc) {
        return new StrictEqAVM2Item(src, leftSide, rightSide);
    }

    @Override
    public Object getResult() {
        Object x = leftSide.getResult();
        Object y = rightSide.getResult();
        return EcmaScript.type(x) != EcmaScript.type(y)
                || (!EcmaScript.equals(x, y));
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, leftSide, rightSide,
                new AVM2Instruction(0, new StrictEqualsIns(), null),
                new AVM2Instruction(0, new NotIns(), null)
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("Boolean");
    }
}
