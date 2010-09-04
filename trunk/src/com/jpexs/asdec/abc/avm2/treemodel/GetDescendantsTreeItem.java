/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class GetDescendantsTreeItem extends TreeItem {
    public TreeItem object;
    public FullMultinameTreeItem multiname;

    public GetDescendantsTreeItem(AVM2Instruction instruction, TreeItem object, FullMultinameTreeItem multiname) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.multiname = multiname;
    }

    @Override
    public String toString(ConstantPool constants) {
        return object.toString(constants) + hilight("..") + multiname.toString(constants);
    }


}
