package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.NewMethodTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionNewMethod extends Action {

    public ActionNewMethod() {
        super(0x53, 0);
    }

    @Override
    public String toString() {
        return "NewMethod";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem methodName = stack.pop();
        TreeItem scriptObject = stack.pop();
        long numArgs = popLong(stack);
        List<TreeItem> args = new ArrayList<TreeItem>();
        for (long l = 0; l < numArgs; l++) {
            args.add(stack.pop());
        }
        stack.push(new NewMethodTreeItem(this, scriptObject, methodName, args));
    }
}