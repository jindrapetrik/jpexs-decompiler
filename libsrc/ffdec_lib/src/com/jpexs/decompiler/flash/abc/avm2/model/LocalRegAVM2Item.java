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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal3Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.FilterAVM2Item;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Set;

public class LocalRegAVM2Item extends AVM2Item {

    public int regIndex;
    public GraphTargetItem computedValue;
    private final Object computedResult;
    private boolean isCT = false;

    public LocalRegAVM2Item(AVM2Instruction instruction, int regIndex, GraphTargetItem computedValue) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.regIndex = regIndex;
        if (computedValue == null) {
            computedResult = null;
        } else {
            if (computedValue.isCompileTime()) {
                computedResult = computedValue.getResult();
                isCT = true;
            } else {
                computedResult = null;
            }
        }
        this.computedValue = computedValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (computedValue instanceof FilterAVM2Item) {
            return computedValue.toString(writer, localData);
        }
        String localName = localRegName(localData.localRegNames, regIndex);
        srcData.put("localName", localName);
        return writer.append(localName);
    }

    @Override
    public GraphTargetItem getThroughRegister() {
        if (computedValue == null) {
            return this;
        }
        return computedValue.getThroughRegister();
    }

    @Override
    public Object getResult() {
        if (computedResult == null) {
            return new Undefined();
        }
        return computedResult;

    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return isCT;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2Instruction ins;
        switch (regIndex) {
            case 0:
                ins = new AVM2Instruction(0, new GetLocal0Ins(), null);
                break;
            case 1:
                ins = new AVM2Instruction(0, new GetLocal1Ins(), null);
                break;
            case 2:
                ins = new AVM2Instruction(0, new GetLocal2Ins(), null);
                break;
            case 3:
                ins = new AVM2Instruction(0, new GetLocal3Ins(), null);
                break;
            default:
                ins = new AVM2Instruction(0, new GetLocalIns(), new int[]{regIndex});
                break;
        }
        return toSourceMerge(localData, generator, ins);
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
