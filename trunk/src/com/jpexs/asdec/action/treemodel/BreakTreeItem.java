/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;


public class BreakTreeItem extends TreeItem {
    public long loopPos;
    public boolean isKnown;

    public BreakTreeItem(Action instruction, long loopPos) {
        this(instruction, loopPos, true);
    }

    public BreakTreeItem(Action instruction, long loopPos, boolean isKnown) {
        super(instruction, NOPRECEDENCE);
        this.loopPos = loopPos;
        this.isKnown = isKnown;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("break") + " loop" + loopPos + ";";
    }

}
