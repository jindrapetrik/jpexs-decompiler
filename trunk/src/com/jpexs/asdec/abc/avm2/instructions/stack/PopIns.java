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
package com.jpexs.asdec.abc.avm2.instructions.stack;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.*;
import com.jpexs.asdec.abc.avm2.treemodel.clauses.AssignmentTreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class PopIns extends InstructionDefinition {

   public PopIns() {
      super(0x29, "pop", new int[]{});
   }

   @Override
   public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
      lda.operandStack.pop();
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      if (stack.size() > 0) {
         TreeItem top = stack.pop();
         if (top instanceof CallPropertyTreeItem) {
            output.add(top);
         } else if (top instanceof CallSuperTreeItem) {
            output.add(top);
         } else if (top instanceof CallStaticTreeItem) {
            output.add(top);
         } else if (top instanceof CallMethodTreeItem) {
            output.add(top);
         } else if (top instanceof CallTreeItem) {
            output.add(top);
         } else if (top instanceof AssignmentTreeItem) {
            output.add(top);
         }
      }
   }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return -1;
   }
}
