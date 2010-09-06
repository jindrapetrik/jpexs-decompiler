package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.InitArrayTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionInitArray extends Action {

    public ActionInitArray() {
        super(0x42, 0);
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        long numArgs = popLong(stack);
        List<TreeItem> args = new ArrayList<TreeItem>();
        for (int l = 0; l < numArgs; l++) {
            args.add(stack.pop());
        }
        stack.push(new InitArrayTreeItem(this, args));
    }

    @Override
    public String toString() {
        return "InitArray";
    }
}