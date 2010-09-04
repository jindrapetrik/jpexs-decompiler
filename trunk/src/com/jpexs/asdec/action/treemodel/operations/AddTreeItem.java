/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class AddTreeItem extends BinaryOpTreeItem {

    public AddTreeItem(Action instruction, TreeItem leftSide, TreeItem rightSide) {
        super(instruction, PRECEDENCE_ADDITIVE, leftSide, rightSide, "+");
    }

}
