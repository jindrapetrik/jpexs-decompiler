package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.CallFunctionTreeItem;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionCallFunction extends Action {

    public ActionCallFunction() {
        super(0x3D, 0);
    }

    @Override
    public String toString() {
        return "CallFunction";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem functionName = stack.pop();
        long numArgs = popLong(stack);
        List<TreeItem> args = new ArrayList<TreeItem>();
        for (long l = 0; l < numArgs; l++) {
            args.add(stack.pop());
        }
        stack.push(new CallFunctionTreeItem(this, functionName, args));
    }
}