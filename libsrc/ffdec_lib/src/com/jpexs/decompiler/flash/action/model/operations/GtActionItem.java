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
package com.jpexs.decompiler.flash.action.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.ActionGraphTargetDialect;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionLess;
import com.jpexs.decompiler.flash.action.swf5.ActionLess2;
import com.jpexs.decompiler.flash.action.swf6.ActionGreater;
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
 * Greater.
 *
 * @author JPEXS
 */
public class GtActionItem extends BinaryOpItem implements LogicalOpItem {

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param leftSide Left side
     * @param rightSide Right side
     */
    public GtActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(ActionGraphTargetDialect.INSTANCE, instruction, lineStartIns, PRECEDENCE_RELATIONAL, leftSide, rightSide, ">", "", "");
    }

    @Override
    public Object getResult() {
        return getResult(rightSide.getResult(), leftSide.getResult());
    }

    /**
     * Gets result.
     * @param rightResult Right result
     * @param leftResult Left result
     * @return Result
     */
    public static Object getResult(Object rightResult, Object leftResult) {
        Object ret = EcmaScript.compare(leftResult, rightResult, true);
        if (ret == Undefined.INSTANCE) {
            return ret;
        }

        int reti = (int) ret;
        return reti == 1;
    }

    @Override
    public GraphTargetItem invert(GraphSourceItem neqSrc) {
        return new LeActionItem(getSrc(), getLineStartItem(), leftSide, rightSide);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator g = (ActionSourceGenerator) generator;
        if (g.getSwfVersion() >= 6) {
            return toSourceMerge(localData, generator, leftSide, rightSide, new ActionGreater());
        }
        return toSourceMerge(localData, generator, rightSide, leftSide, g.getSwfVersion() >= 5 ? new ActionLess2() : new ActionLess());
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.BOOLEAN;
    }

    @Override
    public List<GraphSourceItem> getOperatorInstruction() {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(new ActionGreater()); //FIXME!!!
        return ret;
    }
}
