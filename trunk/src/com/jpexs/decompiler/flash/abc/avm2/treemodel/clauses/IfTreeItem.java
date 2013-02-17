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
package com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ContinueTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.TreeItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IfTreeItem extends TreeItem implements Block {

   public TreeItem expression;
   public List<TreeItem> onTrue;
   public List<TreeItem> onFalse;

   public IfTreeItem(AVM2Instruction instruction, TreeItem expression, List<TreeItem> onTrue, List<TreeItem> onFalse) {
      super(instruction, NOPRECEDENCE);
      this.expression = expression;
      this.onTrue = onTrue;
      this.onFalse = onFalse;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      String ret;
      ret = hilight("if(") + expression.toString(constants, localRegNames, fullyQualifiedNames) + hilight(")") + "\r\n{\r\n";
      for (TreeItem ti : onTrue) {
         ret += ti.toStringSemicoloned(constants, localRegNames, fullyQualifiedNames) + "\r\n";
      }
      ret += hilight("}");
      if (onFalse.size() > 0) {
         ret += "\r\n" + hilight("else") + "\r\n" + hilight("{") + "\r\n";
         for (TreeItem ti : onFalse) {
            ret += ti.toStringSemicoloned(constants, localRegNames, fullyQualifiedNames) + "\r\n";
         }
         ret += hilight("}");
      }
      return ret;
   }

   @Override
   public boolean needsSemicolon() {
      return false;
   }

   public List<ContinueTreeItem> getContinues() {
      List<ContinueTreeItem> ret = new ArrayList<ContinueTreeItem>();
      for (TreeItem ti : onTrue) {
         if (ti instanceof ContinueTreeItem) {
            ret.add((ContinueTreeItem) ti);
         }
         if (ti instanceof Block) {
            ret.addAll(((Block) ti).getContinues());
         }
      }
      for (TreeItem ti : onFalse) {
         if (ti instanceof ContinueTreeItem) {
            ret.add((ContinueTreeItem) ti);
         }
         if (ti instanceof Block) {
            ret.addAll(((Block) ti).getContinues());
         }
      }
      return ret;
   }
}
