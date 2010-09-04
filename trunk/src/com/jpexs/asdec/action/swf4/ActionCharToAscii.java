package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.CharToAsciiTreeItem;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionCharToAscii extends Action {

    public ActionCharToAscii() {
        super(0x32, 0);
    }

    @Override
    public String toString() {
        return "CharToAscii";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        stack.push(new CharToAsciiTreeItem(this, a));
    }
}
