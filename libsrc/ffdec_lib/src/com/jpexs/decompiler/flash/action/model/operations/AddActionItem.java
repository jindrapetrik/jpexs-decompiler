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
 * License along with this library. */
package com.jpexs.decompiler.flash.action.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.swf5.ActionAdd2;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.EcmaType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

public class AddActionItem extends BinaryOpItem {

    boolean version2;

    public AddActionItem(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide, boolean version2) {
        super(instruction, PRECEDENCE_ADDITIVE, leftSide, rightSide, "+");
        this.version2 = version2;
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
        if (version2) {
            if (EcmaScript.type(leftSide.getResult()) == EcmaType.STRING || EcmaScript.type(rightSide.getResult()) == EcmaType.STRING) {
                return "" + leftSide.getResult() + rightSide.getResult();
            }
            return EcmaScript.toNumber(leftSide.getResult()) + EcmaScript.toNumber(rightSide.getResult());
        } else {
            return Action.toFloatPoint(leftSide.getResult()) + Action.toFloatPoint(rightSide.getResult());
        }
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, leftSide, rightSide, new ActionAdd2());
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.BOOLEAN;
    }
}
