/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class NotTreeItem extends UnaryOpTreeItem {

    public NotTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_UNARY, value, "!");
    }

    @Override
    public boolean isTrue() {
        return !value.isTrue();
    }

    @Override
    public boolean isFalse() {
        return !value.isFalse();
    }


}
