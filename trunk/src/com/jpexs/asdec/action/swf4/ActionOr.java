package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.OrTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionOr extends Action {

    public ActionOr() {
        super(0x11, 0);
    }

    @Override
    public String toString() {
        return "Or";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new OrTreeItem(this, b, a));
    }
}
