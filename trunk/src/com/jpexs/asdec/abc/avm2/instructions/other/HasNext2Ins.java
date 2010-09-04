/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.other;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.EachTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.LocalRegTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class HasNext2Ins extends InstructionDefinition {

    public HasNext2Ins() {
        super(0x32, "hasnext2", new int[]{AVM2Code.OPT_U8, AVM2Code.OPT_U8});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        int objectReg = ins.operands[0];
        int indexReg = ins.operands[1];
        //stack.push("_loc_" + objectReg + ".hasNext(cnt=_loc_" + indexReg + ")");
        stack.push(new EachTreeItem(ins, new LocalRegTreeItem(ins, indexReg, localRegs.get(indexReg)), new LocalRegTreeItem(ins, objectReg, localRegs.get(objectReg))));
    }
}
