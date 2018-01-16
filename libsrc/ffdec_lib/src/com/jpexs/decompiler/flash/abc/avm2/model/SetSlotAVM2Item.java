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

import com.jpexs.decompiler.flash.abc.avm2.model.clauses.AssignmentAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
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
public class SetSlotAVM2Item extends AVM2Item implements SetTypeAVM2Item, AssignmentAVM2Item {

    public Multiname slotName;

    public GraphTargetItem scope;

    public DeclarationAVM2Item declaration;

    @Override
    public DeclarationAVM2Item getDeclaration() {
        return declaration;
    }

    @Override
    public void setDeclaration(DeclarationAVM2Item declaration) {
        this.declaration = declaration;
    }

    public SetSlotAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem scope, Multiname slotName, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGMENT, value);
        this.slotName = slotName;
        this.scope = scope;
    }

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        getSrcData().localName = slotName == null ? "/*UnknownSlot*/" : slotName.getName(localData.constantsAvm2, localData.fullyQualifiedNames, false, true);
        if (getSrcData().localName.equals(value.toString(localData))) {
            //assigning parameters to activation reg
            return writer;
        }
        getName(writer, localData);
        writer.append(" = ");
        if (declaration != null && !declaration.type.equals(TypeItem.UNBOUNDED) && (value instanceof ConvertAVM2Item)) {
            return value.value.toString(writer, localData);
        }
        return value.toString(writer, localData);
    }

    public String getNameAsStr(LocalData localData) {
        return slotName == null ? "/*UnknownSlot*/" : slotName.getName(localData.constantsAvm2, localData.fullyQualifiedNames, false, true);
    }

    public GraphTextWriter getName(GraphTextWriter writer, LocalData localData) {
        if (slotName == null) {
            return writer.append("/*UnknownSlot*/");
        }
        return writer.append(slotName.getName(localData.constantsAvm2, localData.fullyQualifiedNames, false, true));
    }

    @Override
    public GraphTargetItem getObject() {
        return new GetSlotAVM2Item(getInstruction(), getLineStartIns(), scope, slotName);
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
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
