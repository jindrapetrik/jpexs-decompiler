/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 *
 * @author JPEXS
 */
public class GetSlotAVM2Item extends AVM2Item {

    public Multiname slotName;

    public GraphTargetItem scope;

    public GraphTargetItem slotObject;

    public int slotIndex;

    public GetSlotAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem scope, GraphTargetItem slotObject, int slotIndex, Multiname slotName) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.slotName = slotName;
        this.scope = scope;
        this.slotObject = slotObject;
        this.slotIndex = slotIndex;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(scope);
        visitor.visit(slotObject);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        String name = getNameAsStr(localData);
        getSrcData().localName = name;
        return writer.append(name);
    }

    public String getNameAsStr(LocalData localData) throws InterruptedException {
        if (slotName == null) {
            return slotObject.toString(localData) + ".§§slot[" + slotIndex + "]";
        }
        return slotName.getName(localData.constantsAvm2, localData.fullyQualifiedNames, false, true);
    }

    public GraphTextWriter getName(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer.append(getNameAsStr(localData));
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
