/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;


public abstract class UnaryOpTreeItem extends TreeItem {
    public TreeItem value;
    public String operator;

    public UnaryOpTreeItem(Action instruction, int precedence, TreeItem value, String operator) {
        super(instruction, precedence);
        this.value = value;
        this.operator = operator;
    }

    @Override
    public String toString(ConstantPool constants) {
        String s = value.toString(constants);
        if (value.precedence > precedence) s = "(" + s + ")";
        return hilight(operator) + s;
    }
}
