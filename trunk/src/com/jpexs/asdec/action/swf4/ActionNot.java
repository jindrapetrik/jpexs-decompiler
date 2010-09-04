package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.NotTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionNot extends Action {

    public ActionNot() {
        super(0x12, 0);
    }

    @Override
    public String toString() {
        return "Not";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        stack.push(new NotTreeItem(this, a));
    }
}
