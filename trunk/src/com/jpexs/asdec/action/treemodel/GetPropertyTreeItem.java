package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class GetPropertyTreeItem extends TreeItem {
    public TreeItem target;
    public int propertyIndex;

    public GetPropertyTreeItem(Action instruction, TreeItem target, int propertyIndex) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.target = target;
        this.propertyIndex = propertyIndex;
    }

    @Override
    public String toString(ConstantPool constants) {
        if(isEmptyString(target))
         return Action.propertyNames[propertyIndex];
        return target.toString(constants) + "." + Action.propertyNames[propertyIndex];
    }
}
