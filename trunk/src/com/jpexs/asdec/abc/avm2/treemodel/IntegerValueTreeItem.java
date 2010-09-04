/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class IntegerValueTreeItem extends NumberValueTreeItem {
    public Long value;

    public IntegerValueTreeItem(AVM2Instruction instruction, Long value) {
        super(instruction);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("" + value);
    }


}
