package com.jpexs.asdec.action.swf7;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ImplementsOpTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionImplementsOp extends Action {

    public ActionImplementsOp() {
        super(0x2C, 0);
    }

    @Override
    public String toString() {
        return "ImplementsOp";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem subclass = stack.pop();
        long inCount = popLong(stack);
        List<TreeItem> superclasses = new ArrayList<TreeItem>();
        for (long l = 0; l < inCount; l++) {
            superclasses.add(stack.pop());
        }
        output.add(new ImplementsOpTreeItem(this, subclass, superclasses));
    }
}