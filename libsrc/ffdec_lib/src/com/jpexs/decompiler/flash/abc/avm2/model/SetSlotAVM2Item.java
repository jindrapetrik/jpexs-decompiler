/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.AssignmentAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.Objects;

/**
 * Set slot value.
 *
 * @author JPEXS
 */
public class SetSlotAVM2Item extends AVM2Item implements SetTypeAVM2Item, AssignmentAVM2Item {

    /**
     * Slot name
     */
    public Multiname slotName;

    /**
     * Scope
     */
    public GraphTargetItem scope;

    /**
     * Declaration
     */
    public DeclarationAVM2Item declaration;

    /**
     * Slot object
     */
    public GraphTargetItem slotObject;

    /**
     * Slot index
     */
    public int slotIndex;

    /**
     * Compound value
     */
    public GraphTargetItem compoundValue;

    /**
     * Compound operator
     */
    public String compoundOperator;

    /**
     * Type
     */
    public GraphTargetItem type;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(scope);
        visitor.visit(slotObject);
        if (value != null) {
            visitor.visit(value);
        }
    }

    @Override
    public DeclarationAVM2Item getDeclaration() {
        return declaration;
    }

    @Override
    public void setDeclaration(DeclarationAVM2Item declaration) {
        this.declaration = declaration;
    }

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param scope Scope
     * @param slotObject Slot object
     * @param slotIndex Slot index
     * @param slotName Slot name
     * @param value Value
     * @param type Type
     */
    public SetSlotAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem scope, GraphTargetItem slotObject, int slotIndex, Multiname slotName, GraphTargetItem value, GraphTargetItem type) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGNMENT, value);
        this.slotName = slotName;
        this.scope = scope;
        this.slotObject = slotObject;
        this.slotIndex = slotIndex;
        this.type = type;
    }

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        getSrcData().localName = getNameAsStr(localData);
        if (getSrcData().localName.equals(value.toString(localData))) {
            //assigning parameters to activation reg
            return writer;
        }
        getName(writer, localData);

        if (compoundOperator != null) {
            writer.append(" ");
            writer.append(compoundOperator);
            writer.append("= ");
            return compoundValue.toString(writer, localData);
        }
        writer.append(" = ");
        /*if (declaration != null && !declaration.type.equals(TypeItem.UNBOUNDED) && (value instanceof ConvertAVM2Item)) {
            return value.value.toString(writer, localData);
        }*/
        return SetTypeIns.handleNumberToInt(value, type).toString(writer, localData);
    }

    /**
     * Gets name as string.
     * @param localData Local data
     * @return Name as string
     * @throws InterruptedException On interrupt
     */
    public String getNameAsStr(LocalData localData) throws InterruptedException {
        if (slotName == null) {
            return slotObject.toString(localData) + ".§§slot[" + slotIndex + "]";
        }
        return slotName.getName(localData.constantsAvm2, localData.fullyQualifiedNames, false, true);
    }

    /**
     * Gets name.
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter getName(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer.append(getNameAsStr(localData));
    }

    @Override
    public GraphTargetItem getObject() {
        return new GetSlotAVM2Item(getInstruction(), getLineStartIns(), scope, slotObject, slotIndex, slotName, type);
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.scope);
        hash = 29 * hash + this.slotIndex;
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
        final SetSlotAVM2Item other = (SetSlotAVM2Item) obj;
        if (this.slotIndex != other.slotIndex) {
            return false;
        }
        if (!Objects.equals(this.scope, other.scope)) {
            return false;
        }

        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public GraphTargetItem getCompoundValue() {
        return compoundValue;
    }

    @Override
    public void setCompoundValue(GraphTargetItem value) {
        this.compoundValue = value;
    }

    @Override
    public void setCompoundOperator(String operator) {
        compoundOperator = operator;
    }

    @Override
    public String getCompoundOperator() {
        return compoundOperator;
    }
}
