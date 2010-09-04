package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class CallTreeItem extends TreeItem {
    private TreeItem value;

    public CallTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return stripQuotes(value) + "()";
    }
}