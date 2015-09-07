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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import java.util.List;

public class GeAVM2Item extends BinaryOpItem implements LogicalOpItem, IfCondition {

    public GeAVM2Item(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, PRECEDENCE_RELATIONAL, leftSide, rightSide, ">=");
    }

    @Override
    public int getIfDefinition() {
        return AVM2Instructions.IfGe;
    }

    @Override
    public int getIfNotDefinition() {
        return AVM2Instructions.IfNGe;
    }

    @Override
    public GraphTargetItem invert(GraphSourceItem neqSrc) {
        return new LtAVM2Item(getSrc(), leftSide, rightSide);
    }

    @Override
    public Object getResult() {
        Object ret = EcmaScript.compare(leftSide.getResult(), rightSide.getResult());
        if (ret == Boolean.TRUE) {
            return Boolean.FALSE;
        }
        if (ret == Boolean.FALSE) {
            return Boolean.TRUE;
        }
        return ret;//undefined
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, leftSide, rightSide,
                new AVM2Instruction(0, AVM2Instructions.GreaterEquals, null)
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(DottedChain.BOOLEAN);
    }
}
