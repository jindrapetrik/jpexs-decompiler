package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class TypeOfTreeItem extends TreeItem {
    private TreeItem value;

    public TypeOfTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "typeof(" + value.toString(constants) + ")";
    }
}