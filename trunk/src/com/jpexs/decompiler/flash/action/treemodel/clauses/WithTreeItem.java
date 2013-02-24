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
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.ArrayList;
import java.util.List;

public class WithTreeItem extends TreeItem {

   public GraphTargetItem scope;
   public List<GraphTargetItem> items;

   public WithTreeItem(Action instruction, GraphTargetItem scope, List<GraphTargetItem> items) {
      super(instruction, NOPRECEDENCE);
      this.scope = scope;
      this.items = items;
   }

   public WithTreeItem(Action instruction, TreeItem scope) {
      super(instruction, NOPRECEDENCE);
      this.scope = scope;
      this.items = new ArrayList<GraphTargetItem>();
   }

   @Override
   public String toString(ConstantPool constants) {
      String ret;
      List localData = new ArrayList();
      localData.add(constants);
      ret = hilight("with(") + scope.toString(localData) + hilight(")\r\n{\r\n");
      for (GraphTargetItem ti : items) {
         ret += ti.toString(localData) + "\r\n";
      }
      ret += hilight("}");
      return ret;
   }
}
