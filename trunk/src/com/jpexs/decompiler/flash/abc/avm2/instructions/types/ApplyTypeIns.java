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
package com.jpexs.decompiler.flash.abc.avm2.instructions.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ApplyTypeTreeItem;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.*;

public class ApplyTypeIns extends InstructionDefinition {

   public ApplyTypeIns() {
      super(0x53, "applytype", new int[]{AVM2Code.DAT_ARG_COUNT});
   }

   @Override
   public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
      int argCount = (int) ((Long) arguments.get(0)).longValue();
      List params = new ArrayList();
      for (int i = 0; i < argCount; i++) {
         params.add(lda.operandStack.pop());
      }
      Collections.reverse(params);
      //TODO: pop type and push type<params>
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      int argCount = ins.operands[0];
      List<GraphTargetItem> params = new ArrayList<GraphTargetItem>();
      for (int i = 0; i < argCount; i++) {
         params.add(0, stack.pop());
      }
      stack.push(new ApplyTypeTreeItem(ins, stack.pop(), params));
   }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return -ins.operands[0] + 1;
   }
}
