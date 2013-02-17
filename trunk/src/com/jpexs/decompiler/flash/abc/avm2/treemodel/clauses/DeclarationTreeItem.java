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
import com.jpexs.decompiler.flash.abc.avm2.treemodel.CoerceTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ConvertTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.SetLocalTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.SetSlotTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.TreeItem;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class DeclarationTreeItem extends TreeItem {

   public TreeItem assignment;
   public String type;

   public DeclarationTreeItem(TreeItem assignment, String type) {
      super(assignment.instruction, assignment.precedence);
      this.type = type;
      this.assignment = assignment;
   }

   public DeclarationTreeItem(TreeItem assignment) {
      this(assignment, null);
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      if (assignment instanceof SetLocalTreeItem) {
         SetLocalTreeItem lti = (SetLocalTreeItem) assignment;
         String type = "*";
         if (lti.value instanceof CoerceTreeItem) {
            type = ((CoerceTreeItem) lti.value).type;
         }
         if (lti.value instanceof ConvertTreeItem) {
            type = ((ConvertTreeItem) lti.value).type;
         }
         return "var " + hilight(localRegName(localRegNames, lti.regIndex) + ":" + type + " = ") + lti.value.toString(constants, localRegNames, fullyQualifiedNames);
      }
      if (assignment instanceof SetSlotTreeItem) {
         SetSlotTreeItem ssti = (SetSlotTreeItem) assignment;
         return "var " + ssti.getName(constants, localRegNames, fullyQualifiedNames) + ":" + type + hilight(" = ") + ssti.value.toString(constants, localRegNames, fullyQualifiedNames);
      }
      return "var " + assignment.toString(constants, localRegNames, fullyQualifiedNames);
   }
}
