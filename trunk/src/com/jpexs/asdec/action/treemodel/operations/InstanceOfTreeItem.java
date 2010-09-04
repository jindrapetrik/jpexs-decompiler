/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class InstanceOfTreeItem extends BinaryOpTreeItem {

    public InstanceOfTreeItem(Action instruction, TreeItem value, TreeItem type) {
        super(instruction, PRECEDENCE_RELATIONAL, value, type, " instanceof ");
    }

}