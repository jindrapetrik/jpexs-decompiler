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
import com.jpexs.decompiler.flash.action.treemodel.SetMemberTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
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
   public void translate(Stack<TreeItem> stack, List<TreeItem> output, java.util.HashMap<Integer, String> regNames) {
      TreeItem value = stack.pop();
      TreeItem objectName = stack.pop();
      TreeItem object = stack.pop();
      output.add(new SetMemberTreeItem(this, object, objectName, value));
   }
}
