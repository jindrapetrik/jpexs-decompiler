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


package com.jpexs.asdec.abc.avm2.instructions;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.helpers.Highlighting;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class InstructionDefinition {

   protected String hilighOffset(String text, long offset) {
      return Highlighting.hilighOffset(text, offset);
   }
   public int operands[];
   public String instructionName = "";
   public int instructionCode = 0;

   public InstructionDefinition(int instructionCode, String instructionName, int operands[]) {
      this.instructionCode = instructionCode;
      this.instructionName = instructionName;
      this.operands = operands;
   }

   @Override
   public String toString() {
      String s = instructionName;
      for (int i = 0; i < operands.length; i++) {
         if ((operands[i] & 0xff00) == AVM2Code.OPT_U30) {
            s += " U30";
         }
         if ((operands[i] & 0xff00) == AVM2Code.OPT_U8) {
            s += " U8";
         }
         if ((operands[i] & 0xff00) == AVM2Code.OPT_BYTE) {
            s += " BYTE";
         }
         if ((operands[i] & 0xff00) == AVM2Code.OPT_S24) {
            s += " S24";
         }
         if ((operands[i] & 0xff00) == AVM2Code.OPT_CASE_OFFSETS) {
            s += " U30 S24,[S24]...";
         }
      }
      return s;
   }

   public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
      throw new UnsupportedOperationException("Instruction " + instructionName + " not implemented");
   }

   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
   }

   protected FullMultinameTreeItem resolveMultiname(Stack<TreeItem> stack, ConstantPool constants, int multinameIndex, AVM2Instruction ins) {
      TreeItem ns = null;
      TreeItem name = null;
      if (constants.constant_multiname[multinameIndex].needsName()) {
         name = (TreeItem) stack.pop();
      }
      if (constants.constant_multiname[multinameIndex].needsNs()) {
         ns = (TreeItem) stack.pop();
      }
      return new FullMultinameTreeItem(ins, multinameIndex, name, ns);
   }

   protected int resolvedCount(ConstantPool constants, int multinameIndex) {
      int pos = 0;
      if (constants.constant_multiname[multinameIndex].needsNs()) {
         pos++;
      }
      if (constants.constant_multiname[multinameIndex].needsName()) {
         pos++;
      }
      return pos;

   }

   protected String resolveMultinameNoPop(int pos, Stack<TreeItem> stack, ConstantPool constants, int multinameIndex, AVM2Instruction ins, List<String> fullyQualifiedNames) {
      String ns = "";
      String name;
      if (constants.constant_multiname[multinameIndex].needsNs()) {
         ns = "[" + stack.get(pos) + "]";
         pos++;
      }
      if (constants.constant_multiname[multinameIndex].needsName()) {
         name = stack.get(pos).toString();
      } else {
         name = hilighOffset(constants.constant_multiname[multinameIndex].getName(constants, fullyQualifiedNames), ins.offset);
      }
      return name + ns;
   }

   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return 0;
   }

   public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
      return 0;
   }
}
