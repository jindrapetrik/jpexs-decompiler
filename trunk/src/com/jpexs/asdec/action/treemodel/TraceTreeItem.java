package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class TraceTreeItem extends TreeItem {
    private TreeItem value;

    public TraceTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "trace(" + value.toString(constants) + ");";
    }
}