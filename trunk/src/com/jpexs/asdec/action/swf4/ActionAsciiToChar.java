package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.AsciiToCharTreeItem;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionAsciiToChar extends Action {

    public ActionAsciiToChar() {
        super(0x33, 0);
    }

    @Override
    public String toString() {
        return "AsciiToChar";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        stack.push(new AsciiToCharTreeItem(this, a));
    }
}
