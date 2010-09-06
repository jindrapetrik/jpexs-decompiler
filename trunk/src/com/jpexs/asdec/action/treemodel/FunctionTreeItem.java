package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

import java.util.List;

public class FunctionTreeItem extends TreeItem {
    public List<TreeItem> actions;
    public List<String> constants;
    public String functionName;
    public List<String> paramNames;

    public FunctionTreeItem(Action instruction, String functionName,List<String> paramNames,List<TreeItem> actions,ConstantPool constants) {
        super(instruction,PRECEDENCE_PRIMARY);
        this.actions=actions;
        this.constants=constants.constants;
        this.functionName=functionName;
        this.paramNames=paramNames;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret="function";
        if(!functionName.equals(""))
            ret+=" "+functionName;
        ret+="(";
        for(int p=0;p<paramNames.size();p++){
            if(p>0) ret+=", ";
            ret+=paramNames.get(p);
        }
        ret+=")\r\n{\r\n"+Action.treeToString(actions)+"}";
        return ret;
    }
}
