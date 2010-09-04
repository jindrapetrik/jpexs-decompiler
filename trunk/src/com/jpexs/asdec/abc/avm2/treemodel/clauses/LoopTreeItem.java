/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public abstract class LoopTreeItem extends TreeItem {
    public int loopBreak;
    public int loopContinue;

    public LoopTreeItem(AVM2Instruction instruction, int loopBreak, int loopContinue) {
        super(instruction, NOPRECEDENCE);
        this.loopBreak = loopBreak;
        this.loopContinue = loopContinue;
    }
}
