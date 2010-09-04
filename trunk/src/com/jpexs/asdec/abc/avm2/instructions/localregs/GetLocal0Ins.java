/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.localregs;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.ClassTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.ThisTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class GetLocal0Ins extends InstructionDefinition implements GetLocalTypeIns {

    public GetLocal0Ins() {
        super(0xd0, "getlocal_0", new int[]{});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        if (isStatic) {
            stack.push(new ClassTreeItem(abc.instance_info[classIndex].getName(constants).getName(constants)));
        } else {
            stack.push(new ThisTreeItem());
        }
    }

    public int getRegisterId(AVM2Instruction par0) {
        return 0;
    }
}
