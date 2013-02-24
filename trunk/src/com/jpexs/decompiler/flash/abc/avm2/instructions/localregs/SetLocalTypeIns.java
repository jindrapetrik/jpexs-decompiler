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
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.DecrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.FindPropertyTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.IncrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.LocalRegTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NewActivationTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.PostDecrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.PostIncrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.SetLocalTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.TreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.operations.PreDecrementTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.operations.PreIncrementTreeItem;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public abstract class SetLocalTypeIns extends InstructionDefinition implements SetTypeIns {

   public SetLocalTypeIns(int instructionCode, String instructionName, int[] operands) {
      super(instructionCode, instructionName, operands);
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      int regId = getRegisterId(ins);
      TreeItem value = (TreeItem) stack.pop();
      localRegs.put(regId, value);
      if (value instanceof NewActivationTreeItem) {
         return;
      }
      if (value instanceof FindPropertyTreeItem) {
         return;
      }
      if (value.getNotCoerced() instanceof IncrementTreeItem) {
         GraphTargetItem inside = ((IncrementTreeItem) value.getNotCoerced()).object.getNotCoerced();
         if (inside instanceof LocalRegTreeItem) {
            if (((LocalRegTreeItem) inside).regIndex == regId) {
               if (stack.size() > 0) {
                  GraphTargetItem top = stack.peek().getNotCoerced();
                  if (top == inside) {
                     stack.pop();
                     stack.push(new PostIncrementTreeItem(ins, inside));
                  } else if ((top instanceof IncrementTreeItem) && (((IncrementTreeItem) top).object == inside)) {
                     stack.pop();
                     stack.push(new PreIncrementTreeItem(ins, inside));
                  } else {
                     output.add(new PostIncrementTreeItem(ins, inside));
                  }
               } else {
                  output.add(new PostIncrementTreeItem(ins, inside));
               }
               return;
            }
         }
      }

      if (value.getNotCoerced() instanceof DecrementTreeItem) {
         GraphTargetItem inside = ((DecrementTreeItem) value.getNotCoerced()).object.getNotCoerced();
         if (inside instanceof LocalRegTreeItem) {
            if (((LocalRegTreeItem) inside).regIndex == regId) {
               if (stack.size() > 0) {
                  GraphTargetItem top = stack.peek().getNotCoerced();
                  if (top == inside) {
                     stack.pop();
                     stack.push(new PostDecrementTreeItem(ins, inside));
                  } else if ((top instanceof DecrementTreeItem) && (((DecrementTreeItem) top).object == inside)) {
                     stack.pop();
                     stack.push(new PreDecrementTreeItem(ins, inside));
                  } else {
                     output.add(new PostDecrementTreeItem(ins, inside));
                  }
               } else {
                  output.add(new PostDecrementTreeItem(ins, inside));
               }
               return;
            }
         }
      }

      //if(val.startsWith("catchscope ")) return;
      //if(val.startsWith("newactivation()")) return;
      output.add(new SetLocalTreeItem(ins, regId, value));
   }

   public String getObject(Stack<TreeItem> stack, ABC abc, AVM2Instruction ins, List<TreeItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return TreeItem.localRegName(localRegNames, getRegisterId(ins));
   }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return -1;
   }

   public abstract int getRegisterId(AVM2Instruction ins);
}
