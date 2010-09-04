package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SimpleActionTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionGetTime extends Action {

    public ActionGetTime() {
        super(0x34, 0);
    }

    @Override
    public String toString() {
        return "GetTime";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        stack.push(new SimpleActionTreeItem(this, "getTimer()"));
    }
}