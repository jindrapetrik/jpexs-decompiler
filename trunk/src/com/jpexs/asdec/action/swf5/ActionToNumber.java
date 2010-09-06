package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ToNumberTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionToNumber extends Action {

    public ActionToNumber() {
        super(0x4A, 0);
    }

    @Override
    public String toString() {
        return "ToNumber";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem object = stack.pop();
        stack.push(new ToNumberTreeItem(this, object));
    }
}