package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

import java.util.List;

public class InitArrayTreeItem extends TreeItem {
    public List<TreeItem> values;

    public InitArrayTreeItem(Action instruction, List<TreeItem> values) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.values = values;
    }

    @Override
    public String toString(ConstantPool constants) {
        String arrStr = "";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) arrStr += ",";
            arrStr += values.get(i).toString(constants);
        }
        return "[" + arrStr + "]";
    }
}
