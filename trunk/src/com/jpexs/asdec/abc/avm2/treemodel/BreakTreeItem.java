/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class BreakTreeItem extends TreeItem {
    public int loopPos;
    public boolean isKnown;

    public BreakTreeItem(AVM2Instruction instruction, int loopPos) {
        this(instruction, loopPos, true);
    }

    public BreakTreeItem(AVM2Instruction instruction, int loopPos, boolean isKnown) {
        super(instruction, NOPRECEDENCE);
        this.loopPos = loopPos;
        this.isKnown = isKnown;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("break") + " loop" + loopPos + ";";
    }

}
