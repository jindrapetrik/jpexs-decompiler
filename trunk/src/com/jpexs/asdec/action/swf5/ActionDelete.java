package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.DeleteTreeItem;
import com.jpexs.asdec.action.treemodel.DirectValueTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionDelete extends Action {

    public ActionDelete() {
        super(0x3A, 0);
    }

    @Override
    public String toString() {
        return "Delete";
    }


    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem propertyName = stack.pop();
        TreeItem object = stack.pop();

        output.add(new DeleteTreeItem(this, object, propertyName));
        stack.push(new DirectValueTreeItem(this, Boolean.TRUE, constants));
    }
}