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
package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForEachInTreeItem extends LoopTreeItem implements Block {

   public InTreeItem expression;
   public List<TreeItem> commands;

   public ForEachInTreeItem(AVM2Instruction instruction, int loopBreak, int loopContinue, InTreeItem expression, List<TreeItem> commands) {
      super(instruction, loopBreak, loopContinue);
      if (!commands.isEmpty()) {
         TreeItem firstAssign = commands.get(0);
         if (firstAssign instanceof SetTypeTreeItem) {
            if (expression.object instanceof LocalRegTreeItem) {
               if (((SetTypeTreeItem) firstAssign).getValue().getNotCoerced() instanceof LocalRegTreeItem) {
                  if (((LocalRegTreeItem) ((SetTypeTreeItem) firstAssign).getValue().getNotCoerced()).regIndex == ((LocalRegTreeItem) expression.object).regIndex) {
                     commands.remove(0);
                     expression.object = ((SetTypeTreeItem) firstAssign).getObject();
                  }
               }

            }
            //locAssign.
         }
      }
      this.expression = expression;
      this.commands = commands;
   }

   @Override
   public boolean needsSemicolon() {
      return false;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      String ret = "";
      ret += "loop" + loopBreak + ":\r\n";
      ret += hilight("for each (") + expression.toString(constants, localRegNames, fullyQualifiedNames) + ")\r\n{\r\n";
      for (TreeItem ti : commands) {
         ret += ti.toStringSemicoloned(constants, localRegNames, fullyQualifiedNames) + "\r\n";
      }
      ret += hilight("}") + "\r\n";
      ret += ":loop" + loopBreak;
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
}
