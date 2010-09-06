package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.DirectValueTreeItem;
import com.jpexs.asdec.action.treemodel.GetPropertyTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionGetProperty extends Action {

    public ActionGetProperty() {
        super(0x22, 0);
    }

    @Override
    public String toString() {
        return "GetProperty";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem index = stack.pop();
        TreeItem target = stack.pop();
        int indexInt = 0;
        if (index instanceof DirectValueTreeItem) {
            if (((DirectValueTreeItem) index).value instanceof Long) {
                indexInt = (int) (long) (Long) ((DirectValueTreeItem) index).value;
            }
        }
        stack.push(new GetPropertyTreeItem(this, target, indexInt));
    }
}