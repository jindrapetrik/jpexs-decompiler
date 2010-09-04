package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class ReturnTreeItem extends TreeItem {
    public TreeItem value;

    public ReturnTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "return " + value.toString(constants) + ";";
    }
}
