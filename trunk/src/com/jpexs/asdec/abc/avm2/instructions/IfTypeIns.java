/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions;

import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;

import java.util.Stack;


public interface IfTypeIns {
    public abstract void translateInverted(java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, AVM2Instruction ins);
}
