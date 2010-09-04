/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class UndefinedTreeItem extends TreeItem {

    public UndefinedTreeItem(AVM2Instruction instruction) {
        super(instruction, PRECEDENCE_PRIMARY);
    }


    @Override
    public String toString(ConstantPool constants) {
        return hilight("undefined");
    }


}
