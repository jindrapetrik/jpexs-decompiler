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
package com.jpexs.decompiler.flash.action.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.ActionGraphTargetDialect;
import com.jpexs.decompiler.flash.action.model.CompoundableBinaryOpAs12;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionSubtract;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 * Subtract.
 *
 * @author JPEXS
 */
public class SubtractActionItem extends BinaryOpItem implements CompoundableBinaryOpAs12 {

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param leftSide Left side
     * @param rightSide Right side
     */
    public SubtractActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(ActionGraphTargetDialect.INSTANCE, instruction, lineStartIns, PRECEDENCE_ADDITIVE, leftSide, rightSide, "-", "Number", "Number");
    }

    @Override
    public Object getResult() {
        return getResult(rightSide.getResultAsNumber(), leftSide.getResultAsNumber());
    }

    /**
     * Gets result.
     *
     * @param rightResult Right result
     * @param leftResult Left result
     * @return Result
     */
    public static Double getResult(Double rightResult, Double leftResult) {
        return leftResult - rightResult;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if ((leftSide instanceof DirectValueActionItem)
                && (((((DirectValueActionItem) leftSide).value instanceof Float) && (((Float) ((DirectValueActionItem) leftSide).value) == 0f))
                || ((((DirectValueActionItem) leftSide).value instanceof Double) && (((Double) ((DirectValueActionItem) leftSide).value) == 0.0))
                || ((((DirectValueActionItem) leftSide).value instanceof Long) && (((Long) ((DirectValueActionItem) leftSide).value) == 0L))
                || ((DirectValueActionItem) leftSide).isString() && "0".equals(((DirectValueActionItem) leftSide).toString()))) {
            writer.append(operator);
            writer.append(" ");

            int rightPrecedence = rightSide.getPrecedence();
            if (rightPrecedence >= precedence && rightPrecedence != GraphTargetItem.NOPRECEDENCE) {
                writer.append("(");
                rightSide.toString(writer, localData, coerceRight);
                writer.append(")");
            } else {
                rightSide.toString(writer, localData, coerceRight);
            }
            return writer;
        } else if (rightSide.getPrecedence() >= precedence) { // >=  add or subtract too

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
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, leftSide, rightSide, new ActionSubtract());
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public List<GraphSourceItem> getOperatorInstruction() {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(new ActionSubtract());
        return ret;
    }
}
