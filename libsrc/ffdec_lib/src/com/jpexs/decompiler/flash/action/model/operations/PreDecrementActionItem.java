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
package com.jpexs.decompiler.flash.action.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetProperty;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDecrement;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.UnaryOpItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre decrement.
 *
 * @author JPEXS
 */
public class PreDecrementActionItem extends UnaryOpItem {

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param object Object
     */
    public PreDecrementActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object) {
        super(instruction, lineStartIns, PRECEDENCE_UNARY, object, "--", "" /*"Number" Causes unnecessary ++Number(xx) when xx not number*/);
    }

    @Override
    public Object getResult() {
        return value.getResultAsNumber() - 1;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        List<GraphSourceItem> ret = new ArrayList<>();
        GraphTargetItem val = value;
        if (val instanceof VariableActionItem) {
            val = ((VariableActionItem) val).getBoxedValue();
        }
        if (val instanceof GetVariableActionItem) {
            GetVariableActionItem gv = (GetVariableActionItem) val;
            ret.addAll(gv.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetVariable
            ret.addAll(gv.toSource(localData, generator));
            ret.add(new ActionDecrement());
            int tmpReg = asGenerator.getTempRegister(localData);
            ret.add(new ActionStoreRegister(tmpReg, charset));
            ret.add(new ActionSetVariable());
            ret.add(new ActionPush(new RegisterNumber(tmpReg), charset));
            asGenerator.releaseTempRegister(localData, tmpReg);
        } else if (val instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) val;
            ret.addAll(mem.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetMember
            ret.addAll(mem.toSource(localData, generator));
            ret.add(new ActionDecrement());
            int tmpReg = asGenerator.getTempRegister(localData);
            ret.add(new ActionStoreRegister(tmpReg, charset));
            ret.add(new ActionSetMember());
            ret.add(new ActionPush(new RegisterNumber(tmpReg), charset));
            asGenerator.releaseTempRegister(localData, tmpReg);
        } else if ((val instanceof DirectValueActionItem) && ((DirectValueActionItem) val).value instanceof RegisterNumber) {
            RegisterNumber rn = (RegisterNumber) ((DirectValueActionItem) val).value;
            ret.add(new ActionPush(new RegisterNumber(rn.number), charset));
            ret.add(new ActionDecrement());
            ret.add(new ActionStoreRegister(rn.number, charset));
        } else if (val instanceof GetPropertyActionItem) {
            GetPropertyActionItem gp = (GetPropertyActionItem) val;
            ret.addAll(gp.toSource(localData, generator)); // old value
            ret.addAll(gp.toSource(localData, generator));
            ret.remove(ret.size() - 1);
            ret.addAll(gp.toSource(localData, generator));
            ret.add(new ActionDecrement());
            ret.add(new ActionSetProperty());
        }
        return ret;
    }

    @Override
    public GraphTargetItem returnType() {
        return value.returnType();
    }
}
