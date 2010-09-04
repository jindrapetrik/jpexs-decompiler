package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

import java.util.List;

public class ImplementsOpTreeItem extends TreeItem {
    public TreeItem subclass;
    public List<TreeItem> superclasses;

    public ImplementsOpTreeItem(Action instruction, TreeItem subclass, List<TreeItem> superclasses) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.subclass = subclass;
        this.superclasses = superclasses;
    }

    @Override
    public String toString(ConstantPool constants) {
        String impStr = "";
        for (int i = 0; i < superclasses.size(); i++) {
            if (i > 0) impStr += ",";
            impStr += superclasses.get(i).toString(constants);
        }
        return subclass.toString(constants) + " implements " + impStr;
    }
}