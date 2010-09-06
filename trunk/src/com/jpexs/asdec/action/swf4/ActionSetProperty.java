package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.DirectValueTreeItem;
import com.jpexs.asdec.action.treemodel.SetPropertyTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionSetProperty extends Action {

    public ActionSetProperty() {
        super(0x23, 0);
    }

    @Override
    public String toString() {
        return "SetProperty";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem value = stack.pop();
        TreeItem index = stack.pop();
        TreeItem target = stack.pop();
        int indexInt = 0;
        if (index instanceof DirectValueTreeItem) {
            if (((DirectValueTreeItem) index).value instanceof Long) {
                indexInt = (int) (long) (Long) ((DirectValueTreeItem) index).value;
            }
        }
        output.add(new SetPropertyTreeItem(this, target, indexInt, value));
    }
}