package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class DefineLocalTreeItem extends TreeItem {
    public TreeItem name;
    public TreeItem value;

    public DefineLocalTreeItem(Action instruction, TreeItem name, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.name=name;
        this.value=value;
    }

    @Override
    public String toString(ConstantPool constants) {
        if (value == null) return "var " + stripQuotes(name) + ";";
        return "var " + stripQuotes(name) + "=" + value.toString(constants) + ";";
    }
}
