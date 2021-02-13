/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class GetPropertyActionItem extends ActionItem {

    public GraphTargetItem target;

    public int propertyIndex;

    public boolean useGetPropertyFunction = true;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(target);
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

        if (!useGetPropertyFunction) {
            target.appendToNoQuotes(writer, localData);
            writer.append(":");
            writer.append(Action.propertyNames[propertyIndex]);
            return writer;
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
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.target);
        hash = 79 * hash + this.propertyIndex;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GetPropertyActionItem other = (GetPropertyActionItem) obj;
        if (this.propertyIndex != other.propertyIndex) {
            return false;
        }
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        return true;
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
