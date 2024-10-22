/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.AssignmentAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 * Set local register value.
 *
 * @author JPEXS
 */
public class SetLocalAVM2Item extends AVM2Item implements SetTypeAVM2Item, AssignmentAVM2Item {

    /**
     * Register index
     */
    public int regIndex;

    /**
     * Declaration
     */
    public DeclarationAVM2Item declaration;

    /**
     * Compound value
     */
    public GraphTargetItem compoundValue;

    /**
     * Compound operator
     */
    public String compoundOperator;

    /**
     * Type
     */
    public GraphTargetItem type;

    /**
     * Hide value
     */
    public boolean hideValue = false;

    /**
     * Caused by duplicate
     */
    public boolean causedByDup = false;
    
    /**
     * Directly caused by duplicate
     */
    public boolean directlyCausedByDup = false;

    @Override
    public DeclarationAVM2Item getDeclaration() {
        return declaration;
    }

    @Override
    public void setDeclaration(DeclarationAVM2Item declaration) {
        this.declaration = declaration;
    }

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param regIndex Register index
     * @param value Value
     * @param type Type
     */
    public SetLocalAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, int regIndex, GraphTargetItem value, GraphTargetItem type) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGNMENT, value);
        this.regIndex = regIndex;
        this.type = type;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        String localName = localRegName(localData.localRegNames, regIndex);
        getSrcData().localName = localName;
        writer.append(localName);
        if (hideValue) {
            return writer;
        }
        if (compoundOperator != null) {
            writer.append(" ");
            writer.append(compoundOperator);
            writer.append("= ");
            return compoundValue.toString(writer, localData);
        }        
        writer.append(" = ");
        /*if (declaration != null && !declaration.type.equals(TypeItem.UNBOUNDED) && (value instanceof ConvertAVM2Item)) {
            return value.value.toString(writer, localData);
        }*/
        return SetTypeIns.handleNumberToInt(value, type).toString(writer, localData);
    }

    @Override
    public GraphTargetItem getObject() {
        return new LocalRegAVM2Item(getInstruction(), getLineStartIns(), regIndex, null, value.returnType());
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
        return type;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.regIndex;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SetLocalAVM2Item other = (SetLocalAVM2Item) obj;
        if (this.regIndex != other.regIndex) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public GraphTargetItem getCompoundValue() {
        return compoundValue;
    }

    @Override
    public void setCompoundValue(GraphTargetItem value) {
        this.compoundValue = value;
    }

    @Override
    public void setCompoundOperator(String operator) {
        compoundOperator = operator;
    }

    @Override
    public String getCompoundOperator() {
        return compoundOperator;
    }

    @Override
    public int getPrecedence() {
        if (hideValue) {
            return PRECEDENCE_PRIMARY;
        }
        return precedence;
    }

}
