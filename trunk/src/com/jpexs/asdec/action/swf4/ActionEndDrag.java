package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SimpleActionTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionEndDrag extends Action {

    public ActionEndDrag() {
        super(0x28, 0);
    }

    @Override
    public String toString() {
        return "EndDrag";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        output.add(new SimpleActionTreeItem(this, "stopDrag();"));
    }
}