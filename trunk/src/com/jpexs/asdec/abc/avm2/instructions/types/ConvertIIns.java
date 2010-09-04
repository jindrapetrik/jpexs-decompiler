/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.ConvertTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class ConvertIIns extends InstructionDefinition {

    public ConvertIIns() {
        super(0x73, "convert_i", new int[]{});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        Object value = lda.operandStack.pop();
        long ret = 0;
        if (value == null) {
            ret = 0;
        } else if (value instanceof Boolean) {
            if (((Boolean) value).booleanValue()) {
                ret = 1;
            } else {
                ret = 0;
            }
        } else if (value instanceof Long) {
            ret = ((Long) value).longValue();
        } else if (value instanceof String) {
            ret = Long.parseLong((String) value);
        } else {
            ret = 1; //must call toPrimitive
        }
        lda.operandStack.push(new Long(ret));
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        stack.push(new ConvertTreeItem(ins, (TreeItem) stack.pop(), "int"));
    }
}
