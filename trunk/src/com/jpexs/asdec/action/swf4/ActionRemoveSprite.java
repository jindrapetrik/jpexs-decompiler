package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.RemoveSpriteTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionRemoveSprite extends Action {

    public ActionRemoveSprite() {
        super(0x25, 0);
    }

    @Override
    public String toString() {
        return "RemoveSprite";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem target = stack.pop();
        output.add(new RemoveSpriteTreeItem(this, target));
    }
}