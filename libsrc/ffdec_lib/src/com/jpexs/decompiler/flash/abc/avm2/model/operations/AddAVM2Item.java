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
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.EcmaType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AddAVM2Item extends BinaryOpItem {

    public AddAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, lineStartIns, PRECEDENCE_ADDITIVE, leftSide, rightSide, "+", "", ""); //?
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
        if (rightSide instanceof IntegerValueAVM2Item) {
            IntegerValueAVM2Item iv = (IntegerValueAVM2Item) rightSide;
            if (iv.value == 1) {
                return toSourceMerge(localData, generator, leftSide,
                        new AVM2Instruction(0, AVM2Instructions.Increment, null)
                );
            }
        }
        return toSourceMerge(localData, generator, leftSide, rightSide,
                new AVM2Instruction(0, AVM2Instructions.Add, null)
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new UnboundedTypeItem();
    }
}
