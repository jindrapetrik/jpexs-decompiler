package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.operations.ModuloTreeItem;

import java.util.List;
import java.util.Stack;

public class ActionModulo extends Action {

    public ActionModulo() {
        super(0x3F, 0);
    }

    @Override
    public String toString() {
        return "Modulo";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem a = stack.pop();
        TreeItem b = stack.pop();
        stack.push(new ModuloTreeItem(this, b, a));
    }
}