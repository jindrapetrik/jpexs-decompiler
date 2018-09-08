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

/**
 *
 * @author JPEXS
 */
public class SetPropertyActionItem extends ActionItem implements SetTypeActionItem {

    public GraphTargetItem target;

    public int propertyIndex;

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
    }

    @Override
    public void setValue(GraphTargetItem value) {
        this.value = value;
    }

    private int tempRegister = -1;

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

    public SetPropertyActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem target, int propertyIndex, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGMENT, value);
        this.target = target;
        this.propertyIndex = propertyIndex;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (isEmptyString(target)) {
            writer.append(Action.propertyNames[propertyIndex]).append(" = ");
            return value.toString(writer, localData);
        }

        if ((target instanceof DirectValueActionItem) && ((DirectValueActionItem) target).isString()) {
            target.toStringNoQuotes(writer, localData);
            writer.append(":" + Action.propertyNames[propertyIndex]).append(" = ");
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
        int tmpReg = asGenerator.getTempRegister(localData);
        try {
            return toSourceMerge(localData, generator, target, new ActionPush((Long) (long) propertyIndex), value, new ActionStoreRegister(tmpReg), new ActionSetProperty(), new ActionPush(new RegisterNumber(tmpReg)));
        } finally {
            asGenerator.releaseTempRegister(localData, tmpReg);
        }
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, target, new ActionPush((Long) (long) propertyIndex), value, new ActionSetProperty());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
