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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetProperty;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 * Set property.
 *
 * @author JPEXS
 */
public class SetPropertyActionItem extends ActionItem implements SetTypeActionItem {

    /**
     * Target
     */
    public GraphTargetItem target;

    /**
     * Property index
     */
    public int propertyIndex;

    /**
     * Temp register
     */
    private int tempRegister = -1;

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
    }

    @Override
    public void setValue(GraphTargetItem value) {
        this.value = value;
    }

    @Override
    public int getTempRegister() {
        return tempRegister;
    }

    @Override
    public void setTempRegister(int tempRegister) {
        this.tempRegister = tempRegister;
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param target Target
     * @param propertyIndex Property index
     * @param value Value
     */
    public SetPropertyActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem target, int propertyIndex, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGNMENT, value);
        this.target = target;
        this.propertyIndex = propertyIndex;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (isEmptyString(target)) {
            writer.append(Action.propertyNames[propertyIndex]).append(" = ");
            return value.toString(writer, localData);
        }

        writer.append("setProperty");
        writer.spaceBeforeCallParenthesies(3);
        writer.append("(");
        target.appendTo(writer, localData);
        writer.append(", ");
        writer.append(Action.propertyNames[propertyIndex]);
        writer.append(", ");
        value.appendTo(writer, localData);
        writer.append(")");
        return writer;
    }

    @Override
    public GraphTargetItem getObject() {
        return new GetPropertyActionItem(getSrc(), getLineStartItem(), target, propertyIndex);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(target.getNeededSources());
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        int tmpReg = asGenerator.getTempRegister(localData);
        try {
            return toSourceMerge(localData, generator, target, new ActionPush((Long) (long) propertyIndex, charset), value, new ActionStoreRegister(tmpReg, charset), new ActionSetProperty(), new ActionPush(new RegisterNumber(tmpReg), charset));
        } finally {
            asGenerator.releaseTempRegister(localData, tmpReg);
        }
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        return toSourceMerge(localData, generator, target, new ActionPush((Long) (long) propertyIndex, charset), value, new ActionSetProperty());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.target);
        hash = 97 * hash + this.propertyIndex;
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
        final SetPropertyActionItem other = (SetPropertyActionItem) obj;
        if (this.propertyIndex != other.propertyIndex) {
            return false;
        }
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SetPropertyActionItem other = (SetPropertyActionItem) obj;
        if (this.propertyIndex != other.propertyIndex) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.target, other.target)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public GraphTargetItem getCompoundValue() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setCompoundValue(GraphTargetItem value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setCompoundOperator(String operator) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getCompoundOperator() {
        throw new UnsupportedOperationException("Not supported.");
    }

}
