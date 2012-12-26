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
package com.jpexs.asdec.abc.avm2.instructions.construction;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.NewFunctionTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.helpers.Highlighting;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class NewFunctionIns extends InstructionDefinition {

   public NewFunctionIns() {
      super(0x40, "newfunction", new int[]{AVM2Code.DAT_METHOD_INDEX});
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc, HashMap<Integer, String> localRegNames) {
      int methodIndex = ins.operands[0];
      MethodBody mybody = abc.findBody(methodIndex);
      String bodyStr = "";
      String paramStr = "";
      if (mybody != null) {
         bodyStr = Highlighting.hilighMethodEnd() + mybody.toString(false, isStatic, classIndex, abc, constants, method_info,scopeStack, true) + Highlighting.hilighMethodBegin(body.method_info);
         paramStr = method_info[methodIndex].getParamStr(constants, mybody, abc);
      }
      stack.push(new NewFunctionTreeItem(ins, paramStr, method_info[methodIndex].getReturnTypeStr(constants), bodyStr));
   }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return 1;
   }
}
