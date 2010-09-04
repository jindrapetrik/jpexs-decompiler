package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class WaitForFrameTreeItem extends TreeItem {
    public int frame;
    public int skipCount;

    public WaitForFrameTreeItem(Action instruction, int frame, int skipCount) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.frame = frame;
        this.skipCount = skipCount;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "waitForFrame(" + frame + "," + skipCount + ");";
    }
}