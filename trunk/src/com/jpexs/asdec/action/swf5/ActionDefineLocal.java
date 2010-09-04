package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.DefineLocalTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionDefineLocal extends Action {

    public ActionDefineLocal() {
        super(0x3C, 0);
    }

    @Override
    public String toString() {
        return "DefineLocal";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem value = stack.pop();
        TreeItem name = stack.pop();
        output.add(new DefineLocalTreeItem(this, name, value));
    }
}