package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SimpleActionTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionPlay extends Action {
    public ActionPlay() {
        super(0x06, 0);
    }

    @Override
    public String toString() {
        return "Play";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        output.add(new SimpleActionTreeItem(this, "Play();"));
    }
}
