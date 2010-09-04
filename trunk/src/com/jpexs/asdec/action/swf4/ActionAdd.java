package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.AddTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionAdd extends Action {

    public ActionAdd() {
        super(0x0A, 0);
    }

    @Override
    public String toString() {
        return "Add";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new AddTreeItem(this, b, a));
    }
}
