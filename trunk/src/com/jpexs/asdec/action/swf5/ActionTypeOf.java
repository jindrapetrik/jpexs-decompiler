package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.TypeOfTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionTypeOf extends Action {

    public ActionTypeOf() {
        super(0x44, 0);
    }

    @Override
    public String toString() {
        return "TypeOf";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem object = stack.pop();
        stack.push(new TypeOfTreeItem(this, object));
    }
}