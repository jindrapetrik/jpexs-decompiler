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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.model.operations.AddActionItem;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetProperty;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionIncrement;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class PostIncrementActionItem extends ActionItem implements SetTypeActionItem {

    public GraphTargetItem object;

    private int tempRegister = -1;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(object);
        return ret;
    }

    public PostIncrementActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object) {
        super(instruction, lineStartIns, PRECEDENCE_POSTFIX);
        this.object = object;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        object.toString(writer, localData);
        return writer.append("++");
    }

    @Override
    public Object getResult() {
        return object.getResultAsNumber();
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public GraphTargetItem getObject() {
        return object;
    }

    @Override
    public GraphTargetItem getValue() {
        return new AddActionItem(null, null, object, new DirectValueActionItem(null, null, 0, 1L, null), true);
    }

    @Override
    public void setTempRegister(int regIndex) {
        tempRegister = regIndex;
    }

    @Override
    public int getTempRegister() {
        return tempRegister;
    }

    @Override
    public void setValue(GraphTargetItem value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();

        GraphTargetItem val = object;
        if (val instanceof VariableActionItem) {
            val = ((VariableActionItem) val).getBoxedValue();
        }

        if (val instanceof GetVariableActionItem) {
            GetVariableActionItem gv = (GetVariableActionItem) val;
            ret.addAll(gv.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetVariable
            ret.addAll(gv.toSource(localData, generator));
            ret.add(new ActionIncrement());
            ret.add(new ActionSetVariable());
        } else if (val instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) val;
            ret.addAll(mem.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetMember
            ret.addAll(mem.toSource(localData, generator));
            ret.add(new ActionIncrement());
            ret.add(new ActionSetMember());
        } else if ((val instanceof DirectValueActionItem) && ((DirectValueActionItem) val).value instanceof RegisterNumber) {
            RegisterNumber rn = (RegisterNumber) ((DirectValueActionItem) val).value;
            ret.add(new ActionPush(new RegisterNumber(rn.number)));
            ret.add(new ActionIncrement());
            ret.add(new ActionStoreRegister(rn.number));
            ret.add(new ActionPop());
        } else if (val instanceof GetPropertyActionItem) {
            GetPropertyActionItem gp = (GetPropertyActionItem) val;
            ret.addAll(gp.toSource(localData, generator));
            ret.remove(ret.size() - 1);
            ret.addAll(gp.toSource(localData, generator));
            ret.add(new ActionIncrement());
            ret.add(new ActionSetProperty());
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, object.toSource(localData, generator), toSourceIgnoreReturnValue(localData, generator));
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
