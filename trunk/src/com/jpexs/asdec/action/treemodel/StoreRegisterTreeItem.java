package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class StoreRegisterTreeItem extends TreeItem {
    public int registerIndex;
    public TreeItem value;

    public StoreRegisterTreeItem(Action instruction, int registerIndex, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
        this.registerIndex = registerIndex;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "register" + registerIndex + "=" + value.toString(constants) + ";";
    }
}
