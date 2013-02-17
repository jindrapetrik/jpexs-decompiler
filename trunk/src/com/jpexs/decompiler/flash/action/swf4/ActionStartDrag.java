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
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
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
   public void translate(Stack<TreeItem> stack, List<TreeItem> output, java.util.HashMap<Integer, String> regNames) {
      TreeItem target = stack.pop();
      TreeItem lockCenter = stack.pop();
      TreeItem constrain = stack.pop();

      boolean hasConstrains = true;
      if (constrain instanceof DirectValueTreeItem) {
         if (((DirectValueTreeItem) constrain).value instanceof Long) {
            if (((long) (Long) ((DirectValueTreeItem) constrain).value) == 0) {
               hasConstrains = false;
            }
         }
         if (((DirectValueTreeItem) constrain).value instanceof Boolean) {
            if (((boolean) (Boolean) ((DirectValueTreeItem) constrain).value) == false) {
               hasConstrains = false;
            }
         }
      }
      TreeItem x1 = null;
      TreeItem y1 = null;
      TreeItem x2 = null;
      TreeItem y2 = null;
      if (hasConstrains) {
         y2 = stack.pop();
         x2 = stack.pop();
         y1 = stack.pop();
         x1 = stack.pop();
      }
      output.add(new StartDragTreeItem(this, target, lockCenter, constrain, x1, y1, x2, y2));
   }
}
