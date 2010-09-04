package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.RandomNumberTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionRandomNumber extends Action {

    public ActionRandomNumber() {
        super(0x30, 0);
    }

    @Override
    public String toString() {
        return "RandomNumber";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem maximum = stack.pop();
        stack.push(new RandomNumberTreeItem(this, maximum));
    }
}