package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class SimpleActionTreeItem extends TreeItem {
    private String actionString;

    @Override
    public String toString(ConstantPool constants) {
        return actionString;
    }

    public SimpleActionTreeItem(Action instruction, String actionString) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.actionString = actionString;
    }
}
