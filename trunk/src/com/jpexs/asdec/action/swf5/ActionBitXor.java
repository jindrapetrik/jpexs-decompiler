package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.BitXorTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionBitXor extends Action {

    public ActionBitXor() {
        super(0x62, 0);
    }

    @Override
    public String toString() {
        return "BitXor";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new BitXorTreeItem(this, b, a));
    }
}