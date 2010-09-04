package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.EqTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionEquals extends Action {

    public ActionEquals() {
        super(0x0E, 0);
    }

    @Override
    public String toString() {
        return "Equals";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new EqTreeItem(this, b, a));
    }
}
