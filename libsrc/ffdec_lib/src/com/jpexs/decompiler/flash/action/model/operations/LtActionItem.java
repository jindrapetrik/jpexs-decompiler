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
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphTargetDialect;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionLess;
import com.jpexs.decompiler.flash.action.swf5.ActionLess2;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Less than.
 *
 * @author JPEXS
 */
public class LtActionItem extends BinaryOpItem implements LogicalOpItem {

    /**
     * Version 2 flag
     */
    boolean version2;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param leftSide Left side
     * @param rightSide Right side
     * @param version2 Version 2 flag
     */
    public LtActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem leftSide, GraphTargetItem rightSide, boolean version2) {
        super(ActionGraphTargetDialect.INSTANCE, instruction, lineStartIns, PRECEDENCE_RELATIONAL, leftSide, rightSide, "<", "", "");
        this.version2 = version2;
    }

    @Override
    public Object getResult() {
        return getResult(rightSide.getResult(), leftSide.getResult(), version2);
    }

    /**
     * Gets result.
     *
     * @param rightResult Right result
     * @param leftResult Left result
     * @param version2 Version 2 flag
     * @return Result
     */
    public static Object getResult(Object rightResult, Object leftResult, boolean version2) {
        if (version2) {
            Object ret = EcmaScript.compare(leftResult, rightResult, true);
            if (ret == Undefined.INSTANCE) {
                return ret;
            }

            int reti = (int) ret;
            return reti == -1;
        } else {
            //For SWF 4 and older, it should return 1 or 0
            return Action.toFloatPoint(leftResult) < Action.toFloatPoint(rightResult);
        }
    }

    @Override
    public GraphTargetItem invert(GraphSourceItem neqSrc) {
        return new GeActionItem(getSrc(), getLineStartItem(), leftSide, rightSide, version2);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator g = (ActionSourceGenerator) generator;
        return toSourceMerge(localData, generator, leftSide, rightSide, g.getSwfVersion() >= 5 ? new ActionLess2() : new ActionLess());
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.BOOLEAN;
    }

    @Override
    public List<GraphSourceItem> getOperatorInstruction() {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(new ActionLess2()); //FIXME!!!
        return ret;
    }
}
