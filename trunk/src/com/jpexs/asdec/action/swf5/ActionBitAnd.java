package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.BitAndTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionBitAnd extends Action {

    public ActionBitAnd() {
        super(0x60, 0);
    }

    @Override
    public String toString() {
        return "BitAnd";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new BitAndTreeItem(this, b, a));
    }
}