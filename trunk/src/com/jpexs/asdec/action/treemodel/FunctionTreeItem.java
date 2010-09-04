package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

import java.util.List;

public class FunctionTreeItem extends TreeItem {
    public List<TreeItem> actions;
    public List<String> constants;

    public FunctionTreeItem(Action instruction, List<TreeItem> actions,ConstantPool constants) {
        super(instruction,PRECEDENCE_PRIMARY);
        this.actions=actions;
        this.constants=constants.constants;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "function()\r\n{\r\n"+Action.treeToString(actions)+"}";
    }
}
