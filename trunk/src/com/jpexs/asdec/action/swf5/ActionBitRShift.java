package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.RShiftTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionBitRShift extends Action {

    public ActionBitRShift() {
        super(0x64, 0);
    }

    @Override
    public String toString() {
        return "BitRShift";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new RShiftTreeItem(this, b, a));
    }
}