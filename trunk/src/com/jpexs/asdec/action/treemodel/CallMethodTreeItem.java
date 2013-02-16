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
import com.jpexs.asdec.action.swf4.Undefined;
import java.util.List;

public class CallMethodTreeItem extends TreeItem {

   public TreeItem methodName;
   public TreeItem scriptObject;
   public List<TreeItem> arguments;

   public CallMethodTreeItem(Action instruction, TreeItem scriptObject, TreeItem methodName, List<TreeItem> arguments) {
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
            paramStr += hilight(",");
         }
         paramStr += arguments.get(t).toString(constants);
      }
      boolean blankMethod = false;
      if (methodName instanceof DirectValueTreeItem) {
         if (((DirectValueTreeItem) methodName).value instanceof Undefined) {
            blankMethod = true;
         }
         if (((DirectValueTreeItem) methodName).value instanceof String) {
            if (((DirectValueTreeItem) methodName).value.equals("")) {
               blankMethod = true;
            }
         }
      }
      if (blankMethod) {
         return scriptObject.toString(constants) + hilight("(") + paramStr + hilight(")");
      }
      return scriptObject.toString(constants) + hilight(".") + stripQuotes(methodName) + hilight("(") + paramStr + hilight(")");
   }

   @Override
   public List<com.jpexs.asdec.action.IgnoredPair> getNeededActions() {
      List<com.jpexs.asdec.action.IgnoredPair> ret = super.getNeededActions();
      ret.addAll(methodName.getNeededActions());
      ret.addAll(scriptObject.getNeededActions());
      for (TreeItem ti : arguments) {
         ret.addAll(ti.getNeededActions());
      }
      return ret;
   }
}
