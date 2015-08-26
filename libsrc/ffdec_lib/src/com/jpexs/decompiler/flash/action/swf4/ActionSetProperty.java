/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.DecrementActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.IncrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostIncrementActionItem;
import com.jpexs.decompiler.flash.action.model.SetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.operations.PreDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.operations.PreIncrementActionItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.HashMap;
import java.util.List;

public class ActionSetProperty extends Action {

    public ActionSetProperty() {
        super(0x23, 0);
    }

    @Override
    public String toString() {
        return "SetProperty";
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem value = stack.pop().getThroughDuplicate();
        GraphTargetItem index = stack.pop().getThroughDuplicate();
        GraphTargetItem target = stack.pop().getThroughDuplicate();
        int indexInt = 0;
        if (index instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) index).value instanceof Long) {
                indexInt = (int) (long) (Long) ((DirectValueActionItem) index).value;
            } else if (((DirectValueActionItem) index).value instanceof Double) {
                indexInt = (int) Math.round((Double) ((DirectValueActionItem) index).value);
            } else if (((DirectValueActionItem) index).value instanceof Float) {
                indexInt = (int) Math.round((Float) ((DirectValueActionItem) index).value);
            }
        }
        if (value.getThroughDuplicate() instanceof IncrementActionItem) {
            GraphTargetItem obj = ((IncrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();
                stack.push(new PostIncrementActionItem(this, obj));
                return;
            }
        }
        if (value.getThroughDuplicate() instanceof DecrementActionItem) {
            GraphTargetItem obj = ((DecrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();
                stack.push(new PostDecrementActionItem(this, obj));
                return;
            }
        }

        GraphTargetItem ret = new SetPropertyActionItem(this, target, indexInt, value);

        if (value instanceof StoreRegisterActionItem) {
            StoreRegisterActionItem sr = (StoreRegisterActionItem) value;
            if (sr.define) {
                value = sr.getValue();
                ((SetPropertyActionItem) ret).setValue(value);
                if (value instanceof IncrementActionItem) {
                    if (((IncrementActionItem) value).object instanceof GetPropertyActionItem) {
                        if (((GetPropertyActionItem) ((IncrementActionItem) value).object).valueEquals(((SetPropertyActionItem) ret).getObject())) {
                            ret = new PreIncrementActionItem(this, ((IncrementActionItem) value).object);
                        }
                    }
                } else if (value instanceof DecrementActionItem) {
                    if (((DecrementActionItem) value).object instanceof GetPropertyActionItem) {
                        if (((GetPropertyActionItem) ((DecrementActionItem) value).object).valueEquals(((SetPropertyActionItem) ret).getObject())) {
                            ret = new PreDecrementActionItem(this, ((DecrementActionItem) value).object);
                        }
                    }
                } else {
                    sr.temporary = true;
                    ((SetPropertyActionItem) ret).setValue(sr);
                }
                variables.put("__register" + sr.register.number, new TemporaryRegister(sr.register.number, ret));
                return;
            }
        }
        output.add(ret);
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 3;
    }
}
