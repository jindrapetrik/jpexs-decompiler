package com.jpexs.asdec.action.swf6;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.EnumerateTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionEnumerate2 extends Action {
    public ActionEnumerate2() {
        super(0x55, 0);
    }

    @Override
    public String toString() {
        return "Enumerate2";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem object = stack.pop();
        stack.push(new EnumerateTreeItem(this, object));
    }
}