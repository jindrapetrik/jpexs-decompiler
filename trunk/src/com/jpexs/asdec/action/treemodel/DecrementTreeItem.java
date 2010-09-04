package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class DecrementTreeItem extends TreeItem {
    public TreeItem object;

    public DecrementTreeItem(Action instruction, TreeItem object) {
        super(instruction, PRECEDENCE_ADDITIVE);
        this.object = object;
    }

    @Override
    public String toString(ConstantPool constants) {
        return object.toString(constants) + "-1";
    }
}