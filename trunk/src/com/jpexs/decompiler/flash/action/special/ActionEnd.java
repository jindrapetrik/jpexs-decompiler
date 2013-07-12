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
package com.jpexs.decompiler.flash.action.special;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionEnd extends Action {

    public ActionEnd() {
        super(0, 0);
        setIgnored(true, 0);
    }

    @Override
    public String toString() {
        return "End";
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public void translate(Stack<com.jpexs.decompiler.flash.graph.GraphTargetItem> stack, List<com.jpexs.decompiler.flash.graph.GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        //output.add(new SimpleActionTreeItem(this, "end()"));
    }
}
