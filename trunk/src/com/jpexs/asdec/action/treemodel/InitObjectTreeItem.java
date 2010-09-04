package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

import java.util.List;

public class InitObjectTreeItem extends TreeItem {
    public List<TreeItem> names;
    public List<TreeItem> values;

    public InitObjectTreeItem(Action instruction, List<TreeItem> names, List<TreeItem> values) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.values = values;
        this.names = names;
    }

    @Override
    public String toString(ConstantPool constants) {
        String objStr = "";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) objStr += ",";
            objStr += names.get(i).toString(constants) + ":" + values.get(i).toString(constants);
        }
        return "{" + objStr + "}";
    }
}