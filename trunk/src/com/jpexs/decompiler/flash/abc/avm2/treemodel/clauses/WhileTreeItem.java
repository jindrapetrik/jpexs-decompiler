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

public class WhileTreeItem extends LoopTreeItem implements Block {

   public TreeItem expression;
   public List<TreeItem> commands;

   public WhileTreeItem(AVM2Instruction instruction, long loopId, int loopContinue, TreeItem expression, List<TreeItem> commands) {
      super(instruction, loopId, loopContinue);
      this.expression = expression;
      this.commands = commands;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      String ret = "";
      ret += "loop" + loopId + ":\r\n";
      ret += hilight("while(") + expression.toString(constants, localRegNames, fullyQualifiedNames) + hilight(")") + "\r\n{\r\n";
      for (TreeItem ti : commands) {
         ret += ti.toStringSemicoloned(constants, localRegNames, fullyQualifiedNames) + "\r\n";
      }
      ret += hilight("}") + "\r\n";
      ret += ":loop" + loopId;
      return ret;
   }

   public List<ContinueTreeItem> getContinues() {
      List<ContinueTreeItem> ret = new ArrayList<ContinueTreeItem>();
      for (TreeItem ti : commands) {
         if (ti instanceof ContinueTreeItem) {
            ret.add((ContinueTreeItem) ti);
         }
         if (ti instanceof Block) {
            ret.addAll(((Block) ti).getContinues());
         }
      }
      return ret;
   }

   @Override
   public boolean needsSemicolon() {
      return false;
   }
}
