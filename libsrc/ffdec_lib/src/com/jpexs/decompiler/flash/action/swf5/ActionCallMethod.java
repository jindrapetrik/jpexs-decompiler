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
import com.jpexs.decompiler.flash.action.ActionScriptObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 5)
public class ActionCallMethod extends Action {

    public ActionCallMethod() {
        super(0x52, 0);
    }

    @Override
    public String toString() {
        return "CallMethod";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (lda.stack.size() < 3) {
            return false;
        }

        String methodName = lda.popAsString();
        Object obj0 = lda.pop();
        if (!(obj0 instanceof ActionScriptObject)) {
            return false;
        }

        ActionScriptObject obj = (ActionScriptObject) obj0;
        int numArgs = (int) (double) lda.popAsNumber();
        if (lda.stack.size() < numArgs) {
            return false;
        }

        List<Object> args = new ArrayList<>();
        for (int i = 0; i < numArgs; i++) {
            args.add(lda.pop());
        }

        ActionScriptFunction f = (ActionScriptFunction) obj.getMember(methodName);

        lda.stack.push(lda.stage.callFunction(f.getFunctionOffset(), f.getFunctionLength(), args, f.getFuncRegNames(), obj));
        return true;
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem methodName = stack.pop();
        GraphTargetItem scriptObject = stack.pop();
        long numArgs = popLong(stack);
        List<GraphTargetItem> args = new ArrayList<>();
        for (long l = 0; l < numArgs; l++) {
            args.add(stack.pop());
        }
        stack.push(new CallMethodActionItem(this, lineStartAction, scriptObject, methodName, args));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        int result = 3;
        if (stack.size() >= 3) {
            result += stack.peek(3).getAsLong();
        }

        return result;
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }
}
