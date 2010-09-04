/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;


public class ClassTreeItem extends TreeItem {
    public String className;

    public ClassTreeItem(String className) {
        super(null, PRECEDENCE_PRIMARY);
        this.className = className;
    }

    @Override
    public String toString(ConstantPool constants) {
        return className;
    }


}
