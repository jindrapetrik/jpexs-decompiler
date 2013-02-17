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
package com.jpexs.decompiler.flash.abc.avm2.treemodel;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.FilterTreeItem;
import java.util.HashMap;
import java.util.List;

public class LocalRegTreeItem extends TreeItem {

   public int regIndex;
   public TreeItem computedValue;

   public LocalRegTreeItem(AVM2Instruction instruction, int regIndex, TreeItem computedValue) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.regIndex = regIndex;
      if (computedValue == null) {
         computedValue = new UndefinedTreeItem(instruction);
      }
      this.computedValue = computedValue;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      if (computedValue instanceof FilterTreeItem) {
         return computedValue.toString(constants, localRegNames, fullyQualifiedNames);
      }
      return hilight(localRegName(localRegNames, regIndex));
   }

   @Override
   public boolean isFalse() {
      return computedValue.isFalse();
   }

   @Override
   public boolean isTrue() {
      return computedValue.isTrue();
   }

   @Override
   public TreeItem getThroughRegister() {
      return computedValue.getThroughRegister();
   }
}
