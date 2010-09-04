/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class BooleanTreeItem extends TreeItem {

    public Boolean value;

    public BooleanTreeItem(AVM2Instruction instruction, Boolean value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return value.toString();
    }

    @Override
    public boolean isFalse() {
        return value == false;
    }

    @Override
    public boolean isTrue() {
        return value == true;
    }


}
