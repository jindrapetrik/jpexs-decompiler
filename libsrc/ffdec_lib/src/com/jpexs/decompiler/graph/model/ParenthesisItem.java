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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.List;

/**
 * Parenthesis.
 *
 * @author JPEXS
 */
public class ParenthesisItem extends GraphTargetItem {

    /**
     * Constructor.
     * 
     * @param dialect Dialect
     * @param src Source
     * @param lineStartIns Line start instruction
     * @param value Value
     */
    public ParenthesisItem(GraphTargetDialect dialect, GraphSourceItem src, GraphSourceItem lineStartIns, GraphTargetItem value) {
        super(dialect, src, lineStartIns, PRECEDENCE_PRIMARY, value);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("(");
        appendNoParenthesis(writer, localData);
        return writer.append(")");
    }

    @Override
    public GraphTextWriter appendNoParenthesis(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        value.toString(writer, localData);
        return writer;
    }   
    

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return value.toSource(localData, generator);
    }

    @Override
    public boolean hasReturnValue() {
        return value.hasReturnValue();
    }

    @Override
    public GraphTargetItem returnType() {
        return value.returnType();
    }
}
