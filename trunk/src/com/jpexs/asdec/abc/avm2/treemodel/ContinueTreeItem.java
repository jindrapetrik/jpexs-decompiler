/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class ContinueTreeItem extends TreeItem {
    public int loopPos;
    public boolean isKnown;

    public ContinueTreeItem(AVM2Instruction instruction, int loopPos) {
        this(instruction, loopPos, true);
    }

    public ContinueTreeItem(AVM2Instruction instruction, int loopPos, boolean isKnown) {
        super(instruction, NOPRECEDENCE);
        this.loopPos = loopPos;
        this.isKnown = isKnown;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("continue") + " " + (isKnown ? "loop" : "unk") + loopPos + ";";
    }

}
