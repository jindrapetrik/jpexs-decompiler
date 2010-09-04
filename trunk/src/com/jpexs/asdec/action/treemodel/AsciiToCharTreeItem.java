package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class AsciiToCharTreeItem extends TreeItem {
    private TreeItem value;

    public AsciiToCharTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "ord(" + value.toString(constants) + ")";
    }
}