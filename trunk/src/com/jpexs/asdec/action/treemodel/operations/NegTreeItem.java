/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class NegTreeItem extends UnaryOpTreeItem {

    public NegTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_UNARY, value, "-");
    }
}
