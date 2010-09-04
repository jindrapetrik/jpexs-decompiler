/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class EachTreeItem extends TreeItem {
    public TreeItem object;
    public TreeItem collection;

    public EachTreeItem(AVM2Instruction instruction, TreeItem object, TreeItem collection) {
        super(instruction, NOPRECEDENCE);
        this.object = object;
        this.collection = collection;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("each (") + object.toString(constants) + hilight(" in ") + collection.toString(constants) + ")";
    }


}
