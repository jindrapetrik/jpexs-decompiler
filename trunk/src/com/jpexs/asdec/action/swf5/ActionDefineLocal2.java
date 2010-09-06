package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.DefineLocalTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionDefineLocal2 extends Action {

    public ActionDefineLocal2() {
        super(0x41, 0);
    }

    @Override
    public String toString() {
        return "DefineLocal2";
    }


    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem name = stack.pop();
        output.add(new DefineLocalTreeItem(this, name, null));
    }
}