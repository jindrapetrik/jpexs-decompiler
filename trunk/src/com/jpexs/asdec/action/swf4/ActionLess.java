package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.LtTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionLess extends Action {

    public ActionLess() {
        super(0x0F, 0);
    }

    @Override
    public String toString() {
        return "Less";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new LtTreeItem(this, b, a));
    }
}
