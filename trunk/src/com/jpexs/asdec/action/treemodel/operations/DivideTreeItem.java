/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class DivideTreeItem extends BinaryOpTreeItem {

    public DivideTreeItem(Action instruction, TreeItem leftSide, TreeItem rightSide) {
        super(instruction, PRECEDENCE_MULTIPLICATIVE, leftSide, rightSide, "/");
    }

}
