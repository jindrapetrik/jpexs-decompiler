package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ToStringTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionToString extends Action {

    public ActionToString() {
        super(0x4B, 0);
    }

    @Override
    public String toString() {
        return "ToString";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem object = stack.pop();
        stack.push(new ToStringTreeItem(this, object));
    }
}