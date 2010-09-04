/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.executing;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.CallSuperTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class CallSuperVoidIns extends InstructionDefinition {

    public CallSuperVoidIns() {
        super(0x4e, "callsupervoid", new int[]{AVM2Code.DAT_MULTINAME_INDEX, AVM2Code.DAT_ARG_COUNT});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        int multinameIndex = (int) ((Long) arguments.get(0)).longValue();
        int argCount = (int) ((Long) arguments.get(1)).longValue();
        List passArguments = new ArrayList();
        for (int i = argCount - 1; i >= 0; i--) {
            passArguments.set(i, lda.operandStack.pop());
        }
        //if multiname[multinameIndex] is runtime
        //pop(name) pop(ns)
        Object receiver = lda.operandStack.pop();
        throw new RuntimeException("Call to unknown super method");
        //do not push anything
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        int multinameIndex = ins.operands[0];
        int argCount = ins.operands[1];
        List<TreeItem> args = new ArrayList<TreeItem>();
        for (int a = 0; a < argCount; a++) {
            args.add((TreeItem) stack.pop());
        }
        FullMultinameTreeItem multiname = resolveMultiname(stack, constants, multinameIndex, ins);
        TreeItem receiver = (TreeItem) stack.pop();

        output.add(new CallSuperTreeItem(ins, true, receiver, multiname, args));

    }
}
