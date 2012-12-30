/*
 *  Copyright (C) 2010-2011 JPEXS
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
package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.clauses.AssignmentTreeItem;
import java.util.HashMap;
import java.util.List;

public class SetLocalTreeItem extends TreeItem implements SetTypeTreeItem, AssignmentTreeItem {

   public int regIndex;
   public TreeItem value;

   public SetLocalTreeItem(AVM2Instruction instruction, int regIndex, TreeItem value) {
      super(instruction, PRECEDENCE_ASSIGMENT);
      this.regIndex = regIndex;
      this.value = value;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return hilight(localRegName(localRegNames, regIndex) + "=") + value.toString(constants, localRegNames, fullyQualifiedNames);
   }

   public TreeItem getObject() {
      return new LocalRegTreeItem(instruction, regIndex, null);
   }

   public TreeItem getValue() {
      return value;
   }
}
