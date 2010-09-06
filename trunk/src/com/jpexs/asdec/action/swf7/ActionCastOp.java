package com.jpexs.asdec.action.swf7;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.CastOpTreeItem;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionCastOp extends Action {

    public ActionCastOp() {
        super(0x2B, 0);
    }

    @Override
    public String toString() {
        return "CastOp";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem object = stack.pop();
        TreeItem constructor = stack.pop();
        stack.push(new CastOpTreeItem(this, constructor, object));
    }
}