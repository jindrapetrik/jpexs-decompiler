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
package com.jpexs.decompiler.flash.action.swf5;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.treemodel.DecrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetMemberTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.IncrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PostDecrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PostIncrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.SetMemberTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StoreRegisterTreeItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionSetMember extends Action {

    public ActionSetMember() {
        super(0x4F, 0);
    }

    @Override
    public String toString() {
        return "SetMember";
    }

    @Override
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        GraphTargetItem value = stack.pop();
        GraphTargetItem memberName = stack.pop();
        GraphTargetItem object = stack.pop();
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

        if (value instanceof IncrementTreeItem) {
            if (((IncrementTreeItem) value).object instanceof GetMemberTreeItem) {
                if (((GetMemberTreeItem) ((IncrementTreeItem) value).object).object.equals(object)) {
                    if (((GetMemberTreeItem) ((IncrementTreeItem) value).object).memberName.equals(memberName)) {
                        output.add(new PostIncrementTreeItem(this, ((IncrementTreeItem) value).object));
                        return;
                    }
                }
            }
        }
        if (value instanceof DecrementTreeItem) {
            if (((DecrementTreeItem) value).object instanceof GetMemberTreeItem) {
                if (((GetMemberTreeItem) ((DecrementTreeItem) value).object).object.equals(object)) {
                    if (((GetMemberTreeItem) ((DecrementTreeItem) value).object).memberName.equals(memberName)) {
                        output.add(new PostDecrementTreeItem(this, ((DecrementTreeItem) value).object));
                        return;
                    }
                }
            }
        }
        if (value instanceof StoreRegisterTreeItem) {
            ((StoreRegisterTreeItem) value).define = false;
        }
        output.add(new SetMemberTreeItem(this, object, memberName, value));
    }
}
