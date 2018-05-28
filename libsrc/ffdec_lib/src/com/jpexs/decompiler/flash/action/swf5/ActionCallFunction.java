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
import com.jpexs.decompiler.flash.action.ActionScriptFunction;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.CallFunctionActionItem;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 5)
public class ActionCallFunction extends Action {

    public ActionCallFunction() {
        super(0x3D, 0);
    }

    @Override
    public String toString() {
        return "CallFunction";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        String functionName = lda.popAsString();
        int numArgs = (int) (double) lda.popAsNumber();
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < numArgs; i++) {
            args.add(lda.pop());
        }

        for (ActionScriptFunction f : lda.functions) {
            if (functionName.equals(f.getFunctionName())) {
                lda.push(lda.stage.callFunction(f.getFunctionOffset(), f.getFunctionLength(), args, f.getFuncRegNames(), Undefined.INSTANCE /*?*/));
                return true;
            }
        }

        return true;
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem functionName = stack.pop();
        long numArgs = popLong(stack);
        List<GraphTargetItem> args = new ArrayList<>();
        for (long l = 0; l < numArgs; l++) {
            args.add(stack.pop());
        }
        CallFunctionActionItem cft = new CallFunctionActionItem(this, lineStartAction, functionName, args);
        cft.calculatedFunction = functions.get(functionName.toStringNoQuotes(LocalData.empty));
        stack.push(cft);
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        int result = 2;
        if (stack.size() >= 2) {
            result += stack.peek(2).getAsLong();
        }

        return result;
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }
}
