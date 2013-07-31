/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.swf4;

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
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionSetProperty extends Action {

    public ActionSetProperty() {
        super(0x23, 0);
    }

    @Override
    public String toString() {
        return "SetProperty";
    }

    @Override
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem value = stack.pop().getThroughDuplicate();
        GraphTargetItem index = stack.pop().getThroughDuplicate();
        GraphTargetItem target = stack.pop().getThroughDuplicate();
        int indexInt = 0;
        if (index instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) index).value instanceof Long) {
                indexInt = (int) (long) (Long) ((DirectValueActionItem) index).value;
            }
        }
        if (value.getThroughDuplicate() instanceof IncrementActionItem) {
            GraphTargetItem obj = ((IncrementActionItem) value).object;
            if (!stack.isEmpty()) {
                if (stack.peek().valueEquals(obj)) {
                    stack.pop();
                    stack.push(new PostIncrementActionItem(this, obj));
                    return;
                }
            }
        }
        if (value.getThroughDuplicate() instanceof DecrementActionItem) {
            GraphTargetItem obj = ((DecrementActionItem) value).object;
            if (!stack.isEmpty()) {
                if (stack.peek().valueEquals(obj)) {
                    stack.pop();
                    stack.push(new PostDecrementActionItem(this, obj));
                    return;
                }
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
                }
                variables.put("__register" + sr.register.number, new TemporaryRegister(ret));
                return;
            }
        }
        output.add(ret);
    }
}
