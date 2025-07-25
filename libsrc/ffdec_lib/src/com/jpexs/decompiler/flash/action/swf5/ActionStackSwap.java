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
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StackSwap action - Swap top two stack values.
 *
 * @author JPEXS
 */
@SWFVersion(from = 5)
public class ActionStackSwap extends Action {

    /**
     * Constructor.
     *
     * @param charset Charset
     */
    public ActionStackSwap(String charset) {
        super(0x4D, 0, charset);
    }

    @Override
    public String toString() {
        return "StackSwap";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (!lda.stackHasMinSize(2)) {
            return false;
        }

        Object obj1 = lda.pop();
        Object obj2 = lda.pop();
        lda.push(obj1);
        lda.push(obj2);
        return true;
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem a = stack.pop();
        GraphTargetItem b = stack.pop();
        stack.push(a);
        stack.push(b);
        a.getMoreSrc().add(new GraphSourceItemPos(this, 0));
        b.getMoreSrc().add(new GraphSourceItemPos(this, 0));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }
}
