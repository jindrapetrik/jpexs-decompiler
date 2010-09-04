package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class MBAsciiToCharTreeItem extends TreeItem {
    private TreeItem value;

    public MBAsciiToCharTreeItem(Action instruction, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "mbord(" + value.toString(constants) + ")";
    }
}