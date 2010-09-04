/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.other;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.GetDescendantsTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class GetDescendantsIns extends InstructionDefinition {

    public GetDescendantsIns() {
        super(0x59, "getdescentants", new int[]{AVM2Code.DAT_MULTINAME_INDEX});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        int multiIndex = (int) ((Long) arguments.get(0)).longValue();
        //if is runtime
        //pop(name), pop(ns)
        Object obj = lda.operandStack.pop();
        throw new RuntimeException("getdescentants not working");
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        int multinameIndex = ins.operands[0];
        FullMultinameTreeItem multiname = resolveMultiname(stack, constants, multinameIndex, ins);
        TreeItem obj = (TreeItem) stack.pop();
        stack.push(new GetDescendantsTreeItem(ins, obj, multiname));
    }
}
