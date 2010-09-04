package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.DeleteTreeItem;
import com.jpexs.asdec.action.treemodel.DirectValueTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionDelete2 extends Action {

    public ActionDelete2() {
        super(0x3B, 0);
    }

    @Override
    public String toString() {
        return "Delete2";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem propertyName = stack.pop();

        output.add(new DeleteTreeItem(this, null, propertyName));
        stack.push(new DirectValueTreeItem(this, Boolean.TRUE, constants));
    }
}