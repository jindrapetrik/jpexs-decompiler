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
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.AddIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIns;
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

public class AddAVM2Item extends BinaryOpItem {

    public AddAVM2Item(AVM2Instruction instruction, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, PRECEDENCE_ADDITIVE, leftSide, rightSide, "+");
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
        if (EcmaScript.type(leftSide.getResult()) == EcmaType.STRING || EcmaScript.type(rightSide.getResult()) == EcmaType.STRING) {
            return "" + leftSide.getResult() + rightSide.getResult();
        }
        return EcmaScript.toNumber(leftSide.getResult()) + EcmaScript.toNumber(rightSide.getResult());
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (rightSide instanceof IntegerValueAVM2Item) {
            IntegerValueAVM2Item iv = (IntegerValueAVM2Item) rightSide;
            if (iv.value == 1) {
                return toSourceMerge(localData, generator, leftSide,
                        new AVM2Instruction(0, new IncrementIns(), new int[]{})
                );
            }
        }
        return toSourceMerge(localData, generator, leftSide, rightSide,
                new AVM2Instruction(0, new AddIns(), new int[]{})
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new UnboundedTypeItem();
    }

}
