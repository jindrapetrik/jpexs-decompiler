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
package com.jpexs.decompiler.flash.action.treemodel;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.swf4.Undefined;
import java.util.List;

public class NewMethodTreeItem extends TreeItem {

   public TreeItem methodName;
   public TreeItem scriptObject;
   public List<TreeItem> arguments;

   public NewMethodTreeItem(Action instruction, TreeItem scriptObject, TreeItem methodName, List<TreeItem> arguments) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.methodName = methodName;
      this.arguments = arguments;
      this.scriptObject = scriptObject;
   }

   @Override
   public String toString(ConstantPool constants) {
      String paramStr = "";
      for (int t = 0; t < arguments.size(); t++) {
         if (t > 0) {
            paramStr += ",";
         }
         paramStr += arguments.get(t).toString(constants);
      }
      boolean blankMethod = false;
      String methodNameStr = "";
      if (methodName instanceof DirectValueTreeItem) {
         if (((DirectValueTreeItem) methodName).value instanceof Undefined) {
            blankMethod = true;
         } else if (((DirectValueTreeItem) methodName).value instanceof String) {
            if (((DirectValueTreeItem) methodName).value.equals("")) {
               blankMethod = true;
            }
            methodNameStr = ((DirectValueTreeItem) methodName).toStringNoQuotes(constants);
         } else {
            methodNameStr = methodName.toString(constants);
         }
      } else {
         methodNameStr = methodName.toString(constants);
      }
      if (blankMethod) {
         return scriptObject.toString(constants) + "(" + paramStr + ")";
      }
      return hilight("new ") + scriptObject.toString(constants) + hilight(".") + methodNameStr + hilight("(") + paramStr + hilight(")");
   }

   @Override
   public List<com.jpexs.decompiler.flash.action.IgnoredPair> getNeededActions() {
      List<com.jpexs.decompiler.flash.action.IgnoredPair> ret = super.getNeededActions();
      ret.addAll(methodName.getNeededActions());
      ret.addAll(scriptObject.getNeededActions());
      for (TreeItem ti : arguments) {
         ret.addAll(ti.getNeededActions());
      }
      return ret;
   }
}
