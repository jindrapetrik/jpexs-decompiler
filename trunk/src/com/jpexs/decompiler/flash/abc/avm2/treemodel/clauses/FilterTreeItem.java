/*
 *  Copyright (C) 2012-2013 JPEXS
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
package com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.TreeItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FilterTreeItem extends TreeItem {

   public GraphTargetItem expression;
   public GraphTargetItem collection;

   public FilterTreeItem(AVM2Instruction instruction, GraphTargetItem collection, GraphTargetItem expression) {
      super(instruction, NOPRECEDENCE);
      this.expression = expression;
      this.collection = collection;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return collection.toString(constants, localRegNames, fullyQualifiedNames) + hilight(".(") + expression.toString(constants, localRegNames, fullyQualifiedNames) + hilight(")");
   }
}
