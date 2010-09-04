/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class FindPropertyTreeItem extends TreeItem {
    public FullMultinameTreeItem propertyName;

    public FindPropertyTreeItem(AVM2Instruction instruction, FullMultinameTreeItem propertyName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.propertyName = propertyName;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "";
    }


}
