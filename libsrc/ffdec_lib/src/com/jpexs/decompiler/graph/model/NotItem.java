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
import com.jpexs.decompiler.flash.action.model.operations.Inverted;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class NotItem extends UnaryOpItem implements LogicalOpItem, Inverted {

    public NotItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_UNARY, value, "!", "Boolean");
    }

    @Override
    public Object getResult() {
        return !value.getResultAsBoolean();
    }

    public static Boolean getResult(Object obj) {
        boolean ret = EcmaScript.toBoolean(obj);
        if (ret) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        dependencies.add(value);
        return value.isCompileTime(dependencies);
    }

    public GraphTargetItem getOriginal() {
        return value;
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
    public GraphTargetItem returnType() {
        return TypeItem.BOOLEAN;
    }

    @Override
    public GraphTextWriter toStringBoolean(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        //Skip explicit conversion to boolean, it is not needed, it is done implicitly
        if (value instanceof NotItem) {
            return value.value.toStringBoolean(writer, localData);
        }
        return super.toStringBoolean(writer, localData);
    }

    @Override
    public GraphTargetItem invert(GraphSourceItem src) {
        //if this is already !!val, convert to !val
        if (value instanceof NotItem) {
            return value;
        }
        //If it is not a boolean, put !! there for toBoolean conversion
        if (!TypeItem.BOOLEAN.equals(value.returnType()) && !(value instanceof DuplicateItem)) {
            return new NotItem(null, null, this);
        }

        //can be inverted
        return value;
    }
}
