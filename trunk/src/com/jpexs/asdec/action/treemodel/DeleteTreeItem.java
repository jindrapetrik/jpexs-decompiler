package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class DeleteTreeItem extends TreeItem {
    public TreeItem object;
    public TreeItem propertyName;

    public DeleteTreeItem(Action instruction, TreeItem object, TreeItem propertyName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public String toString(ConstantPool constants) {
        if (object == null) return "delete " + propertyName.toString(constants);
        return "delete " + object.toString(constants) + "." + stripQuotes(propertyName);
    }
}
