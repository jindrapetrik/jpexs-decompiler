/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.operations;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public class DeletePropertyTreeItem extends TreeItem {
    public TreeItem object;
    public FullMultinameTreeItem propertyName;

    public DeletePropertyTreeItem(AVM2Instruction instruction, TreeItem object, FullMultinameTreeItem propertyName) {
        super(instruction, PRECEDENCE_UNARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("delete ") + object.toString(constants) + "[" + propertyName.toString(constants) + "]";
    }


}
