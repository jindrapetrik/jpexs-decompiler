/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class NameSpaceTreeItem extends TreeItem {
    public int namespaceIndex;

    public NameSpaceTreeItem(AVM2Instruction instruction, int namespaceIndex) {
        super(instruction, NOPRECEDENCE);
        this.namespaceIndex = namespaceIndex;
    }

    public String toString(ConstantPool constants) {
        if (namespaceIndex == 0) return "*";
        return hilight(constants.constant_namespace[namespaceIndex].toString(constants));
    }
}
