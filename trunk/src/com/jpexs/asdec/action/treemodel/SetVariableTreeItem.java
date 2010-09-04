package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class SetVariableTreeItem extends TreeItem {
    public TreeItem name;
    public TreeItem value;

    public SetVariableTreeItem(Action instruction, TreeItem name, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return stripQuotes(name) + hilight("=") + value.toString(constants) + ";";
    }
}