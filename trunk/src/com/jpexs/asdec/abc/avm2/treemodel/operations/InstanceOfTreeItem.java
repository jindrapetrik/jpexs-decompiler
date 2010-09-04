/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.operations;

import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public class InstanceOfTreeItem extends BinaryOpTreeItem {

    public InstanceOfTreeItem(AVM2Instruction instruction, TreeItem value, TreeItem type) {
        super(instruction, PRECEDENCE_RELATIONAL, value, type, " instanceof ");
    }

}
