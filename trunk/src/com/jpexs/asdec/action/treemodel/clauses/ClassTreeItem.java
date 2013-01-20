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
package com.jpexs.asdec.action.treemodel.clauses;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ContinueTreeItem;
import com.jpexs.asdec.action.treemodel.FunctionTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassTreeItem extends TreeItem implements Block {

   public List<FunctionTreeItem> functions;
   public TreeItem extendsOp;
   public List<TreeItem> implementsOp;
   public TreeItem className;
   public HashMap<TreeItem, TreeItem> vars;

   public ClassTreeItem(TreeItem className, TreeItem extendsOp, List<TreeItem> implementsOp, List<FunctionTreeItem> functions, HashMap<TreeItem, TreeItem> vars) {
      super(null, NOPRECEDENCE);
      this.className = className;
      this.functions = functions;
      this.vars = vars;
      this.extendsOp = extendsOp;
      this.implementsOp = implementsOp;
   }

   @Override
   public String toString(ConstantPool constants) {
      String ret;
      ret = "class " + className.toStringNoQuotes(constants);
      if (extendsOp != null) {
         ret += " extends " + extendsOp.toString(constants);
      }
      if (!implementsOp.isEmpty()) {
         ret += " implements ";
         boolean first = true;
         for (TreeItem t : implementsOp) {
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
      for (TreeItem v : vars.keySet()) {
         ret += "var " + v.toStringNoQuotes(constants) + " = " + vars.get(v).toStringNoQuotes(constants)+";\r\n";
      }
      ret += "}\r\n";
      return ret;
   }

   @Override
   public List<ContinueTreeItem> getContinues() {
      List<ContinueTreeItem> ret = new ArrayList<ContinueTreeItem>();
      return ret;
   }
}
