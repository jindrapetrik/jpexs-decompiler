package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class ExtendsTreeItem extends TreeItem {
    public TreeItem subclass;
    public TreeItem superclass;

    public ExtendsTreeItem(Action instruction, TreeItem subclass, TreeItem superclass) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.subclass = subclass;
        this.superclass = superclass;
    }

    @Override
    public String toString(ConstantPool constants) {
        return subclass.toString(constants) + " extends " + stripQuotes(superclass);
    }
}
