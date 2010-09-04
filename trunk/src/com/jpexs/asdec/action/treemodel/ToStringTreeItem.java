package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class ToStringTreeItem extends TreeItem {
    private TreeItem value;

    public ToStringTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return value.toString(constants) + ".toString()";
    }
}