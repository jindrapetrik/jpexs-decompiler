package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.GetMemberTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionGetMember extends Action {

    public ActionGetMember() {
        super(0x4E, 0);
    }

    @Override
    public String toString() {
        return "GetMember";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem functionName = stack.pop();
        TreeItem object = stack.pop();
        stack.push(new GetMemberTreeItem(this, object, functionName));
    }
}