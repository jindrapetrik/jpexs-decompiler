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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf5.ActionIncrement;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IncrementActionItem extends ActionItem {

    public GraphTargetItem object;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(object);
        return ret;
    }

    public IncrementActionItem(GraphSourceItem instruction, GraphTargetItem object) {
        super(instruction, PRECEDENCE_ADDITIVE);
        this.object = object;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        object.toString(writer, localData);
        return writer.append(" + 1");
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(object)) {
            return false;
        }
        dependencies.add(object);
        return object.isCompileTime(dependencies);
    }

    @Override
    public Object getResult() {
        return EcmaScript.toNumber(object.getResult()) + 1;
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(object.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, object, new ActionIncrement());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
