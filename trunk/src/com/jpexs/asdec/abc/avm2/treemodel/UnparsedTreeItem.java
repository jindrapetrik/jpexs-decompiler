/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class UnparsedTreeItem extends TreeItem {
    public String value;

    public UnparsedTreeItem(AVM2Instruction instruction, String value) {
        super(instruction, NOPRECEDENCE);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight(value);
    }


}
