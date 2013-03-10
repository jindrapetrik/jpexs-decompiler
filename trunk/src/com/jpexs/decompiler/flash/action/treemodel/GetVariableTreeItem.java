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

public class GetVariableTreeItem extends TreeItem {

   public GraphTargetItem name;
   public GraphTargetItem computedValue;

   public GetVariableTreeItem(GraphSourceItem instruction, GraphTargetItem value) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.name = value;
   }

   @Override
   public String toString(ConstantPool constants) {
      //return ""+computedValue.toNumber(); 
      return stripQuotes(name);
   }

   @Override
   public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
      List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
      ret.addAll(name.getNeededSources());
      return ret;
   }

   @Override
   public boolean isVariableComputed() {
      return true;
   }

   @Override
   public boolean isCompileTime() {
      if (computedValue == null) {
         return false;
      }
      return computedValue.isCompileTime();
   }

   @Override
   public boolean toBoolean() {
      if (computedValue == null) {
         return false;
      }
      return computedValue.toBoolean();
   }

   @Override
   public double toNumber() {
      if (computedValue == null) {
         return 0;
      }
      return computedValue.toNumber();
   }
}
