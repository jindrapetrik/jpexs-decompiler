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

import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.List;

public abstract class TreeItem extends GraphTargetItem {

   public TreeItem(GraphSourceItem instruction, int precedence) {
      super(instruction, precedence);
   }

   public abstract String toString(ConstantPool constants);

   @Override
   public String toString() {
      ConstantPool c = null;
      return toString(c);
   }

   protected boolean isEmptyString(GraphTargetItem target) {
      if (target instanceof DirectValueTreeItem) {
         if (((DirectValueTreeItem) target).value instanceof String) {

            if (((DirectValueTreeItem) target).value.equals("")) {
               return true;
            }
         }
      }
      return false;
   }

   protected String stripQuotes(GraphTargetItem target) {
      if (target instanceof DirectValueTreeItem) {
         if (((DirectValueTreeItem) target).value instanceof String) {
            return (String) ((DirectValueTreeItem) target).hilight((String) ((DirectValueTreeItem) target).value);
         }
      }
      if (target == null) {
         return "";
      } else {
         return target.toString();
      }
   }

   @Override
   public String toString(List localData) {
      if (localData.isEmpty()) {
         ConstantPool c = null;
         return toString(c);
      }
      return toString((ConstantPool) localData.get(0));
   }
}
