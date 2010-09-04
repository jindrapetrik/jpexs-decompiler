/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class BitNotTreeItem extends UnaryOpTreeItem {

    public BitNotTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_UNARY, value, "~");
    }
}
