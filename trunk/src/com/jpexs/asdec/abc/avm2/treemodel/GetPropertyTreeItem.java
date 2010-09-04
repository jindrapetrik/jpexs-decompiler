/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class GetPropertyTreeItem extends TreeItem {
    public TreeItem object;
    public FullMultinameTreeItem propertyName;

    public GetPropertyTreeItem(AVM2Instruction instruction, TreeItem object, FullMultinameTreeItem propertyName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public String toString(ConstantPool constants) {
        return formatProperty(constants, object, propertyName);
    }


}
