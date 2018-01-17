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
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionLess;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
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
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class LeActionItem extends BinaryOpItem implements LogicalOpItem, Inverted {

    public LeActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, lineStartIns, PRECEDENCE_RELATIONAL, leftSide, rightSide, "<=", "", "");
    }

    @Override
    public Object getResult() {
        Object ret = EcmaScript.compare(leftSide.getResult(), rightSide.getResult(), true);
        if (ret == Undefined.INSTANCE) {
            return ret;
        }

        int reti = (int) ret;
        return reti != 1;
    }

    @Override
    public GraphTargetItem invert(GraphSourceItem negSrc) {
        return new GtActionItem(getSrc(), getLineStartItem(), leftSide, rightSide);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator g = (ActionSourceGenerator) generator;
        if (g.getSwfVersion() >= 6) {
            return toSourceMerge(localData, generator, leftSide, rightSide, new ActionGreater(), new ActionNot());
        }
        return toSourceMerge(localData, generator, rightSide, leftSide, g.getSwfVersion() >= 5 ? new ActionLess2() : new ActionLess(), new ActionNot());
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.BOOLEAN;
    }
}
