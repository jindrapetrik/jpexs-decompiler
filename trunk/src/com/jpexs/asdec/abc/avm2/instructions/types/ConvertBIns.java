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


public class ConvertBIns extends InstructionDefinition {

    public ConvertBIns() {
        super(0x76, "convert_b", new int[]{});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        Object value = lda.operandStack.pop();
        boolean bval = false;
        if (value instanceof Boolean) {
            bval = (Boolean) value;
        } else if (value instanceof Long) {
            bval = ((Long) value).longValue() != 0;
        } else if (value instanceof String) {
            bval = !((String) value).equals("");
        } else {
            bval = true;
        }
        lda.operandStack.push(new Boolean(bval));
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        stack.push(new ConvertTreeItem(ins, (TreeItem) stack.pop(), "boolean"));
    }
}
