/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.clauses;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.TreeItem;


public abstract class LoopTreeItem extends TreeItem {
    public long loopBreak;
    public long loopContinue;

    public LoopTreeItem(Action instruction, long loopBreak, long loopContinue) {
        super(instruction, NOPRECEDENCE);
        this.loopBreak = loopBreak;
        this.loopContinue = loopContinue;
    }
}
