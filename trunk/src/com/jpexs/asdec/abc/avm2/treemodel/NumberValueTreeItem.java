/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public abstract class NumberValueTreeItem extends TreeItem {

    public NumberValueTreeItem(AVM2Instruction instruction) {
        super(instruction, PRECEDENCE_PRIMARY);
    }

}
