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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.swf4.ActionGetProperty;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GetPropertyActionItem extends ActionItem {

    public GraphTargetItem target;

    public int propertyIndex;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(target);
        return ret;
    }

    public GetPropertyActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem target, int propertyIndex) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.target = target;
        this.propertyIndex = propertyIndex;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (isEmptyString(target)) {
            return writer.append(Action.propertyNames[propertyIndex]);
        }

        if ((target instanceof DirectValueActionItem) && ((DirectValueActionItem) target).isString()) {
            target.toStringNoQuotes(writer, localData);
            return writer.append(":" + Action.propertyNames[propertyIndex]);
        }

        writer.append("getProperty");
        writer.spaceBeforeCallParenthesies(2);
        writer.append("(");
        target.appendTo(writer, localData);
        writer.append(", ");
        writer.append(Action.propertyNames[propertyIndex]);
        writer.append(")");
        return writer;
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(target.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, target, new ActionPush((Long) (long) propertyIndex), new ActionGetProperty());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
