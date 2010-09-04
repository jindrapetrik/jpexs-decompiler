package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class CharToAsciiTreeItem extends TreeItem {
    private TreeItem value;

    public CharToAsciiTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "chr(" + value.toString(constants) + ")";
    }
}