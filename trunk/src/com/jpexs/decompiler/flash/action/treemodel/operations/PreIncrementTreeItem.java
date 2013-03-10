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

import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.UnaryOpItem;

public class PreIncrementTreeItem extends UnaryOpItem {

   public PreIncrementTreeItem(GraphSourceItem instruction, TreeItem object) {
      super(instruction, PRECEDENCE_UNARY, object, "++");
   }

   @Override
   public double toNumber() {
      return value.toNumber() + 1;
   }
}
