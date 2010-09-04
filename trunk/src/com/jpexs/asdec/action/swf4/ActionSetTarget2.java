package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SetTarget2TreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionSetTarget2 extends Action {

    public ActionSetTarget2() {
        super(0x20, 0);
    }

    @Override
    public String toString() {
        return "SetTarget2";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem target = stack.pop();
        output.add(new SetTarget2TreeItem(this, target));
    }
}
