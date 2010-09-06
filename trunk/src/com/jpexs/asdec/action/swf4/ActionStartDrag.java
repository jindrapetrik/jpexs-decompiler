package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.DirectValueTreeItem;
import com.jpexs.asdec.action.treemodel.StartDragTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionStartDrag extends Action {

    public ActionStartDrag() {
        super(0x27, 0);
    }

    @Override
    public String toString() {
        return "StartDrag";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem target = stack.pop();
        TreeItem lockCenter = stack.pop();
        TreeItem constrain = stack.pop();

        boolean hasConstrains = true;
        if (constrain instanceof DirectValueTreeItem) {
            if (((DirectValueTreeItem) constrain).value instanceof Long) {
                if (((long) (Long) ((DirectValueTreeItem) constrain).value) == 0) {
                    hasConstrains = false;
                }
            }
            if (((DirectValueTreeItem) constrain).value instanceof Boolean) {
                if (((boolean) (Boolean) ((DirectValueTreeItem) constrain).value) == false) {
                    hasConstrains = false;
                }
            }
        }
        TreeItem x1 = null;
        TreeItem y1 = null;
        TreeItem x2 = null;
        TreeItem y2 = null;
        if (hasConstrains) {
            y2 = stack.pop();
            x2 = stack.pop();
            y1 = stack.pop();
            x1 = stack.pop();
        }
        output.add(new StartDragTreeItem(this, target, lockCenter, constrain, x1, y1, x2, y2));
    }
}