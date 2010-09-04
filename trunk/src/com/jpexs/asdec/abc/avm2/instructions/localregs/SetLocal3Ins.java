/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.localregs;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.instructions.SetTypeIns;
import com.jpexs.asdec.abc.avm2.treemodel.FindPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.NewActivationTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.SetLocalTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class SetLocal3Ins extends InstructionDefinition implements SetTypeIns, SetLocalTypeIns {

    public SetLocal3Ins() {
        super(0xd7, "setlocal_3", new int[]{});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        TreeItem val = (TreeItem) stack.pop();
        localRegs.put(3, val);
        if (val instanceof NewActivationTreeItem) return;
        if (val instanceof FindPropertyTreeItem) return;
        //if(val.startsWith("catchscope ")) return;
        //if(val.startsWith("newactivation()")) return;
        output.add(new SetLocalTreeItem(ins, 3, val));
    }

    public String getObject(Stack<TreeItem> stack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body) {
        return localRegName(3);
    }

    public int getRegisterId(AVM2Instruction ins) {
        return 3;
    }
}
