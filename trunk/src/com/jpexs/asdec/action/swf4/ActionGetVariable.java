package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.GetVariableTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionGetVariable extends Action {

    public ActionGetVariable() {
        super(0x1C, 0);
    }

    @Override
    public String toString() {
        return "GetVariable";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem value=stack.pop();
        stack.push(new GetVariableTreeItem(this,value));
    }
}
