package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class WaitForFrame2TreeItem extends TreeItem {
    public TreeItem frame;
    public int skipCount;

    public WaitForFrame2TreeItem(Action instruction, TreeItem frame, int skipCount) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.frame = frame;
        this.skipCount = skipCount;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "waitForFrame2(" + frame.toString(constants) + "," + skipCount + ");";
    }
}