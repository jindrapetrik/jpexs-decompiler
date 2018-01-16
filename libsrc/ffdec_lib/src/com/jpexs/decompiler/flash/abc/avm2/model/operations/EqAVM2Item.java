/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.EqualsTypeItem;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class EqAVM2Item extends BinaryOpItem implements LogicalOpItem, IfCondition, EqualsTypeItem {

    public EqAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, lineStartIns, PRECEDENCE_EQUALITY, leftSide, rightSide, "==", "", "");
    }

    @Override
    public int getIfDefinition() {
        return AVM2Instructions.IfEq;
    }

    @Override
    public int getIfNotDefinition() {
        return AVM2Instructions.IfNe;
    }

    @Override
    public Object getResult() {
        return EcmaScript.equals(leftSide.getResult(), rightSide.getResult());
    }

    @Override
    public GraphTargetItem invert(GraphSourceItem neqSrc) {
        return new NeqAVM2Item(getSrc(), getLineStartItem(), leftSide, rightSide);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, leftSide, rightSide,
                new AVM2Instruction(0, AVM2Instructions.Equals, null)
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(DottedChain.BOOLEAN);
    }
}
