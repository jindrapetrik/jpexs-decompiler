package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class GotoFrameTreeItem extends TreeItem {
    public int frame;

    public GotoFrameTreeItem(Action instruction, int frame) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.frame = frame;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "gotoAndStop(" + frame + ");";
    }
}
