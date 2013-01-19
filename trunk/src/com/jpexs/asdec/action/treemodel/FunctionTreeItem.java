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


package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;
import java.util.List;

public class FunctionTreeItem extends TreeItem {

   public List<TreeItem> actions;
   public List<String> constants;
   public String functionName;
   public List<String> paramNames;

   public FunctionTreeItem(Action instruction, String functionName, List<String> paramNames, List<TreeItem> actions, ConstantPool constants) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.actions = actions;
      this.constants = constants.constants;
      this.functionName = functionName;
      this.paramNames = paramNames;
   }

   @Override
   public String toString(ConstantPool constants) {
      String ret = "function";
      if (!functionName.equals("")) {
         ret += " " + functionName;
      }
      ret += "(";
      for (int p = 0; p < paramNames.size(); p++) {
         if (p > 0) {
            ret += ", ";
         }
         ret += paramNames.get(p);
      }
      ret += ")\r\n{\r\n" + Action.treeToString(actions) + "}";
      return ret;
   }
}
