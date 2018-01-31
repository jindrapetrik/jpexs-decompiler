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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.DisplayObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.StartDragActionItem;
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
@SWFVersion(from = 4)
public class ActionStartDrag extends Action {

    public ActionStartDrag() {
        super(0x27, 0);
    }

    @Override
    public String toString() {
        return "StartDrag";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (lda.target instanceof DisplayObject) {
            ((DisplayObject) lda.target).startDrag();
        }

        return true;
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem target = stack.pop();
        GraphTargetItem lockCenter = stack.pop();
        GraphTargetItem constrain = stack.pop();

        boolean hasConstrains = true;
        if (constrain instanceof DirectValueActionItem) {
            if (Double.compare(constrain.getResultAsNumber(), 0) == 0) {
                hasConstrains = false;
            }
        }

        GraphTargetItem x1 = null;
        GraphTargetItem y1 = null;
        GraphTargetItem x2 = null;
        GraphTargetItem y2 = null;
        if (hasConstrains) {
            y2 = stack.pop();
            x2 = stack.pop();
            y1 = stack.pop();
            x1 = stack.pop();
        }

        output.add(new StartDragActionItem(this, lineStartAction, target, lockCenter, constrain, x1, y1, x2, y2));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        if (stack.size() >= 3) {
            GraphTargetItem constrain = stack.peek(3);
            boolean hasConstrains = true;
            if (constrain instanceof DirectValueActionItem) {
                if (Double.compare(constrain.getResultAsNumber(), 0) == 0) {
                    hasConstrains = false;
                }
            }
            if (hasConstrains) {
                return 7;
            }
        }

        return 3;
    }
}
