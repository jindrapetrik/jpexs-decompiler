/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIIns;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.Objects;
import java.util.Set;

/**
 * Decrement value by 1.
 *
 * @author JPEXS
 */
public class DecrementAVM2Item extends AVM2Item {

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param object Object
     */
    public DecrementAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object) {
        super(instruction, lineStartIns, PRECEDENCE_ADDITIVE, object);
    }

    private boolean isInteger() {
        return getInstruction() != null && getInstruction().definition instanceof DecrementIIns;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean integer = isInteger();
        if (integer) {
            writer.append("int(");
        }
        if (integer && !TypeItem.INT.equals(value.returnType())) {
            writer.append("int(");
            value.toString(writer, localData);
            writer.append(")");
        } else if (value.getPrecedence() > precedence) {
            writer.append("(");
            value.toString(writer, localData);
            writer.append(")");
        } else {
            value.toString(writer, localData);
        }
        writer.append(" - 1");
        return integer ? writer.append(")") : writer;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        dependencies.add(value);
        return value.isCompileTime(dependencies);
    }

    @Override
    public Object getResult() {
        if (isInteger()) {
            return EcmaScript.toInt32(value.getResult()) - 1;
        }
        return value.getResultAsNumber() - 1;
    }

    @Override
    public GraphTargetItem returnType() {
        return isInteger() ? TypeItem.INT : TypeItem.NUMBER;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
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
        final DecrementAVM2Item other = (DecrementAVM2Item) obj;
        if (isInteger() != other.isInteger()) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

}
