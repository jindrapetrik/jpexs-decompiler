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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import java.util.Objects;

/**
 * Name-value pair. (For usage in objects)
 *
 * @author JPEXS
 */
public class NameValuePair extends AVM2Item {

    /**
     * Name
     */
    public GraphTargetItem name;

    /**
     * Constructor.
     * @param name Name
     * @param value Value
     */
    public NameValuePair(GraphTargetItem name, GraphTargetItem value) {
        super(name.getSrc(), name.getLineStartItem(), NOPRECEDENCE, value);
        this.name = name;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(name);
        if (value != null) {
            visitor.visit(value);
        }
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean needsParents = !((name.getNotCoerced() instanceof NumberValueAVM2Item) || (name.getNotCoerced() instanceof StringAVM2Item)); // special for obfuscated strings
        if (needsParents) {
            writer.append("(");
        }
        if ((name instanceof ConvertAVM2Item) && ((ConvertAVM2Item) name).type.equals(TypeItem.STRING)) {
            name = name.value;
        }
        name.toString(writer, localData);
        if (needsParents) {
            writer.append(")");
        }
        writer.append(":");
        if (value instanceof TernarOpItem) { //Ternar operator contains ":"
            writer.append("(");
            value.toString(writer, localData);
            writer.append(")");
        } else {
            value.toString(writer, localData);
        }
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.name);
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
        final NameValuePair other = (NameValuePair) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return name.hasSideEffect() || value.hasSideEffect();
    }
}
