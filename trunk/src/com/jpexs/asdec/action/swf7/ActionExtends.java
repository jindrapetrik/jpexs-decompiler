package com.jpexs.asdec.action.swf7;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ExtendsTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionExtends extends Action {

    public ActionExtends() {
        super(0x69, 0);
    }

    @Override
    public String toString() {
        return "Extends";
    }

    @Override

    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem superclass = stack.pop();
        TreeItem subclass = stack.pop();
        output.add(new ExtendsTreeItem(this, subclass, superclass));
    }
}