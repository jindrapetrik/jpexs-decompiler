package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.helpers.Helper;

public class SetTargetTreeItem extends TreeItem {
    public String target;

    public SetTargetTreeItem(Action instruction, String target) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.target = target;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "tellTarget(\"" + Helper.escapeString(target) + "\");";
    }
}