/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.operations;

import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public class InTreeItem extends BinaryOpTreeItem {

    public InTreeItem(AVM2Instruction instruction, TreeItem name, TreeItem object) {
        super(instruction, PRECEDENCE_RELATIONAL, name, object, " in ");
    }


}
