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

public class ConstructPropTreeItem extends TreeItem {

   public TreeItem object;
   public FullMultinameTreeItem propertyName;
   public List<TreeItem> args;

   public ConstructPropTreeItem(AVM2Instruction instruction, TreeItem object, FullMultinameTreeItem propertyName, List<TreeItem> args) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.object = object;
      this.propertyName = propertyName;
      this.args = args;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      String argStr = "";
      for (int a = 0; a < args.size(); a++) {
         if (a > 0) {
            argStr = argStr + ",";
         }
         argStr = argStr + args.get(a).toString(constants, localRegNames, fullyQualifiedNames);
      }
      String objstr = object.toString(constants, localRegNames, fullyQualifiedNames);
      if (!objstr.equals("")) {
         objstr += ".";
      }
      return hilight("new ") + objstr + propertyName.toString(constants, localRegNames, fullyQualifiedNames) + hilight("(") + argStr + hilight(")");

   }
}
