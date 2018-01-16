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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CommaExpressionItem extends GraphTargetItem {

    public List<GraphTargetItem> commands;

    public CommaExpressionItem(GraphSourceItem src, GraphSourceItem lineStartIns, List<GraphTargetItem> commands) {
        super(src, lineStartIns, PRECEDENCE_PRIMARY);
        this.commands = commands;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean first = true;
        for (GraphTargetItem t : commands) {
            if (!first) {
                writer.append(", ");
            }
            t.toString(writer, localData);
            first = false;
        }
        return writer;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return commands.isEmpty() ? TypeItem.UNBOUNDED : commands.get(commands.size() - 1).returnType();
    }
}
