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
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class NextValueTreeItem extends TreeItem {

   GraphTargetItem index;
   GraphTargetItem obj;

   public NextValueTreeItem(AVM2Instruction instruction, GraphTargetItem index, GraphTargetItem obj) {
      super(instruction, NOPRECEDENCE);
      this.index = index;
      this.obj = obj;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return "nextValue(" + index.toString(Helper.toList(constants, localRegNames, fullyQualifiedNames)) + "," + obj.toString(Helper.toList(constants, localRegNames, fullyQualifiedNames)) + ")";
   }
}
