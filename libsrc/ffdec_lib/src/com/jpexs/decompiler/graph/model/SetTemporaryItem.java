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
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Set temporary item.
 *
 * @author JPEXS
 */
public class SetTemporaryItem extends GraphTargetItem implements SimpleValue, HasTempIndex {
    
    private static final boolean SHOW_SUFFIX = false;

    public int tempIndex;
    public boolean declaration = false;
    public int refCount = 0;
    
    private final String suffix;

    public String getSuffix() {
        return suffix;
    }   
    
    /**
     * Constructor.
     * 
     * @param dialect Dialect
     * @param src Source
     * @param lineStartIns Line start item
     * @param value Value
     */
    public SetTemporaryItem(GraphTargetDialect dialect, GraphSourceItem src, GraphSourceItem lineStartIns, GraphTargetItem value, int tempIndex, String suffix, int refCount) {
        super(dialect, src, lineStartIns, PRECEDENCE_ASSIGNMENT, value);
        this.tempIndex = tempIndex;
        this.suffix = suffix;
        this.refCount = refCount;
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
        GraphTargetItem val = value;
        while ((val instanceof HasTempIndex) && ((HasTempIndex) val).getTempIndex() == tempIndex) {
            val = val.value;
        }
        if (declaration) {
            return dialect.writeTemporaryDeclaration(writer, localData, SHOW_SUFFIX ? suffix : "", tempIndex, val);
        }
        writer.append("_temp");        
        if (SHOW_SUFFIX) {
            writer.append(suffix);
        }
        writer.append("_").append(tempIndex).append(" = ");
        return value.appendTry(writer, localData);
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
        if (!((value instanceof SimpleValue) && ((SimpleValue) value).isSimpleValue())) {
            dependencies.add(value);
        }
        return value.isCompileTime(dependencies);
    }

    @Override
    public boolean isVariableComputed() {
        return value.isVariableComputed();
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return new ArrayList<>();
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

    @Override
    public boolean isSimpleValue() {
        return ((value instanceof SimpleValue) && ((SimpleValue) value).isSimpleValue());
    }

    @Override
    public boolean hasSideEffect() {
        return value.hasSideEffect();
    }

    @Override
    public int getTempIndex() {
        return tempIndex;
    }
}
