/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphTargetDialect;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.EcmaType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.CompoundableBinaryOp;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 * Add two values.
 *
 * @author JPEXS
 */
public class AddAVM2Item extends BinaryOpItem implements CompoundableBinaryOp {

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param leftSide Left side
     * @param rightSide Right side
     */
    public AddAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(AVM2GraphTargetDialect.INSTANCE, instruction, lineStartIns, PRECEDENCE_ADDITIVE, leftSide, rightSide, "+", "", ""); //?
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (rightSide.getPrecedence() >= precedence) { //string + vs number +
            if (leftSide.getPrecedence() > precedence) {
                writer.append("(");
                leftSide.toString(writer, localData);
                writer.append(")");
            } else {
                leftSide.toString(writer, localData);
            }
            writer.append(" ");
            writer.append(operator);
            writer.append(" ");

            writer.append("(");
            rightSide.toString(writer, localData);
            return writer.append(")");
        } else {
            return super.appendTo(writer, localData);
        }
    }

    @Override
    public Object getResult() {
        Object leftResult = leftSide.getResult();
        Object rightResult = rightSide.getResult();
        leftResult = EcmaScript.toPrimitive(leftResult, "");
        rightResult = EcmaScript.toPrimitive(rightResult, "");
        if (EcmaScript.type(leftResult) == EcmaType.STRING || EcmaScript.type(rightResult) == EcmaType.STRING) {
            return EcmaScript.toString(leftResult) + EcmaScript.toString(rightResult);
        }
        return EcmaScript.toNumber(leftResult) + EcmaScript.toNumber(rightResult);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        /*if (rightSide instanceof IntegerValueAVM2Item) {
            IntegerValueAVM2Item iv = (IntegerValueAVM2Item) rightSide;
            if (iv.value == 1) {
                return toSourceMerge(localData, generator, leftSide,
                        new AVM2Instruction(0, AVM2Instructions.Increment, null)
                );
            }
        }*/        
        if (localData.numberContext != null) {
            return toSourceMerge(localData, generator, leftSide, rightSide,
                new AVM2Instruction(0, AVM2Instructions.AddP, new int[] {localData.numberContext})
            );
        }
        return toSourceMerge(localData, generator, leftSide, rightSide,
                new AVM2Instruction(0, AVM2Instructions.Add, null)
        );
    }

    @Override
    public GraphTargetItem returnType() {
        GraphTargetItem leftType = leftSide.returnType();
        GraphTargetItem rightType = rightSide.returnType();
        if (leftType.equals(TypeItem.STRING) || rightType.equals(TypeItem.STRING)) {
            return TypeItem.STRING;
        }
        if (leftType.equals(TypeItem.INT) && rightType.equals(TypeItem.INT)) {
            return TypeItem.INT;
        }

        if ((leftType.equals(TypeItem.INT) && rightType.equals(TypeItem.UINT))
                || (leftType.equals(TypeItem.UINT) && rightType.equals(TypeItem.INT))) {
            return TypeItem.INT;
        }

        if (leftType.equals(TypeItem.UINT) && rightType.equals(TypeItem.UINT)) {
            return TypeItem.UINT;
        }

        if (leftType.equals(TypeItem.NUMBER) || rightType.equals(TypeItem.NUMBER)) {
            return TypeItem.NUMBER;
        }
        return TypeItem.NUMBER;
    }

    @Override
    public List<GraphSourceItem> getOperatorInstruction() {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(new AVM2Instruction(0, AVM2Instructions.Add, null));
        return ret;
    }

}
