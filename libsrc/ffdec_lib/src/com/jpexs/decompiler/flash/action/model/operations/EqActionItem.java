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
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.EqualsTypeItem;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Equality.
 *
 * @author JPEXS
 */
public class EqActionItem extends BinaryOpItem implements LogicalOpItem, EqualsTypeItem {

    /**
     * Version 2 flag.
     */
    boolean version2;

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param leftSide Left side
     * @param rightSide Right side
     * @param version2 Version 2 flag
     */
    public EqActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem leftSide, GraphTargetItem rightSide, boolean version2) {
        super(ActionGraphTargetDialect.INSTANCE, instruction, lineStartIns, PRECEDENCE_EQUALITY, leftSide, rightSide, "==", "", "");
        this.version2 = version2;
    }

    @Override
    public Object getResult() {
        return getResult(rightSide.getResult(), leftSide.getResult(), version2);
    }

    /**
     * Gets result.
     * @param rightResult Right result
     * @param leftResult Left result
     * @param version2 Version 2 flag
     * @return Result
     */
    public static Boolean getResult(Object rightResult, Object leftResult, boolean version2) {
        if (version2) {
            return EcmaScript.equals(true, leftResult, rightResult);
        } else {
            //For SWF 4 and older, it should return 1 or 0
            return (Action.toFloatPoint(leftResult) == Action.toFloatPoint(rightResult));
        }
    }

    @Override
    public GraphTargetItem invert(GraphSourceItem neqSrc) {
        return new NeqActionItem(getSrc(), getLineStartItem(), leftSide, rightSide, version2);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, leftSide, rightSide, new ActionEquals2());
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.BOOLEAN;
    }

    @Override
    public List<GraphSourceItem> getOperatorInstruction() {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(new ActionEquals2());
        return ret;
    }
}
