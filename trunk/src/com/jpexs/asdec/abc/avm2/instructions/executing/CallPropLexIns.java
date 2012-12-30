/*
 *  Copyright (C) 2010-2011 JPEXS
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
package com.jpexs.asdec.abc.avm2.instructions.executing;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.CallPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class CallPropLexIns extends CallPropertyIns {

   public CallPropLexIns() {
      instructionName = "callproplex";
      instructionCode = 0x4c;
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      int multinameIndex = ins.operands[0];
      int argCount = ins.operands[1];
      List<TreeItem> args = new ArrayList<TreeItem>();
      for (int a = 0; a < argCount; a++) {
         args.add(0, (TreeItem) stack.pop());
      }
      FullMultinameTreeItem multiname = resolveMultiname(stack, constants, multinameIndex, ins);
      TreeItem receiver = (TreeItem) stack.pop();

      stack.push(new CallPropertyTreeItem(ins, false, receiver, multiname, args));
   }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      int ret = -ins.operands[1] - 1 + 1;
      int multinameIndex = ins.operands[0];
      if (abc.constants.constant_multiname[multinameIndex].needsName()) {
         ret--;
      }
      if (abc.constants.constant_multiname[multinameIndex].needsNs()) {
         ret--;
      }
      return ret;
   }
}
