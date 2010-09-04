package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.MBAsciiToCharTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionMBAsciiToChar extends Action {

    public ActionMBAsciiToChar() {
        super(0x37, 0);
    }

    @Override
    public String toString() {
        return "MBAsciiToChar";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        stack.push(new MBAsciiToCharTreeItem(this, a));
    }
}