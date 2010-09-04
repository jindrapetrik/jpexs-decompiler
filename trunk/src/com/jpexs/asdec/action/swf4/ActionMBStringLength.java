package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.MBStringLengthTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionMBStringLength extends Action {
    public ActionMBStringLength() {
        super(0x31, 0);
    }

    @Override
    public String toString() {
        return "MBStringLength";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        stack.push(new MBStringLengthTreeItem(this, a));
    }
}
