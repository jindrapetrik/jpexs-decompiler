/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.types;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.AsTypeTreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class AsTypeIns extends InstructionDefinition {

    public AsTypeIns() {
        super(0x86, "astype", new int[]{AVM2Code.DAT_MULTINAME_INDEX});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        Long typeIndex = (Long) arguments.get(0);
        Object obj = lda.operandStack.pop();
        //if multiname[typeIndex]==obj
        lda.operandStack.push(obj);
        //else push null

    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        TreeItem val = (TreeItem) stack.pop();

        stack.push(new AsTypeTreeItem(ins, val, new FullMultinameTreeItem(ins, ins.operands[0])));
    }
}
