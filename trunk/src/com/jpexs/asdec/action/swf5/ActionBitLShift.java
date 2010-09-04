package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.LShiftTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionBitLShift extends Action {
    public ActionBitLShift() {
        super(0x63, 0);
    }

    @Override
    public String toString() {
        return "BitLShift";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new LShiftTreeItem(this, b, a));
    }
}