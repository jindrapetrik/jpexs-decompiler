/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class SetGlobalSlotTreeItem extends TreeItem {
    public int slotId;
    public TreeItem value;

    public SetGlobalSlotTreeItem(AVM2Instruction instruction, int slotId, TreeItem value) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.slotId = slotId;
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("setglobalslot(" + slotId + ",") + value.toString(constants) + hilight(")") + ";";
    }


}
