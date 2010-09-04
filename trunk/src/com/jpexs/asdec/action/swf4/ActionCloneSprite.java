package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.CloneSpriteTreeItem;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionCloneSprite extends Action {

    public ActionCloneSprite() {
        super(0x24, 0);
    }

    @Override
    public String toString() {
        return "CloneSprite";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem depth = stack.pop();
        TreeItem target = stack.pop();
        TreeItem source = stack.pop();
        output.add(new CloneSpriteTreeItem(this, source, target, depth));
    }
}
