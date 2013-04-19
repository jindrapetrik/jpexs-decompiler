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
import com.jpexs.decompiler.flash.action.treemodel.DecrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.IncrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PostDecrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PostIncrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.SetPropertyTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StoreRegisterTreeItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
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
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        GraphTargetItem value = stack.pop();
        GraphTargetItem index = stack.pop();
        GraphTargetItem target = stack.pop();
        int indexInt = 0;
        if (index instanceof DirectValueTreeItem) {
            if (((DirectValueTreeItem) index).value instanceof Long) {
                indexInt = (int) (long) (Long) ((DirectValueTreeItem) index).value;
            }
        }
        if (value instanceof IncrementTreeItem) {
            GraphTargetItem obj = ((IncrementTreeItem) value).object;
            if (!stack.isEmpty()) {
                if (stack.peek().equals(obj)) {
                    stack.pop();
                    stack.push(new PostIncrementTreeItem(this, obj));
                    return;
                }
            }
        }
        if (value instanceof DecrementTreeItem) {
            GraphTargetItem obj = ((DecrementTreeItem) value).object;
            if (!stack.isEmpty()) {
                if (stack.peek().equals(obj)) {
                    stack.pop();
                    stack.push(new PostDecrementTreeItem(this, obj));
                    return;
                }
            }
        }
        if (value instanceof StoreRegisterTreeItem) {
            ((StoreRegisterTreeItem) value).define = false;
        }
        output.add(new SetPropertyTreeItem(this, target, indexInt, value));
    }
}
