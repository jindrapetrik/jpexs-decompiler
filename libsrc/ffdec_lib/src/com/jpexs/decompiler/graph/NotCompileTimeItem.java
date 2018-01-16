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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class NotCompileTimeItem extends GraphTargetItem {

    public GraphTargetItem object;

    public NotCompileTimeItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object) {
        super(instruction, lineStartIns, NOPRECEDENCE);
        this.object = object;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return false;
    }

    @Override
    public GraphTargetItem getThroughNotCompilable() {
        if (object == null) {
            return object;
        }
        return object.getThroughNotCompilable();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return object.toString(writer, localData);
    }

    @Override
    public boolean hasReturnValue() {
        return object.hasReturnValue();
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }
}
