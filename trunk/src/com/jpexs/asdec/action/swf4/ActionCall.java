package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.CallTreeItem;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionCall extends Action {

    public ActionCall() {
        super(0x9E, 0);
    }

    @Override
    public String toString() {
        return "Call";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        output.add(new CallTreeItem(this, stack.pop()));
    }
}
