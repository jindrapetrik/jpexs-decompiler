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
package com.jpexs.decompiler.flash.action.swf7;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionScriptObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.ExtendsActionItem;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 7)
public class ActionExtends extends Action {

    public ActionExtends() {
        super(0x69, 0);
    }

    @Override
    public String toString() {
        return "Extends";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (lda.stack.size() < 2) {
            return false;
        }

        //TODO: check if its really ActionScriptObject ?
        ActionScriptObject superClass = (ActionScriptObject) lda.pop();
        ActionScriptObject subClass = (ActionScriptObject) lda.pop();
        ActionScriptObject newClass = new ActionScriptObject();
        newClass.setMember("proto", superClass.getMember("prototype"));
        newClass.setMember("constructor", superClass);
        subClass.setMember("prototype", newClass);

        subClass.setExtendsObj(superClass);

        return true;
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem superclass = stack.pop();
        GraphTargetItem subclass = stack.pop();
        output.add(new ExtendsActionItem(this, lineStartAction, subclass, superclass));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }
}
