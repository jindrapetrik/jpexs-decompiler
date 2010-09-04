package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.DirectValueTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.VoidTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionPop extends Action {

    public ActionPop() {
        super(0x17, 0);
    }

    @Override
    public String toString() {
        return "Pop";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem val = stack.pop();
        if (!(val instanceof DirectValueTreeItem))
            output.add(new VoidTreeItem(this, val));
    }
}
