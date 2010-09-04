/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.construction;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.NameValuePair;
import com.jpexs.asdec.abc.avm2.treemodel.NewObjectTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class NewObjectIns extends InstructionDefinition {

    public NewObjectIns() {
        super(0x55, "newobject", new int[]{AVM2Code.DAT_ARG_COUNT});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        int argCount = ins.operands[0];
        List<NameValuePair> args = new ArrayList<NameValuePair>();
        for (int a = 0; a < argCount; a++) {
            TreeItem value = (TreeItem) stack.pop();
            TreeItem name = (TreeItem) stack.pop();
            args.add(0, new NameValuePair(name, value));
        }
        stack.push(new NewObjectTreeItem(ins, args));
    }
}
