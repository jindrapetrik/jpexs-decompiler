package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.InitObjectTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionInitObject extends Action {

    public ActionInitObject() {
        super(0x43, 0);
    }

    @Override
    public String toString() {
        return "InitObject";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        long numArgs = popLong(stack);
        List<TreeItem> values = new ArrayList<TreeItem>();
        List<TreeItem> names = new ArrayList<TreeItem>();
        for (long l = 0; l < numArgs; l++) {
            values.add(stack.pop());
            names.add(stack.pop());
        }
        stack.push(new InitObjectTreeItem(this, names, values));
    }
}