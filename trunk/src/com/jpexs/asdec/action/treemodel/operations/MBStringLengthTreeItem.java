/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class MBStringLengthTreeItem extends TreeItem {
    public TreeItem value;

    public MBStringLengthTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        String s = value.toString(constants);
        if (value.precedence > precedence) s = "(" + s + ")";
        return hilight("mblength(") + s + ")";
    }
}