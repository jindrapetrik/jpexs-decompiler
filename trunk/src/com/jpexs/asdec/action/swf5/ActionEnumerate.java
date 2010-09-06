package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.EnumerateTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionEnumerate extends Action {

    public ActionEnumerate() {
        super(0x46, 0);
    }

    @Override
    public String toString() {
        return "Enumerate";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem object = stack.pop();
        stack.push(new EnumerateTreeItem(this, object));
    }
}