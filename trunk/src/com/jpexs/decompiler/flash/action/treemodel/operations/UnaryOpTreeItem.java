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
package com.jpexs.decompiler.flash.action.treemodel.operations;

import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItemPos;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.List;

public abstract class UnaryOpTreeItem extends TreeItem {

   public GraphTargetItem value;
   public String operator;

   public UnaryOpTreeItem(GraphSourceItem instruction, int precedence, GraphTargetItem value, String operator) {
      super(instruction, precedence);
      this.value = value;
      this.operator = operator;
   }

   @Override
   public String toString(ConstantPool constants) {
      String s = value.toString(constants);
      if (value.precedence > precedence) {
         s = "(" + s + ")";
      }
      return hilight(operator) + s;
   }

   @Override
   public boolean isCompileTime() {
      return value.isCompileTime();
   }

   @Override
   public List<GraphSourceItemPos> getNeededSources() {
      List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
      ret.addAll(value.getNeededSources());
      return ret;
   }
}
