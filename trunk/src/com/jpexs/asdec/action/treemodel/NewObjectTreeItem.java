package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

import java.util.List;

public class NewObjectTreeItem extends TreeItem {
    public TreeItem objectName;
    public List<TreeItem> arguments;

    public NewObjectTreeItem(Action instruction, TreeItem objectName, List<TreeItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.objectName = objectName;
        this.arguments = arguments;
    }

    @Override
    public String toString(ConstantPool constants) {
        String paramStr = "";
        for (int t = 0; t < arguments.size(); t++) {
            if (t > 0) paramStr += ",";
            paramStr += arguments.get(t).toString(constants);
        }
        return "new " + stripQuotes(objectName) + "(" + paramStr + ")";
    }
}