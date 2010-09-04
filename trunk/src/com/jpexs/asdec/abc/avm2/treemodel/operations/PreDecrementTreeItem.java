/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.operations;

import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public class PreDecrementTreeItem extends UnaryOpTreeItem {
    public PreDecrementTreeItem(AVM2Instruction instruction, TreeItem object) {
        super(instruction, PRECEDENCE_UNARY, object, "--");
    }
}
