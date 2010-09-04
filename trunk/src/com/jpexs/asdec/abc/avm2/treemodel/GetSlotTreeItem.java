/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.types.Multiname;


public class GetSlotTreeItem extends TreeItem {
    public Multiname slotName;
    public TreeItem scope;

    public GetSlotTreeItem(AVM2Instruction instruction, TreeItem scope, Multiname slotName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.slotName = slotName;
        this.scope = scope;
    }

    @Override
    public String toString(ConstantPool constants) {
        //scope.toString(constants)+"."
        return hilight(slotName.getName(constants));
    }

}
