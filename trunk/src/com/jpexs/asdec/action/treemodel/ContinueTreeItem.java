/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;


public class ContinueTreeItem extends TreeItem {
    public long loopPos;
    public boolean isKnown;
    public boolean isBreak;

    public ContinueTreeItem(Action instruction, long loopPos) {
        this(instruction, loopPos, true);
    }

    public ContinueTreeItem(Action instruction, long loopPos, boolean isKnown) {
        super(instruction, NOPRECEDENCE);
        this.loopPos = loopPos;
        this.isKnown = isKnown;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight(isBreak ? "break" : "continue") + " " + (isKnown ? "loop" : "unk") + loopPos + ";";
    }

}
