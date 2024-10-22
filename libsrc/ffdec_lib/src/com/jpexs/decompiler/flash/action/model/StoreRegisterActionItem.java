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
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 * Store value to a register.
 *
 * @author JPEXS
 */
public class StoreRegisterActionItem extends ActionItem implements SetTypeActionItem {

    /**
     * Register number
     */
    public RegisterNumber register;

    /**
     * Define
     */
    public boolean define = false;

    /**
     * Temporary
     */
    public boolean temporary = false;

    /**
     * Compound value
     */
    public GraphTargetItem compoundValue;

    /**
     * Compound operator
     */
    public String compoundOperator;

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
     * @param register Register number
     * @param value Value
     * @param define Define
     */
    public StoreRegisterActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, RegisterNumber register, GraphTargetItem value, boolean define) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGNMENT, value);
        this.register = register;
        this.define = define;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (temporary) {
            value.toString(writer, localData);
        } else {
            HighlightData srcData = getSrcData();
            srcData.localName = register.translate();
            srcData.regIndex = register.number;

            if (define) {
                srcData.declaration = true;
                srcData.declaredType = DottedChain.ALL;
                writer.append("var ");
            }
            writer.append(IdentifiersDeobfuscation.printIdentifier(false, register.translate()));

            if (compoundOperator != null) {
                writer.append(" ");
                writer.append(compoundOperator);
                writer.append("= ");
                return compoundValue.toString(writer, localData);
            }
            writer.append(" = ");
            value.toString(writer, localData);
        }
        return writer;
    }

    @Override
    public GraphTargetItem getObject() {
        return new DirectValueActionItem(getSrc(), getLineStartItem(), -1, register, null);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        return toSourceMerge(localData, generator, value, new ActionStoreRegister(register.number, charset));
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    /*
    NOT COMPILE TIME! This causes problems in simplifyExpressions, etc.
    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        dependencies.add(value);
        return value.isCompileTime(dependencies);
    }
     */
    @Override
    public Object getResult() {
        return value.getResult();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.register);
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
        final StoreRegisterActionItem other = (StoreRegisterActionItem) obj;
        if (!Objects.equals(this.register, other.register)) {
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
        final StoreRegisterActionItem other = (StoreRegisterActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.register, other.register)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
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
