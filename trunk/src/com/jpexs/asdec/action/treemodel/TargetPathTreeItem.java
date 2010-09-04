package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class TargetPathTreeItem extends TreeItem {
    public TreeItem object;

    public TargetPathTreeItem(Action instruction, TreeItem object) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "targetPath(" + object.toString(constants) + ");";
    }
}
