package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.MultiplyTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionMultiply extends Action {

    public ActionMultiply() {
        super(0x0C, 0);
    }

    @Override
    public String toString() {
        return "Multiply";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new MultiplyTreeItem(this, b, a));
    }
}
