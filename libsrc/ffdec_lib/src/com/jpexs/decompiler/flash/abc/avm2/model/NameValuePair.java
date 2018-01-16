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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.TernarOpItem;

/**
 *
 * @author JPEXS
 */
public class NameValuePair extends AVM2Item {

    public GraphTargetItem name;

    public NameValuePair(GraphTargetItem name, GraphTargetItem value) {
        super(name.getSrc(), name.getLineStartItem(), NOPRECEDENCE, value);
        this.name = name;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        name.toStringString(writer, localData);
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
}
