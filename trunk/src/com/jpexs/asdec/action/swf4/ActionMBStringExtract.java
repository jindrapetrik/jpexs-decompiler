package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.MBStringExtractTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionMBStringExtract extends Action {

    public ActionMBStringExtract() {
        super(0x35, 0);
    }

    @Override
    public String toString() {
        return "MBStringExtract";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem count = stack.pop();
        TreeItem index = stack.pop();
        TreeItem value = stack.pop();
        stack.push(new MBStringExtractTreeItem(this, value, index, count));
    }
}
