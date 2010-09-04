package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.StringAddTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionStringAdd extends Action {

    public ActionStringAdd() {
        super(0x21, 0);
    }

    @Override
    public String toString() {
        return "StringAdd";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new StringAddTreeItem(this, b, a));
    }
}
