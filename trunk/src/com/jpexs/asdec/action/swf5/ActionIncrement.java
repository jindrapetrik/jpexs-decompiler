package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.IncrementTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionIncrement extends Action {

    public ActionIncrement() {
        super(0x50, 0);
    }

    @Override
    public String toString() {
        return "Increment";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem a = stack.pop();
        stack.push(new IncrementTreeItem(this, a));
    }
}