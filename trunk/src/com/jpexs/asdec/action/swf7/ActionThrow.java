package com.jpexs.asdec.action.swf7;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ThrowTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionThrow extends Action {

    public ActionThrow() {
        super(0x2A, 0);
    }

    @Override
    public String toString() {
        return "Throw";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem object = stack.pop();
        output.add(new ThrowTreeItem(this, object));
    }
}