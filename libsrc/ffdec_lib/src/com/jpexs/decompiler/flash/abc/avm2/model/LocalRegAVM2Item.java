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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.FilterAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class LocalRegAVM2Item extends AVM2Item {

    public final int regIndex;

    public GraphTargetItem computedValue;

    private final Object computedResult;

    private boolean isCT = false;

    public LocalRegAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, int regIndex, GraphTargetItem computedValue) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.regIndex = regIndex;
        if (computedValue == null) {
            computedResult = null;
        } else if (computedValue.isCompileTime()) {
            computedResult = computedValue.getResult();
            isCT = true;
        } else {
            computedResult = null;
        }
        this.computedValue = computedValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (computedValue instanceof FilterAVM2Item) {
            return computedValue.toString(writer, localData);
        }

        String localName = localRegName(localData.localRegNames, regIndex);
        getSrcData().localName = localName;
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
        if (computedValue == null) {
            return null;
        }
        return computedValue.getResult();
    }

    @Override
    public Double getResultAsNumber() {
        if (computedValue == null) {
            return null;
        }
        return computedValue.getResultAsNumber();
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return (computedValue instanceof UndefinedAVM2Item);
    }

    @Override
    public boolean isConvertedCompileTime(Set<GraphTargetItem> dependencies) {
        if (computedValue == null) {
            return false;
        }
        return ((computedValue instanceof ThisAVM2Item) && computedValue.isConvertedCompileTime(dependencies));
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2Instruction ins;
        switch (regIndex) {
            case 0:
                ins = new AVM2Instruction(0, AVM2Instructions.GetLocal0, null);
                break;
            case 1:
                ins = new AVM2Instruction(0, AVM2Instructions.GetLocal1, null);
                break;
            case 2:
                ins = new AVM2Instruction(0, AVM2Instructions.GetLocal2, null);
                break;
            case 3:
                ins = new AVM2Instruction(0, AVM2Instructions.GetLocal3, null);
                break;
            default:
                ins = new AVM2Instruction(0, AVM2Instructions.GetLocal, new int[]{regIndex});
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.regIndex;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LocalRegAVM2Item other = (LocalRegAVM2Item) obj;
        if (this.regIndex != other.regIndex) {
            return false;
        }
        return true;
    }
}
