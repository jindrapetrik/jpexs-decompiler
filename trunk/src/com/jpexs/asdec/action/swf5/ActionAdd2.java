package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.AddTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionAdd2 extends Action {
    public ActionAdd2() {
        super(0x47, 0);
    }

    @Override
    public String toString() {
        return "Add2";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new AddTreeItem(this, b, a));
    }
}