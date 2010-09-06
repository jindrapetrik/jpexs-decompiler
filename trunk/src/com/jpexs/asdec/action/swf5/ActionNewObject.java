package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.NewObjectTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionNewObject extends Action {

    public ActionNewObject() {
        super(0x40, 0);
    }

    @Override
    public String toString() {
        return "NewObject";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem objectName = stack.pop();
        long numArgs = popLong(stack);
        List<TreeItem> args = new ArrayList<TreeItem>();
        for (long l = 0; l < numArgs; l++) {
            args.add(stack.pop());
        }
        stack.push(new NewObjectTreeItem(this, objectName, args));
    }
}