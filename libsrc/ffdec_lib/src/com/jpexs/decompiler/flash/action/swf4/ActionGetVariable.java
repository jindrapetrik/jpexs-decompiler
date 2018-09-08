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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.EvalActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.GetVersionActionItem;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 4)
public class ActionGetVariable extends Action {

    public ActionGetVariable() {
        super(0x1C, 0);
    }

    @Override
    public String toString() {
        return "GetVariable";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (lda.stackIsEmpty()) {
            return false;
        }

        Object value = lda.localVariables.get(EcmaScript.toString(lda.pop()));
        if (value == null) {
            value = Undefined.INSTANCE;
        }

        lda.push(value);
        return true;
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem name = stack.pop();
        String nameStr;
        if (name instanceof DirectValueActionItem) {
            nameStr = name.toStringNoQuotes(LocalData.empty);
        } else {
            nameStr = EcmaScript.toString(name.getResult());
        }
        GraphTargetItem computedVal = variables.get(nameStr);
        if (name instanceof DirectValueActionItem && ((DirectValueActionItem) name).value.equals("/:$version")) {
            stack.push(new GetVersionActionItem(this, lineStartAction));
        } else if (!(name instanceof DirectValueActionItem) && !(name instanceof GetVariableActionItem)) {
            stack.push(new EvalActionItem(this, lineStartAction, name));
        } else {
            GetVariableActionItem gvt = new GetVariableActionItem(this, lineStartAction, name);
            gvt.setComputedValue(computedVal);
            stack.push(gvt);
        }
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }
}
