package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.helpers.Helper;

public class GotoLabelTreeItem extends TreeItem {
    public String label;

    public GotoLabelTreeItem(Action instruction, String label) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.label = label;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "gotoAndStop(\"" + Helper.escapeString(label) + "\");";
    }
}