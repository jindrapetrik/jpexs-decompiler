/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.types.Multiname;


public class GetLexTreeItem extends TreeItem {
    public Multiname propertyName;

    public GetLexTreeItem(AVM2Instruction instruction, Multiname propertyName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.propertyName = propertyName;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight(propertyName.getName(constants));
    }


}
