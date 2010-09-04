package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class GetVariableTreeItem extends TreeItem {
    public TreeItem value;
    public GetVariableTreeItem(Action instruction,TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value=value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return stripQuotes(value);
    }
}
