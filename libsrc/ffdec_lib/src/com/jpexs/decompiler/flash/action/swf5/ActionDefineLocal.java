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
package com.jpexs.decompiler.flash.action.swf5;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.DefineLocalActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
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
@SWFVersion(from = 5)
public class ActionDefineLocal extends Action {

    public ActionDefineLocal() {
        super(0x3C, 0);
    }

    @Override
    public String toString() {
        return "DefineLocal";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (lda.stack.size() < 2) {
            return false;
        }

        Object value = lda.pop();
        lda.localVariables.put(EcmaScript.toString(lda.pop()), value);
        return true;
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem value = stack.pop();
        GraphTargetItem name = stack.pop();
        String nameStr;
        if (name instanceof DirectValueActionItem) {
            nameStr = name.toStringNoQuotes(LocalData.empty);
        } else {
            nameStr = EcmaScript.toString(name.getResult());
        }
        variables.put(nameStr, value);
        output.add(new DefineLocalActionItem(this, lineStartAction, name, value));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }
}
