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
import com.jpexs.decompiler.flash.action.StoreTypeAction;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DecrementActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.IncrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostIncrementActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.operations.PreDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.operations.PreIncrementActionItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;

public class ActionSetVariable extends Action implements StoreTypeAction {

    public ActionSetVariable() {
        super(0x1D, 0);
    }

    @Override
    public String toString() {
        return "SetVariable";
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem value = stack.pop().getThroughDuplicate();
        GraphTargetItem name = stack.pop();
        variables.put(name.toStringNoQuotes(LocalData.empty), value);
        if (value instanceof IncrementActionItem) {
            GraphTargetItem obj = ((IncrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();
                stack.push(new PostIncrementActionItem(this, obj));
                return;
            }
        }
        if (value instanceof DecrementActionItem) {
            GraphTargetItem obj = ((DecrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();
                stack.push(new PostDecrementActionItem(this, obj));
                return;
            }
        }
        if (value instanceof IncrementActionItem) {
            if (((IncrementActionItem) value).object instanceof GetVariableActionItem) {
                if (((GetVariableActionItem) ((IncrementActionItem) value).object).name.valueEquals(name)) {
                    output.add(new PostIncrementActionItem(this, ((IncrementActionItem) value).object));
                    return;
                }
            }
        }
        if (value instanceof DecrementActionItem) {
            if (((DecrementActionItem) value).object instanceof GetVariableActionItem) {
                if (((GetVariableActionItem) ((DecrementActionItem) value).object).name.valueEquals(name)) {
                    output.add(new PostDecrementActionItem(this, ((DecrementActionItem) value).object));
                    return;
                }
            }
        }

        GraphTargetItem ret = new SetVariableActionItem(this, name, value);

        if (value instanceof StoreRegisterActionItem) {
            StoreRegisterActionItem sr = (StoreRegisterActionItem) value;
            if (sr.define) {
                value = sr.getValue();
                ((SetVariableActionItem) ret).setValue(value);
                if (value instanceof IncrementActionItem) {
                    if (((IncrementActionItem) value).object instanceof GetVariableActionItem) {
                        if (((GetVariableActionItem) ((IncrementActionItem) value).object).name.valueEquals(name)) {
                            ret = new PreIncrementActionItem(this, ((IncrementActionItem) value).object);
                        }
                    }
                } else if (value instanceof DecrementActionItem) {
                    if (((DecrementActionItem) value).object instanceof GetVariableActionItem) {
                        if (((GetVariableActionItem) ((DecrementActionItem) value).object).name.valueEquals(name)) {
                            ret = new PreDecrementActionItem(this, ((DecrementActionItem) value).object);
                        }
                    }
                } else {
                    sr.temporary = true;
                    ((SetVariableActionItem) ret).setValue(sr);
                }

                variables.put("__register" + sr.register.number, new TemporaryRegister(sr.register.number, ret));
                return;
            }
        }

        output.add(ret);
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }

    @Override
    public String getVariableName(TranslateStack stack, ConstantPool cpool) {
        if (stack.size() < 2) {
            return null;
        }
        return stack.get(stack.size() - 2).toStringNoQuotes(LocalData.create(cpool));
    }
}
