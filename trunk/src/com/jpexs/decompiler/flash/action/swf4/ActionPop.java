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
import com.jpexs.decompiler.flash.action.treemodel.CallFunctionTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.CallMethodTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.NewMethodTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PopTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.SetTypeTreeItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionPop extends Action {

    public ActionPop() {
        super(0x17, 0);
    }

    @Override
    public String toString() {
        return "Pop";
    }

    @Override
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        if (stack.isEmpty()) {
            return;
        }
        GraphTargetItem val = stack.pop();
        if ((val instanceof CallFunctionTreeItem) || (val instanceof CallMethodTreeItem) || (val instanceof NewMethodTreeItem)) {
            output.add(val);
        } else if (val instanceof SetTypeTreeItem) {
            output.add(val);
        } else {
            output.add(new PopTreeItem(this));
        }
        /*if (!(val instanceof DirectValueTreeItem)) {
         output.add(new VoidTreeItem(this, val));
         }*/
    }
}
