/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class BitXorTreeItem extends BinaryOpTreeItem {

    public BitXorTreeItem(Action instruction, TreeItem leftSide, TreeItem rightSide) {
        super(instruction, PRECEDENCE_BITWISEXOR, leftSide, rightSide, "^");
    }

}
