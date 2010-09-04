package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class SetTarget2TreeItem extends TreeItem {
    public TreeItem target;

    public SetTarget2TreeItem(Action instruction, TreeItem target) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.target = target;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "tellTarget(" + target.toString(constants) + ");";
    }
}