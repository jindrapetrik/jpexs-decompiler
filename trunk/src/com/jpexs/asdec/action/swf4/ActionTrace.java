package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TraceTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionTrace extends Action {

    public ActionTrace() {
        super(0x26, 0);
    }

    @Override
    public String toString() {
        return "Trace";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem value = stack.pop();
        output.add(new TraceTreeItem(this, value));
    }
}