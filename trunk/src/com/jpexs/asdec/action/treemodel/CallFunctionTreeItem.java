package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

import java.util.List;

public class CallFunctionTreeItem extends TreeItem {
    public TreeItem functionName;
    public List<TreeItem> arguments;

    public CallFunctionTreeItem(Action instruction, TreeItem functionName, List<TreeItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public String toString(ConstantPool constants) {
        String paramStr = "";
        for (int t = 0; t < arguments.size(); t++) {
            if (t > 0) paramStr += ",";
            paramStr += arguments.get(t).toString(constants);
        }
        return stripQuotes(functionName) + "(" + paramStr + ")";
    }
}
