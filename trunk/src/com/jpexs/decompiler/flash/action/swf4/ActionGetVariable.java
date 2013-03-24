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
import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.GetVariableTreeItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionGetVariable extends Action {

   public ActionGetVariable() {
      super(0x1C, 0);
   }

   @Override
   public String toString() {
      return "GetVariable";
   }

   @Override
   public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
      GraphTargetItem name = stack.pop();
      GraphTargetItem computedVal = variables.get(Highlighting.stripHilights(name.toStringNoQuotes((ConstantPool)null)));
      GetVariableTreeItem gvt = new GetVariableTreeItem(this, name);
      gvt.setComputedValue(computedVal);
      stack.push(gvt);
   }
}
