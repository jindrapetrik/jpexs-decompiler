package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class CastOpTreeItem extends TreeItem {
    public TreeItem constructor;
    public TreeItem object;

    public CastOpTreeItem(Action instruction, TreeItem constructor, TreeItem object) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.constructor = constructor;
        this.object = object;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "(" + stripQuotes(constructor) + ")" + object.toString(constants);
    }
}
