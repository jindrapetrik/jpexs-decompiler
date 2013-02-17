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
import java.util.HashMap;
import java.util.List;

public class SetGlobalSlotTreeItem extends TreeItem {

   public int slotId;
   public TreeItem value;

   public SetGlobalSlotTreeItem(AVM2Instruction instruction, int slotId, TreeItem value) {
      super(instruction, PRECEDENCE_ASSIGMENT);
      this.slotId = slotId;
      this.value = value;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return hilight("setglobalslot(" + slotId + ",") + value.toString(constants, localRegNames, fullyQualifiedNames) + hilight(")");
   }
}
