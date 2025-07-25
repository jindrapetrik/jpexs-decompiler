/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action.swf6;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionScriptObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.operations.InstanceOfActionItem;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * InstanceOf action - Checks if an object is an instance of a class.
 *
 * @author JPEXS
 */
@SWFVersion(from = 6)
public class ActionInstanceOf extends Action {

    /**
     * Constructor.
     */
    public ActionInstanceOf() {
        super(0x54, 0, Utf8Helper.charsetName);
    }

    @Override
    public String toString() {
        return "InstanceOf";
    }

    /**
     * Checks if an object is an instance of a class.
     *
     * @param a Object
     * @param b Class
     * @return True if object is an instance of class
     */
    public static boolean getInstanceOfResult(Object a, Object b) {
        ActionScriptObject type = (ActionScriptObject) b;
        ActionScriptObject obj = (ActionScriptObject) a;

        ActionScriptObject pobj = obj;

        if (pobj.getImplementsObjs().contains(type)) {
            return true;
        }

        while (pobj.getExtendsObj() != null) {
            if (pobj.getExtendsObj() == type) {
                return true;
            }
            pobj = (ActionScriptObject) pobj.getExtendsObj();
            if (pobj.getImplementsObjs().contains(type)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (!lda.stackHasMinSize(2)) {
            return false;
        }

        Object type = lda.pop();
        Object obj = lda.pop();
        if (getInstanceOfResult(obj, type)) {
            lda.push(Boolean.TRUE);
        } else {
            lda.push(Boolean.FALSE);
        }

        return true;
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem a = stack.pop();
        GraphTargetItem b = stack.pop();
        stack.push(new InstanceOfActionItem(this, lineStartAction, b, a));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }
}
