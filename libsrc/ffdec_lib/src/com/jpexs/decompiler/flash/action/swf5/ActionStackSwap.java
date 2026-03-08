/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.ActionGraphTargetDialect;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import com.jpexs.decompiler.graph.model.TemporaryItem;
import com.jpexs.helpers.utf8.Utf8Helper;
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
     */
    public ActionStackSwap() {
        super(0x4D, 0, Utf8Helper.charsetName);
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
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException {
        GraphTargetItem o1 = stack.pop();
        GraphTargetItem o2 = stack.pop();

        if ((((o1 instanceof SimpleValue) && ((SimpleValue) o1).isSimpleValue() && !(o1 instanceof DuplicateItem)))
                && (((o2 instanceof SimpleValue) && ((SimpleValue) o2).isSimpleValue() && !(o2 instanceof DuplicateItem)))) {
            stack.push(o1);
            stack.push(o2);
            o1.getMoreSrc().add(new GraphSourceItemPos(this, 0));
            o2.getMoreSrc().add(new GraphSourceItemPos(this, 0));
            return;
        }
        if (o2 instanceof TemporaryItem) {
            stack.push(o1);
            stack.push(o2);
            return;
        }
        if (o2 instanceof DuplicateItem) {
            stack.push(o1);
            stack.push(o2);
            return;
        }

        if (o2 instanceof DuplicateSourceItem) {
            stack.push(o1);
            stack.push(o2);
            return;
        }

        int temp = localData.maxTempIndex.getVal() + 1;
        localData.maxTempIndex.setVal(temp);
        stack.finishBlock(output);
        stack.addToOutput(new SetTemporaryItem(ActionGraphTargetDialect.INSTANCE, this, localData.lineStartInstruction, o2, temp, "swap", 1));
        stack.push(o1);
        stack.push(new TemporaryItem(ActionGraphTargetDialect.INSTANCE, this, localData.lineStartInstruction, o2, temp));
        stack.finishBlock(output);
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
