package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SimpleActionTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionNextFrame extends Action {

    public ActionNextFrame() {
        super(0x04, 0);
    }

    @Override
    public String toString() {
        return "NextFrame";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        output.add(new SimpleActionTreeItem(this, "nextFrame();"));
    }
}
