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
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class TypeFunctionItem extends GraphTargetItem {

    public static TypeFunctionItem BOOLEAN = new TypeFunctionItem("Boolean");

    public static TypeFunctionItem STRING = new TypeFunctionItem("String");

    public static TypeFunctionItem ARRAY = new TypeFunctionItem("Array");

    public static UnboundedTypeItem UNBOUNDED = new UnboundedTypeItem();

    public String fullTypeName;

    public TypeFunctionItem(String fullTypeName) {
        super(null, null, NOPRECEDENCE);
        this.fullTypeName = fullTypeName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(fullTypeName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeFunctionItem other = (TypeFunctionItem) obj;
        return Objects.equals(fullTypeName, other.fullTypeName);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append(fullTypeName);
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return this;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public String toString() {
        return "Function[" + fullTypeName + "]";
    }
}
