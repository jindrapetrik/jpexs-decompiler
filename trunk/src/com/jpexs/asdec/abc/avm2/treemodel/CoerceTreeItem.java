/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class CoerceTreeItem extends TreeItem {
    public TreeItem value;
    public String type;

    public CoerceTreeItem(AVM2Instruction instruction, TreeItem value, String type) {
        super(instruction, NOPRECEDENCE);
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString(ConstantPool constants) {
        //return hilight("("+type+")")+
        return value.toString(constants);
    }

}
