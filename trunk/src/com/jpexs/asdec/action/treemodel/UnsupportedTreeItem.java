package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class UnsupportedTreeItem extends TreeItem {
    public String value;
    public UnsupportedTreeItem(Action instruction, String value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value=value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "Unsupported:"+value+";";
    }
}
