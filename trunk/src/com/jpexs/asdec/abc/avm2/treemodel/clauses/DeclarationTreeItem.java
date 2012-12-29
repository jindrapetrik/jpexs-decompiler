/*
 * Copyright (C) 2012 Jindra
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.treemodel.CoerceTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.ConvertTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.LocalRegTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.NewActivationTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.SetLocalTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.SetSlotTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import java.util.HashMap;

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
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames) {
      if (assignment instanceof SetLocalTreeItem) {
         SetLocalTreeItem lti = (SetLocalTreeItem) assignment;
         String type = "*";
         if (lti.value instanceof CoerceTreeItem) {
            type = ((CoerceTreeItem) lti.value).type;
         }
         if (lti.value instanceof ConvertTreeItem) {
            type = ((ConvertTreeItem) lti.value).type;
         }
         return "var " + hilight(localRegName(localRegNames, lti.regIndex) + ":" + type + " = ") + lti.value.toString(constants, localRegNames);
      }
      if (assignment instanceof SetSlotTreeItem) {
         SetSlotTreeItem ssti = (SetSlotTreeItem) assignment;         
         return "var " + ssti.getName(constants, localRegNames) + ":" + type + hilight(" = ") + ssti.value.toString(constants, localRegNames);
      }
      return "var " + assignment.toString(constants, localRegNames);
   }
}
