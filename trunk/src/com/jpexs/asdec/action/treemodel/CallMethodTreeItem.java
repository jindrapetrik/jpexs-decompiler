package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.swf4.Undefined;

import java.util.List;

public class CallMethodTreeItem extends TreeItem {
    public TreeItem methodName;
    public TreeItem scriptObject;
    public List<TreeItem> arguments;

    public CallMethodTreeItem(Action instruction, TreeItem scriptObject, TreeItem methodName, List<TreeItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.methodName = methodName;
        this.arguments = arguments;
        this.scriptObject = scriptObject;
    }

    @Override
    public String toString(ConstantPool constants) {
        String paramStr = "";
        for (int t = 0; t < arguments.size(); t++) {
            if (t > 0) paramStr += ",";
            paramStr += arguments.get(t).toString(constants);
        }
        boolean blankMethod = false;
        if (methodName instanceof DirectValueTreeItem) {
            if (((DirectValueTreeItem) methodName).value instanceof Undefined) {
                blankMethod = true;
            }
            if (((DirectValueTreeItem) methodName).value instanceof String) {
                if (((DirectValueTreeItem) methodName).value.equals("")) {
                    blankMethod = true;
                }
            }
        }
        if (blankMethod) {
            return scriptObject.toString(constants) + "(" + paramStr + ")";
        }
        return scriptObject.toString(constants) + "." + stripQuotes(methodName) + "(" + paramStr + ")";
    }
}