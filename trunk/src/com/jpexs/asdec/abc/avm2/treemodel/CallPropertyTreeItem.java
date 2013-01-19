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


package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import java.util.HashMap;
import java.util.List;

public class CallPropertyTreeItem extends TreeItem {

   public TreeItem receiver;
   public FullMultinameTreeItem propertyName;
   public List<TreeItem> arguments;
   public boolean isVoid;

   public CallPropertyTreeItem(AVM2Instruction instruction, boolean isVoid, TreeItem receiver, FullMultinameTreeItem propertyName, List<TreeItem> arguments) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.receiver = receiver;
      this.propertyName = propertyName;
      this.arguments = arguments;
      this.isVoid = isVoid;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      String args = "";
      for (int a = 0; a < arguments.size(); a++) {
         if (a > 0) {
            args = args + hilight(",");
         }
         args = args + arguments.get(a).toString(constants, localRegNames, fullyQualifiedNames);
      }
      return formatProperty(constants, receiver, propertyName, localRegNames, fullyQualifiedNames) + hilight("(") + args + hilight(")");
   }
}
