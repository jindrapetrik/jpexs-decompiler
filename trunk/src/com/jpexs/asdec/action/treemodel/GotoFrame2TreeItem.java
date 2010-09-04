package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class GotoFrame2TreeItem extends TreeItem {
    public TreeItem frame;
    public boolean sceneBiasFlag;
    public boolean playFlag;
    public int sceneBias;

    public GotoFrame2TreeItem(Action instruction, TreeItem frame, boolean sceneBiasFlag, boolean playFlag, int sceneBias) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.frame = frame;
        this.sceneBiasFlag = sceneBiasFlag;
        this.playFlag = playFlag;
        this.sceneBias = sceneBias;
    }

    @Override
    public String toString(ConstantPool constants) {
        String prefix = "gotoAndStop";
        if (playFlag) prefix = "gotoAndPlay";
        return prefix + "(" + frame.toString(constants) + (sceneBiasFlag ? "," + sceneBias : "") + ");";
    }
}