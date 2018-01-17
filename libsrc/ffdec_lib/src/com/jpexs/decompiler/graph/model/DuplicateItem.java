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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class DuplicateItem extends GraphTargetItem implements SimpleValue {

    public DuplicateItem(GraphSourceItem src, GraphSourceItem lineStartIns, GraphTargetItem value) {
        super(src, lineStartIns, value.getPrecedence(), value);
    }

    @Override
    public Object getResult() {
        return value.getResult();
    }

    @Override
    public Double getResultAsNumber() {
        return value.getResultAsNumber();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (((value instanceof SimpleValue) && (((SimpleValue) value).isSimpleValue())) || !Configuration.displayDupInstructions.get()) {
            return value.appendTry(writer, localData);
        }
        writer.append("§§dup(");
        value.appendTry(writer, localData);
        return writer.append(")");
    }

    @Override
    public GraphTargetItem getNotCoerced() {
        return value.getNotCoerced();
    }

    @Override
    public GraphTargetItem getNotCoercedNoDup() {
        return this;
    }

    @Override
    public GraphTargetItem getThroughRegister() {
        return value.getThroughRegister();
    }

    @Override
    public GraphTargetItem getThroughDuplicate() {
        return value.getThroughDuplicate();
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        dependencies.add(value);
        return value.isCompileTime(dependencies);
    }

    @Override
    public boolean isVariableComputed() {
        return value.isVariableComputed();
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public GraphTargetItem simplify(String implicitCoerce) {
        return this;
    }

    @Override
    public GraphTargetItem returnType() {
        return value.returnType();
    }

    /*@Override
     public GraphTargetItem invert(GraphSourceItem src) {
     return //new DuplicateItem(src, value instanceof NotItem ? (value.value) : new NotItem(src, value));
     }*/
    @Override
    public boolean isSimpleValue() {
        return ((value instanceof SimpleValue) && ((SimpleValue) value).isSimpleValue());
    }
}
