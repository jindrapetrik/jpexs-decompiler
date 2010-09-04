package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SimpleActionTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionStopSounds extends Action {
    public ActionStopSounds() {
        super(0x09, 0);
    }

    @Override
    public String toString() {
        return "StopSounds";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        output.add(new SimpleActionTreeItem(this, "stopAllSounds();"));
    }
}
