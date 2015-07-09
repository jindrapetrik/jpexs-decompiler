/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;

/**
 *
 * @author JPEXS
 */
public class PushItem extends GraphTargetItem {

    public PushItem(GraphTargetItem val) {
        super(val.getSrc(), val.getPrecedence());
        this.value = val;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        //Logger.getLogger(PushItem.class.getName()).log(Level.WARNING, "Push item left in the source code");
        writer.append("§§push(");
        value.appendTo(writer, localData);
        writer.append(")");
        return writer;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        return value.returnType();
    }

    @Override
    public GraphTargetItem getNotCoerced() {
        return value.getNotCoerced();
    }

    @Override
    public GraphTargetItem getThroughDuplicate() {
        return value.getThroughDuplicate();
    }

    @Override
    public GraphTargetItem getThroughRegister() {
        return value.getThroughRegister();
    }
}
