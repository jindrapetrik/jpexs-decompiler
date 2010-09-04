package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class SetPropertyTreeItem extends TreeItem {
    public TreeItem target;
    public int propertyIndex;
    public TreeItem value;

    public SetPropertyTreeItem(Action instruction, TreeItem target, int propertyIndex, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.target = target;
        this.propertyIndex = propertyIndex;
        this.value=value;
    }

    @Override
    public String toString(ConstantPool constants) {
        if(isEmptyString(target))
                      return Action.propertyNames[propertyIndex] + "=" + value.toString(constants) + ";";
        return target.toString(constants) + "." + Action.propertyNames[propertyIndex] + "=" + value.toString(constants) + ";";
    }
}