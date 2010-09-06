package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.StringExtractTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

public class ActionStringExtract extends Action {

    public ActionStringExtract() {
        super(0x15, 0);
    }

    @Override
    public String toString() {
        return "StringExtract";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem count = stack.pop();
        TreeItem index = stack.pop();
        TreeItem value = stack.pop();
        stack.push(new StringExtractTreeItem(this, value, index, count));
    }
}
