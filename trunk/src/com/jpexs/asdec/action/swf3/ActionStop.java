package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SimpleActionTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionStop extends Action {

    public ActionStop() {
        super(0x07, 0);
    }

    @Override
    public String toString() {
        return "Stop";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        output.add(new SimpleActionTreeItem(this, "stop();"));
    }
}
