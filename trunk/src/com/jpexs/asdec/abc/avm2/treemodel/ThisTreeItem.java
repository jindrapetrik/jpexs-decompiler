/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;


public class ThisTreeItem extends TreeItem {

    public ThisTreeItem() {
        super(null, PRECEDENCE_PRIMARY);
    }

    @Override
    public String toString(ConstantPool constants) {
        return "this";
    }


}
