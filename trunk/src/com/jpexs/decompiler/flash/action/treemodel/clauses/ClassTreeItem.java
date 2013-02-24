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
package com.jpexs.decompiler.flash.action.treemodel.clauses;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.FunctionTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
import com.jpexs.decompiler.flash.graph.Block;
import com.jpexs.decompiler.flash.graph.ContinueItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassTreeItem extends TreeItem implements Block {

   public List<FunctionTreeItem> functions;
   public List<FunctionTreeItem> staticFunctions;
   public GraphTargetItem extendsOp;
   public List<GraphTargetItem> implementsOp;
   public GraphTargetItem className;
   public HashMap<GraphTargetItem, GraphTargetItem> vars;
   public HashMap<GraphTargetItem, GraphTargetItem> staticVars;

   public ClassTreeItem(GraphTargetItem className, GraphTargetItem extendsOp, List<GraphTargetItem> implementsOp, List<FunctionTreeItem> functions, HashMap<GraphTargetItem, GraphTargetItem> vars, List<FunctionTreeItem> staticFunctions, HashMap<GraphTargetItem, GraphTargetItem> staticVars) {
      super(null, NOPRECEDENCE);
      this.className = className;
      this.functions = functions;
      this.vars = vars;
      this.extendsOp = extendsOp;
      this.implementsOp = implementsOp;
      this.staticFunctions = staticFunctions;
      this.staticVars = staticVars;
   }

   @Override
   public String toString(ConstantPool constants) {
      String ret;
      ret = hilight("class ") + className.toStringNoQuotes(Helper.toList(constants));
      if (extendsOp != null) {
         ret += hilight(" extends ") + extendsOp.toStringNoQuotes(Helper.toList(constants));
      }
      if (!implementsOp.isEmpty()) {
         ret += hilight(" implements ");
         boolean first = true;
         for (GraphTargetItem t : implementsOp) {
            if (!first) {
               ret += ", ";
            }
            first = false;
            ret += Action.getWithoutGlobal(t).toString(constants);
         }
      }
      ret += "\r\n{\r\n";
      for (FunctionTreeItem f : functions) {
         ret += f.toString(constants) + "\r\n";
      }
      for (FunctionTreeItem f : staticFunctions) {
         ret += "static " + f.toString(constants) + "\r\n";
      }
      for (GraphTargetItem v : vars.keySet()) {
         ret += "var " + v.toStringNoQuotes(constants) + " = " + vars.get(v).toStringNoQuotes(constants) + ";\r\n";
      }
      for (GraphTargetItem v : staticVars.keySet()) {
         ret += "static var " + v.toStringNoQuotes(constants) + " = " + staticVars.get(v).toStringNoQuotes(constants) + ";\r\n";
      }
      ret += "}\r\n";
      return ret;
   }

   @Override
   public List<ContinueItem> getContinues() {
      List<ContinueItem> ret = new ArrayList<ContinueItem>();
      return ret;
   }
}
