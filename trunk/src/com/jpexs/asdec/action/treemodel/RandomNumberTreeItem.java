package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class RandomNumberTreeItem extends TreeItem {
    private TreeItem maximum;

    public RandomNumberTreeItem(Action instruction, TreeItem maximum) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.maximum = maximum;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "random(" + maximum.toString(constants) + ")";
    }
}