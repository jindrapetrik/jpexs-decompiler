/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class ReturnVoidTreeItem extends TreeItem {

    public ReturnVoidTreeItem(AVM2Instruction instruction) {
        super(instruction, NOPRECEDENCE);
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("return") + ";";
    }


}
