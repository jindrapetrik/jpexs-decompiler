package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class SetMemberTreeItem extends TreeItem {
    public TreeItem object;
    public TreeItem objectName;
    public TreeItem value;

    public SetMemberTreeItem(Action instruction, TreeItem object, TreeItem objectName, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.objectName = objectName;
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return object.toString(constants) + "." + stripQuotes(objectName) + "=" + value.toString(constants) + ";";
    }
}