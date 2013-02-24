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
package com.jpexs.decompiler.flash.action.treemodel.clauses;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
import com.jpexs.decompiler.flash.graph.Block;
import com.jpexs.decompiler.flash.graph.ContinueItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.Loop;
import java.util.ArrayList;
import java.util.List;

public class ForInTreeItem extends LoopTreeItem implements Block {

   public TreeItem variableName;
   public TreeItem enumVariable;
   public List<TreeItem> commands;

   public ForInTreeItem(Action instruction, Loop loop, TreeItem variableName, TreeItem enumVariable, List<TreeItem> commands) {
      super(instruction, loop);
      this.variableName = variableName;
      this.enumVariable = enumVariable;
      this.commands = commands;
   }

   @Override
   public String toString(ConstantPool constants) {
      String ret = "";
      ret += "loop" + loop.id + ":\r\n";
      ret += hilight("for(") + stripQuotes(variableName) + " in " + enumVariable.toString(constants) + ")\r\n{\r\n";
      for (TreeItem ti : commands) {
         ret += ti.toString(constants) + "\r\n";
      }
      ret += hilight("}") + "\r\n";
      ret += ":loop" + loop.id;
      return ret;
   }

   @Override
   public List<ContinueItem> getContinues() {
      List<ContinueItem> ret = new ArrayList<ContinueItem>();
      for (GraphTargetItem ti : commands) {
         if (ti instanceof ContinueItem) {
            ret.add((ContinueItem) ti);
         }
         if (ti instanceof Block) {
            ret.addAll(((Block) ti).getContinues());
         }
      }
      return ret;
   }
}
