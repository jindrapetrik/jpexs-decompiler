package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class MBStringExtractTreeItem extends TreeItem {
    public TreeItem value;
    public TreeItem index;
    public TreeItem count;

    public MBStringExtractTreeItem(Action instruction, TreeItem value, TreeItem index, TreeItem count) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
        this.index = index;
        this.count = count;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "mbsubstring(" + value.toString(constants) + "," + index.toString(constants) + "," + count.toString(constants) + ")";
    }
}