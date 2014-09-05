/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.LShiftIns;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.List;

public class LShiftAVM2Item extends BinaryOpItem {

    public LShiftAVM2Item(AVM2Instruction instruction, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, PRECEDENCE_BITWISESHIFT, leftSide, rightSide, "<<");
    }

    @Override
    public Object getResult() {
        return ((int) (double) EcmaScript.toNumber(leftSide.getResult())) << ((int) (double) EcmaScript.toNumber(rightSide.getResult()));
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, leftSide, rightSide,
                new AVM2Instruction(0, new LShiftIns(), new int[]{})
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new UnboundedTypeItem();
    }

}
