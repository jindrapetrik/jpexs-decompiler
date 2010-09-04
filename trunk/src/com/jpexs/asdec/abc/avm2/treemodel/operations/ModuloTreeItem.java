/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.operations;

import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public class ModuloTreeItem extends BinaryOpTreeItem {

    public ModuloTreeItem(AVM2Instruction instruction, TreeItem leftSide, TreeItem rightSide) {
        super(instruction, PRECEDENCE_MULTIPLICATIVE, leftSide, rightSide, "%");
    }

}
