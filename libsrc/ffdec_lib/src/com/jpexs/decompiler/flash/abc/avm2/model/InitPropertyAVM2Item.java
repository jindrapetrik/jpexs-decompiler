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

import com.jpexs.decompiler.flash.abc.avm2.model.clauses.AssignmentAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.Objects;

/**
 * Initialize property.
 *
 * @author JPEXS
 */
public class InitPropertyAVM2Item extends AVM2Item implements SetTypeAVM2Item, AssignmentAVM2Item {

    /**
     * Object
     */
    public GraphTargetItem object;

    /**
     * Property name
     */
    public FullMultinameAVM2Item propertyName;

    /**
     * Declaration
     */
    public DeclarationAVM2Item declaration;

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

    /**
     * Call type
     */
    public GraphTargetItem callType;

    /**
     * Is static
     */
    public boolean isStatic;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(object);
        visitor.visit(propertyName);
        visitor.visit(value);
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
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param object Object
     * @param propertyName Property name
     * @param value Value
     * @param type Type
     * @param callType Call type
     * @param isStatic Is static
     */
    public InitPropertyAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, FullMultinameAVM2Item propertyName, GraphTargetItem value, GraphTargetItem type, GraphTargetItem callType, boolean isStatic) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGNMENT, value);
        this.object = object;
        this.propertyName = propertyName;
        this.type = type;
        this.callType = callType;
        this.isStatic = isStatic;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        formatProperty(writer, object, propertyName, localData, isStatic);

        if (compoundOperator != null) {
            writer.append(" ");
            writer.append(compoundOperator);
            writer.append("= ");
            return compoundValue.toString(writer, localData);
        }
        writer.append(" = ");
        return value.toString(writer, localData);
    }

    @Override
    public GraphTargetItem getObject() {
        return new GetPropertyAVM2Item(getInstruction(), getLineStartIns(), object, propertyName, type, callType, isStatic);
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
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
        hash = 17 * hash + Objects.hashCode(this.object);
        hash = 17 * hash + Objects.hashCode(this.propertyName);
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
        final InitPropertyAVM2Item other = (InitPropertyAVM2Item) obj;
        if (!Objects.equals(this.object, other.object)) {
            return false;
        }
        if (!Objects.equals(this.propertyName, other.propertyName)) {
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
