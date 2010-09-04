/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;


public class NameValuePair extends TreeItem {
    public TreeItem name;
    public TreeItem value;

    public NameValuePair(TreeItem name, TreeItem value) {
        super(name.instruction, NOPRECEDENCE);
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        if (name instanceof StringTreeItem) {
            return ((StringTreeItem) name).value + ":" + value.toString(constants);
        }
        return name.toString(constants) + ":" + value.toString(constants);
    }


}
