package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.StringEqTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionStringEquals extends Action {

    public ActionStringEquals() {
        super(0x13, 0);
    }

    @Override
    public String toString() {
        return "StringEquals";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new StringEqTreeItem(this, b, a));
    }
}
