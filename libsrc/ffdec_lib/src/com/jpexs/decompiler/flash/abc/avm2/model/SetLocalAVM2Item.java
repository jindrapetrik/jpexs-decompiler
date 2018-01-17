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
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.AssignmentAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SetLocalAVM2Item extends AVM2Item implements SetTypeAVM2Item, AssignmentAVM2Item {

    public int regIndex;

    public DeclarationAVM2Item declaration;

    @Override
    public DeclarationAVM2Item getDeclaration() {
        return declaration;
    }

    @Override
    public void setDeclaration(DeclarationAVM2Item declaration) {
        this.declaration = declaration;
    }

    public SetLocalAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, int regIndex, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGMENT, value);
        this.regIndex = regIndex;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        String localName = localRegName(localData.localRegNames, regIndex);
        getSrcData().localName = localName;
        writer.append(localName).append(" = ");
        if (declaration != null && !declaration.type.equals(TypeItem.UNBOUNDED) && (value instanceof ConvertAVM2Item)) {
            return value.value.toString(writer, localData);
        }
        return value.toString(writer, localData);
    }

    @Override
    public GraphTargetItem getObject() {
        return new LocalRegAVM2Item(getInstruction(), getLineStartIns(), regIndex, null);
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2Instruction ins;
        switch (regIndex) {
            case 0:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal0, null);
                break;
            case 1:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal1, null);
                break;
            case 2:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal2, null);
                break;
            case 3:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal3, null);
                break;
            default:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal, new int[]{regIndex});
                break;
        }
        return toSourceMerge(localData, generator, value,
                new AVM2Instruction(0, AVM2Instructions.Dup, null), ins);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2Instruction ins;
        switch (regIndex) {
            case 0:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal0, null);
                break;
            case 1:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal1, null);
                break;
            case 2:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal2, null);
                break;
            case 3:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal3, null);
                break;
            default:
                ins = new AVM2Instruction(0, AVM2Instructions.SetLocal, new int[]{regIndex});
                break;
        }
        return toSourceMerge(localData, generator, value, ins);
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
