package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.StringLengthTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionStringLength extends Action {

    public ActionStringLength() {
        super(0x14, 0);
    }

    @Override
    public String toString() {
        return "StringLength";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        stack.push(new StringLengthTreeItem(this, a));
    }
}
