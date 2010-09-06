package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionPushDuplicate extends Action {

    public ActionPushDuplicate() {
        super(0x4C, 0);
    }

    @Override
    public String toString() {
        return "PushDuplicate";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem value = stack.pop();
        stack.push(value);
        stack.push(value);
    }
}