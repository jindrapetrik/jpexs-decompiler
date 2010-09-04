package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.SubtractTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionSubtract extends Action {

    public ActionSubtract() {
        super(0x0B, 0);
    }

    @Override
    public String toString() {
        return "Subtract";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new SubtractTreeItem(this, b, a));
    }
}
