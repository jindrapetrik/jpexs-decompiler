/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class PreIncrementTreeItem extends UnaryOpTreeItem {

    public PreIncrementTreeItem(Action instruction, TreeItem object) {
        super(instruction, PRECEDENCE_UNARY, object, "++");
    }


}
