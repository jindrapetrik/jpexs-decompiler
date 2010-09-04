/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.construction;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.NewFunctionTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class NewFunctionIns extends InstructionDefinition {

    public NewFunctionIns() {
        super(0x40, "newfunction", new int[]{AVM2Code.DAT_METHOD_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        int methodIndex = ins.operands[0];
        MethodBody mybody = abc.findBody(methodIndex);
        String bodyStr = "";
        if (mybody != null) {
            bodyStr = mybody.toString(isStatic, classIndex, abc, constants, method_info, false);
        }
        stack.push(new NewFunctionTreeItem(ins, method_info[methodIndex].getParamStr(constants), method_info[methodIndex].getReturnTypeStr(constants), bodyStr));
    }
}
