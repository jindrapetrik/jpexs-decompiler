package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class ToIntegerTreeItem extends TreeItem {
    private TreeItem value;

    public ToIntegerTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "int(" + value.toString(constants) + ")";
    }
}
