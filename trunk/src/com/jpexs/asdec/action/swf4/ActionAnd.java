package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.AndTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionAnd extends Action {

    public ActionAnd() {
        super(0x10, 0);
    }

    @Override
    public String toString() {
        return "And";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new AndTreeItem(this, b, a));
    }
}
