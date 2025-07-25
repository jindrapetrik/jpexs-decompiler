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
package com.jpexs.decompiler.flash.action.swf5;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionScriptObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.EnumerateActionItem;
import com.jpexs.decompiler.flash.action.model.EnumeratedValueActionItem;
import com.jpexs.decompiler.flash.ecma.Null;
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
 * Enumerate action - Enumerate object members.
 *
 * @author JPEXS
 */
@SWFVersion(from = 5)
public class ActionEnumerate extends Action {

    /**
     * Constructor.
     */
    public ActionEnumerate() {
        super(0x46, 0, Utf8Helper.charsetName);
    }

    @Override
    public String toString() {
        return "Enumerate";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (lda.stackIsEmpty()) {
            return false;
        }

        String objectName = lda.popAsString();
        lda.push(Null.INSTANCE);

        Object o = lda.stage.getMember(objectName);

        if (o instanceof ActionScriptObject) {
            List<String> members = ((ActionScriptObject) o).enumerate();
            for (String m : members) {
                lda.push(m);
            }
        }
        return true;
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem object = stack.pop();
        stack.push(new EnumeratedValueActionItem());
        output.add(new EnumerateActionItem(this, lineStartAction, object));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }
}
