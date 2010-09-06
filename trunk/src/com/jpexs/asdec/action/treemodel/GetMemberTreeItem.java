package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class GetMemberTreeItem extends TreeItem {
    public TreeItem object;
    public TreeItem functionName;

    public GetMemberTreeItem(Action instruction, TreeItem object, TreeItem functionName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.functionName = functionName;
    }

    @Override
    public String toString(ConstantPool constants) {
        if(!((functionName instanceof DirectValueTreeItem)&&(((DirectValueTreeItem)functionName).value instanceof String))){
            //if(!(functionName instanceof GetVariableTreeItem))
              return object.toString(constants) + "[" + stripQuotes(functionName)+"]";
        }
        return object.toString(constants) + "." + stripQuotes(functionName);
    }
}
