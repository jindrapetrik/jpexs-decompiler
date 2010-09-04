/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class SetPropertyTreeItem extends TreeItem {
    public TreeItem object;
    public FullMultinameTreeItem propertyName;
    public TreeItem value;

    public SetPropertyTreeItem(AVM2Instruction instruction, TreeItem object, FullMultinameTreeItem propertyName, TreeItem value) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.object = object;
        this.propertyName = propertyName;
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return formatProperty(constants, object, propertyName) + hilight("=") + value.toString(constants) + ";";
    }


}
