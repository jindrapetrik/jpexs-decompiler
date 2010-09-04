/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class InTreeItem extends BinaryOpTreeItem {

    public InTreeItem(Action instruction, TreeItem name, TreeItem object) {
        super(instruction, PRECEDENCE_RELATIONAL, name, object, " in ");
    }


}
