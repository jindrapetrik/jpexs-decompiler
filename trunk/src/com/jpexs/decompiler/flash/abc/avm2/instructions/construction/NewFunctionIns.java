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
package com.jpexs.decompiler.flash.abc.avm2.instructions.construction;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NewFunctionTreeItem;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewFunctionIns extends InstructionDefinition {

   public NewFunctionIns() {
      super(0x40, "newfunction", new int[]{AVM2Code.DAT_METHOD_INDEX});
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      int methodIndex = ins.operands[0];
      MethodBody mybody = abc.findBody(methodIndex);
      String bodyStr = "";
      String paramStr = "";
      if (mybody != null) {
         try{
         bodyStr = Highlighting.hilighMethodEnd() + mybody.toString("", false, isStatic, classIndex, abc, constants, method_info, new Stack<GraphTargetItem>()/*scopeStack*/, false, true, fullyQualifiedNames, null) + Highlighting.hilighMethodBegin(body.method_info);
         }catch(Exception ex){
            Logger.getLogger(NewFunctionIns.class.getName()).log(Level.SEVERE, "error during newfunction", ex);
         }
         paramStr = method_info[methodIndex].getParamStr(constants, mybody, abc, fullyQualifiedNames);
      }

      stack.push(new NewFunctionTreeItem(ins, "", paramStr, method_info[methodIndex].getReturnTypeStr(constants, fullyQualifiedNames), bodyStr));
   }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return 1;
   }
}
