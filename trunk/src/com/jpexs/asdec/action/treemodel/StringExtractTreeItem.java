package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class StringExtractTreeItem extends TreeItem {
    public TreeItem value;
    public TreeItem index;
    public TreeItem count;

    public StringExtractTreeItem(Action instruction, TreeItem value, TreeItem index, TreeItem count) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
        this.index = index;
        this.count = count;
    }

    @Override
    public String toString(ConstantPool constants) {
        return value.toString(constants) + ".substr(" + index.toString(constants) + "," + count.toString(constants) + ")";
    }
}