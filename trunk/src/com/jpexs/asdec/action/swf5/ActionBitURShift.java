package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.URShiftTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionBitURShift extends Action {
    public ActionBitURShift() {
        super(0x65, 0);
    }

    @Override
    public String toString() {
        return "BitURShift";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new URShiftTreeItem(this, b, a));
    }
}