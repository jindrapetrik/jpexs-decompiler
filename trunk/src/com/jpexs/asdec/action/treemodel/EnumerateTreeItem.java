package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class EnumerateTreeItem extends TreeItem {
    public TreeItem object;

    public EnumerateTreeItem(Action instruction, TreeItem object) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "enumerate " + object.toString(constants);
    }
}
