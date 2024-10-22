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

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Set variable.
 *
 * @author JPEXS
 */
public class SetVariableActionItem extends ActionItem implements SetTypeActionItem {

    /**
     * Name
     */
    public GraphTargetItem name;

    /**
     * Temp register
     */
    private int tempRegister = -1;

    /**
     * Compound value
     */
    public GraphTargetItem compoundValue;

    /**
     * Compound operator
     */
    public String compoundOperator;

    /**
     * Force use set
     */
    public boolean forceUseSet = false;

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
     * @param name Name
     * @param value Value
     */
    public SetVariableActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem name, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGNMENT, value);
        this.name = name;
    }

    private boolean resultNeeded() {
        boolean needsTempRegister = false;
        if (value instanceof StoreRegisterActionItem) {
            StoreRegisterActionItem sr = (StoreRegisterActionItem) value;
            if (sr.temporary) {
                needsTempRegister = true;
            }
        }
        return needsTempRegister;
    }

    private boolean isValidName(LocalData localData) {
        return ((name instanceof DirectValueActionItem)) && (((DirectValueActionItem) name).isString())
                && (IdentifiersDeobfuscation.isValidName(false, ((DirectValueActionItem) name).toStringNoQuotes(localData), "this", "super"));
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (isValidName(localData) || resultNeeded()) {
            //TODO: handle result needed better, without identifierdeobfuscation
            HighlightData srcData = getSrcData();
            srcData.localName = name.toStringNoQuotes(localData);
            writer.append(IdentifiersDeobfuscation.printIdentifier(false, name.toStringNoQuotes(localData)));
            if (compoundOperator != null) {
                writer.append(" ");
                writer.append(compoundOperator);
                writer.append("= ");
                return compoundValue.toString(writer, localData);
            }
            writer.append(" = ");
            return value.toString(writer, localData);
        } else {
            writer.append("set");
            writer.spaceBeforeCallParenthesies(2);
            writer.append("(");
            name.toString(writer, localData);
            writer.append(",");
            value.toString(writer, localData);
            return writer.append(")");
        }
        //IdentifiersDeobfuscation.appendObfuscatedIdentifier(((DirectValueActionItem) name).toStringNoQuotes(localData), writer);
    }

    @Override
    public GraphTargetItem getObject() {
        return new GetVariableActionItem(getSrc(), getLineStartItem(), name);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(name.getNeededSources());
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        if (!((value instanceof SimpleValue) && ((SimpleValue) value).isSimpleValue())) {
            dependencies.add(value);
        }
        return value.isCompileTime(dependencies);
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();

        if (forceUseSet) {
            return toSourceMerge(localData, generator, name, value, new ActionSetVariable(), new ActionPush(Undefined.INSTANCE, charset));
        }
        int tmpReg = asGenerator.getTempRegister(localData);
        try {
            return toSourceMerge(localData, generator, name, value, new ActionStoreRegister(tmpReg, charset), new ActionSetVariable(), new ActionPush(new RegisterNumber(tmpReg), charset));
        } finally {
            asGenerator.releaseTempRegister(localData, tmpReg);
        }
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, name, value, new ActionSetVariable());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.name);
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
        final SetVariableActionItem other = (SetVariableActionItem) obj;
        if (!Objects.equals(this.name, other.name)) {
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
        final SetVariableActionItem other = (SetVariableActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.name, other.name)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.value, other.value)) {
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
        this.compoundOperator = operator;
    }

    @Override
    public String getCompoundOperator() {
        return compoundOperator;
    }
}
