package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class ThrowTreeItem extends TreeItem {
    public TreeItem object;

    public ThrowTreeItem(Action instruction, TreeItem object) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "throw " + object.toString(constants) + ";";
    }
}
