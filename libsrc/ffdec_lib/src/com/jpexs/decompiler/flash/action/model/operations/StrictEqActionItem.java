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
package com.jpexs.decompiler.flash.action.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.EqualsTypeItem;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class StrictEqActionItem extends BinaryOpItem implements LogicalOpItem, Inverted, EqualsTypeItem {

    public StrictEqActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, lineStartIns, PRECEDENCE_EQUALITY, leftSide, rightSide, "===", "", "");
    }

    @Override
    public Object getResult() {
        return getResult(rightSide.getResult(), leftSide.getResult());
    }

    public static boolean getResult(Object rightResult, Object leftResult) {
        Object x = leftResult;
        Object y = rightResult;
        return EcmaScript.strictEquals(true, x, y);
    }

    @Override
    public GraphTargetItem invert(GraphSourceItem negSrc) {
        return new StrictNeqActionItem(getSrc(), getLineStartItem(), leftSide, rightSide);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, leftSide, rightSide, new ActionStrictEquals());
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.BOOLEAN;
    }
}
