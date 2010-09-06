package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.swf4.RegisterNumber;

public class StoreRegisterTreeItem extends TreeItem {
    public RegisterNumber register;
    public TreeItem value;

    public StoreRegisterTreeItem(Action instruction, RegisterNumber register, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
        this.register = register;
    }

    @Override
    public String toString(ConstantPool constants) {
        return register.toString() + "=" + value.toString(constants) + ";";
    }
}
