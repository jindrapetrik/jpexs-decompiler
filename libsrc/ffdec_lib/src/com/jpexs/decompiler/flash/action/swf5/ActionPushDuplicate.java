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
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.HasTempIndex;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.List;

/**
 * PushDuplicate action - Push duplicate of top stack value.
 *
 * @author JPEXS
 */
@SWFVersion(from = 5)
public class ActionPushDuplicate extends Action {

    /**
     * Constructor.
     *
     */
    public ActionPushDuplicate() {
        super(0x4C, 0, Utf8Helper.charsetName);
    }

    @Override
    public String toString() {
        return "PushDuplicate";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (lda.stackIsEmpty()) {
            return false;
        }

        lda.push(lda.peek());
        return true;
    }

    @Override
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException {
        GraphTargetItem v = stack.pop();
        int temp;
                  
        if (v instanceof SimpleValue) {
            SimpleValue sv = (SimpleValue) v;
            if (sv.isSimpleValue()) {
                stack.push(v);
                stack.push(v);
                return;
            }
        }
        
       
        if (v instanceof HasTempIndex) {
            temp = ((HasTempIndex) v).getTempIndex();
            stack.push(v);
        } else {
            temp = localData.maxTempIndex.getVal() + 1;
            localData.maxTempIndex.setVal(temp);
            stack.finishBlock(output);
            stack.addToOutput(new SetTemporaryItem(ActionGraphTargetDialect.INSTANCE, this, localData.lineStartInstruction, v, temp, "dup", 2));
            stack.finishBlock(output);

            stack.push(new DuplicateSourceItem(ActionGraphTargetDialect.INSTANCE, this, localData.lineStartInstruction, v, temp));
        }
        
        //stack.push(v);
        stack.push(new DuplicateItem(ActionGraphTargetDialect.INSTANCE, this, localData.lineStartInstruction, v, temp));
        //v.moreSrc.add(new GraphSourceItemPos(ins, 0));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }
}
