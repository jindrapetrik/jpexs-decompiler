/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Pop item from the stack.
 *
 * @author JPEXS
 */
public class PopItem extends GraphTargetItem {

    private static final AtomicLong NEXT_ORDER = new AtomicLong();

    /**
     * Creation order: a translation pops the top of the stack first.
     */
    public final long order = NEXT_ORDER.getAndIncrement();

    /**
     * The pushed value this pop reads, once the decompiler resolved it, or
     * null.
     */
    public GraphTargetItem resolvedValue;

    /**
     * Constructor.
     * 
     * @param dialect Dialect
     * @param src Source
     * @param lineStartIns Line start instruction
     */
    public PopItem(GraphTargetDialect dialect, GraphSourceItem src, GraphSourceItem lineStartIns) {
        super(dialect, src, lineStartIns, PRECEDENCE_PRIMARY);
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        if (resolvedValue != null) {
            visitor.visit(resolvedValue);
        }
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (resolvedValue != null) {
            if (resolvedValue.getPrecedence() > PRECEDENCE_PRIMARY) {
                writer.append("(");
                resolvedValue.toString(writer, localData);
                return writer.append(")");
            }
            return resolvedValue.toString(writer, localData);
        }
        //Logger.getLogger(PopItem.class.getName()).log(Level.WARNING, "Pop item left in the source code");
        writer.append("§§pop()");
        return writer;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        if (resolvedValue != null) {
            return resolvedValue.returnType();
        }
        return TypeItem.UNBOUNDED;
    }

    @Override
    public Object getResult() {
        if (resolvedValue != null) {
            return resolvedValue.getResult();
        }
        return Null.INSTANCE;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (resolvedValue != null) {
            return resolvedValue.toSource(localData, generator);
        }
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasSideEffect() {
        if (resolvedValue != null) {
            return resolvedValue.hasSideEffect();
        }
        return true; //?
    }
}
