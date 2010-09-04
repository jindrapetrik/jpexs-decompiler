package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.MBCharToAsciiTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionMBCharToAscii extends Action {
    public ActionMBCharToAscii() {
        super(0x36, 0);
    }

    @Override
    public String toString() {
        return "MBCharToAscii";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        stack.push(new MBCharToAsciiTreeItem(this, a));
    }
}