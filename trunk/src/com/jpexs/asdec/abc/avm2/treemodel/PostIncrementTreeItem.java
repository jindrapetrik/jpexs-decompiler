/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class PostIncrementTreeItem extends TreeItem {
    public TreeItem object;

    public PostIncrementTreeItem(AVM2Instruction instruction, TreeItem object) {
        super(instruction, PRECEDENCE_POSTFIX);
        this.object = object;
    }

    @Override
    public String toString(ConstantPool constants) {
        return object.toString(constants) + hilight("++");
    }


}
