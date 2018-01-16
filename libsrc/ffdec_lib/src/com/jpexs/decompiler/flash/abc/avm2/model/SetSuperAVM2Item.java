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
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 *
 * @author JPEXS
 */
public class SetSuperAVM2Item extends AVM2Item {

    public GraphTargetItem object;

    public FullMultinameAVM2Item propertyName;

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
    }

    public SetSuperAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value, GraphTargetItem object, FullMultinameAVM2Item propertyName) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGMENT, value);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (!object.toString().equals("this")) {
            object.toString(writer, localData);
            writer.append(".");
        }
        writer.append("super.");
        propertyName.toString(writer, localData);
        writer.append(" = ");
        return value.toString(writer, localData);
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
