package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SetMemberTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionSetMember extends Action {

    public ActionSetMember() {
        super(0x4F, 0);
    }

    @Override
    public String toString() {
        return "SetMember";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem value = stack.pop();
        TreeItem objectName = stack.pop();
        TreeItem object = stack.pop();
        output.add(new SetMemberTreeItem(this, object, objectName, value));
    }
}