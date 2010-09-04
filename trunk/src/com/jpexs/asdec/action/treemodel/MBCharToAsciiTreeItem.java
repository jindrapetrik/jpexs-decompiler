package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class MBCharToAsciiTreeItem extends TreeItem {
    private TreeItem value;

    public MBCharToAsciiTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "mbchr(" + value.toString(constants) + ")";
    }
}