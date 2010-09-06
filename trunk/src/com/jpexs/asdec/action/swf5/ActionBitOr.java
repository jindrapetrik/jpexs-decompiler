package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.BitOrTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionBitOr extends Action {

    public ActionBitOr() {
        super(0x61, 0);
    }

    @Override
    public String toString() {
        return "BitOr";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new BitOrTreeItem(this, b, a));
    }
}