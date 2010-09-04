package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SetVariableTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionSetVariable extends Action {

    public ActionSetVariable() {
        super(0x1D, 0);
    }

    @Override
    public String toString() {
        return "SetVariable";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem value = stack.pop();
        TreeItem name = stack.pop();
        output.add(new SetVariableTreeItem(this, name, value));
    }
}