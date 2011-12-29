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
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.CallMethodTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class CallMethodIns extends InstructionDefinition {

    public CallMethodIns() {
        super(0x43, "callmethod", new int[]{AVM2Code.DAT_METHOD_INDEX, AVM2Code.DAT_ARG_COUNT});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        int methodIndex = (int) ((Long) arguments.get(0)).longValue(); //index of object's method
        int argCount = (int) ((Long) arguments.get(1)).longValue();
        List passArguments = new ArrayList();
        for (int i = argCount - 1; i >= 0; i--) {
            passArguments.set(i, lda.operandStack.pop());
        }
        Object receiver = lda.operandStack.pop();
        throw new RuntimeException("Call to unknown method");
        //push(result)
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        int methodIndex = ins.operands[0];
        int argCount = ins.operands[1];
        List<TreeItem> args = new ArrayList<TreeItem>();
        for (int a = 0; a < argCount; a++) {
            args.add(0, (TreeItem) stack.pop());
        }
        TreeItem receiver = (TreeItem) stack.pop();
        String methodName = method_info[methodIndex].getName(constants);
        stack.push(new CallMethodTreeItem(ins, receiver, methodName, args));
    }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return -1+1-ins.operands[1];
   }


}
