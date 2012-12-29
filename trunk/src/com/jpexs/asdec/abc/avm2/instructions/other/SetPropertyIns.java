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
package com.jpexs.asdec.abc.avm2.instructions.other;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.instructions.SetTypeIns;
import com.jpexs.asdec.abc.avm2.treemodel.DecrementTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.GetPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.IncrementTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.LocalRegTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.PostDecrementTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.PostIncrementTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.SetPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.PreDecrementTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.PreIncrementTreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class SetPropertyIns extends InstructionDefinition implements SetTypeIns {

   public SetPropertyIns() {
      super(0x61, "setproperty", new int[]{AVM2Code.DAT_MULTINAME_INDEX});
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc, HashMap<Integer, String> localRegNames) {
      int multinameIndex = ins.operands[0];
      TreeItem value = (TreeItem) stack.pop();
      FullMultinameTreeItem multiname = resolveMultiname(stack, constants, multinameIndex, ins);
      TreeItem obj = (TreeItem) stack.pop();
      if (value.getThroughRegister() instanceof IncrementTreeItem) {
         TreeItem inside = ((IncrementTreeItem) value.getThroughRegister()).object.getThroughRegister().getNotCoerced();
         if (inside instanceof GetPropertyTreeItem) {
            GetPropertyTreeItem insideProp = ((GetPropertyTreeItem) inside);
            if (insideProp.propertyName.compareSame(multiname)) {
               TreeItem insideObj = obj;
               if (insideObj instanceof LocalRegTreeItem) {
                  insideObj = ((LocalRegTreeItem) insideObj).computedValue;
               }
               if (insideProp.object == insideObj) {
                  if (stack.size() > 0) {
                     TreeItem top = stack.peek().getNotCoerced();
                     if (top == insideProp) {
                        stack.pop();
                        stack.push(new PostIncrementTreeItem(ins, insideProp));
                     } else if ((top instanceof IncrementTreeItem) && (((IncrementTreeItem) top).object == inside)) {
                        stack.pop();
                        stack.push(new PreIncrementTreeItem(ins, insideProp));
                     } else {
                        output.add(new PostIncrementTreeItem(ins, insideProp));
                     }
                  } else {
                     output.add(new PostIncrementTreeItem(ins, insideProp));
                  }
                  return;
               }
            }
         }
      }

      if (value.getThroughRegister() instanceof DecrementTreeItem) {
         TreeItem inside = ((DecrementTreeItem) value.getThroughRegister()).object.getThroughRegister().getNotCoerced();
         if (inside instanceof GetPropertyTreeItem) {
            GetPropertyTreeItem insideProp = ((GetPropertyTreeItem) inside);
            if (insideProp.propertyName.compareSame(multiname)) {
               TreeItem insideObj = obj;
               if (insideObj instanceof LocalRegTreeItem) {
                  insideObj = ((LocalRegTreeItem) insideObj).computedValue;
               }
               if (insideProp.object == insideObj) {
                  if (stack.size() > 0) {
                     TreeItem top = stack.peek().getNotCoerced();
                     if (top == insideProp) {
                        stack.pop();
                        stack.push(new PostDecrementTreeItem(ins, insideProp));
                     } else if ((top instanceof DecrementTreeItem) && (((DecrementTreeItem) top).object == inside)) {
                        stack.pop();
                        stack.push(new PreDecrementTreeItem(ins, insideProp));
                     } else {
                        output.add(new PostDecrementTreeItem(ins, insideProp));
                     }
                  } else {
                     output.add(new PostDecrementTreeItem(ins, insideProp));
                  }
                  return;
               }
            }
         }
      }
      output.add(new SetPropertyTreeItem(ins, obj, multiname, value));
   }

   public String getObject(Stack<TreeItem> stack, ABC abc, AVM2Instruction ins, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, HashMap<Integer, String> localRegNames) {
      int multinameIndex = ins.operands[0];
      String multiname = resolveMultinameNoPop(0, stack, abc.constants, multinameIndex, ins);
      TreeItem obj = stack.get(1 + resolvedCount(abc.constants, multinameIndex)); //pod vrcholem
      if ((!obj.toString().equals(""))) {
         multiname = "." + multiname;
      }
      return obj + multiname;
   }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      int ret = -2;
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
