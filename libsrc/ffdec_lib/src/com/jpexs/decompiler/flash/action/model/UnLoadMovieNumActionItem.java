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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.model.operations.AddActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf3.ActionGetURL;
import com.jpexs.decompiler.flash.action.swf4.ActionGetURL2;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class UnLoadMovieNumActionItem extends ActionItem {

    private final GraphTargetItem num;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(num);
        return ret;
    }

    public UnLoadMovieNumActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem num) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.num = num;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("unloadMovieNum");
        writer.spaceBeforeCallParenthesies(1);
        writer.append("(");
        num.toString(writer, localData);
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        if ((num instanceof DirectValueActionItem) && (((DirectValueActionItem) num).value instanceof Long)) {
            return toSourceMerge(localData, generator, new ActionGetURL("", "_level" + ((DirectValueActionItem) num).value));
        } else {
            return toSourceMerge(localData, generator, new ActionPush(""), new AddActionItem(getSrc(), getLineStartItem(), asGenerator.pushConstTargetItem("_level"), num, true), new ActionGetURL2(0, false, true));
        }

    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
