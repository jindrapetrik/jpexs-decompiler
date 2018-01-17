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
import com.jpexs.decompiler.flash.action.swf4.ActionGetURL2;
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
public class LoadVariablesNumActionItem extends ActionItem {

    private final GraphTargetItem urlString;

    private final GraphTargetItem num;

    private final int method;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(urlString);
        ret.add(num);
        return ret;
    }

    public LoadVariablesNumActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem urlString, GraphTargetItem num, int method) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.num = num;
        this.method = method;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        String methodStr = "";
        if (method == 1) {
            methodStr = ",\"GET\"";
        }
        if (method == 2) {
            methodStr = ",\"POST\"";
        }
        writer.append("loadVariablesNum");
        writer.spaceBeforeCallParenthesies(2);
        writer.append("(");
        urlString.toString(writer, localData);
        writer.append(",");
        num.toString(writer, localData);
        return writer.append(methodStr).append(")");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        Object lev;
        if ((num instanceof DirectValueActionItem) && (((DirectValueActionItem) num).value instanceof Long)) {
            lev = asGenerator.pushConstTargetItem("_level" + ((DirectValueActionItem) num).value);
        } else {
            lev = new AddActionItem(getSrc(), getLineStartItem(), asGenerator.pushConstTargetItem("_level"), num, true);
        }
        return toSourceMerge(localData, generator, urlString, lev, new ActionGetURL2(method, true, false));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
