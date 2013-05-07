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
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StartDragTreeItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionStartDrag extends Action {

    public ActionStartDrag() {
        super(0x27, 0);
    }

    @Override
    public String toString() {
        return "StartDrag";
    }

    @Override
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        GraphTargetItem target = stack.pop();
        GraphTargetItem lockCenter = stack.pop();
        GraphTargetItem constrain = stack.pop();

        boolean hasConstrains = true;
        if (constrain instanceof DirectValueTreeItem) {
            if (Double.compare(constrain.toNumber(), 0) == 0) {
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
        output.add(new StartDragTreeItem(this, target, lockCenter, constrain, x1, y1, x2, y2));
    }
}
