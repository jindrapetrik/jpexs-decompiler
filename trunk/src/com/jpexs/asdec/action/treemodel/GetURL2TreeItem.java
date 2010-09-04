package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class GetURL2TreeItem extends TreeItem {

    public TreeItem urlString;
    public TreeItem targetString;
    public int method;
    public boolean loadTargetFlag;
    public boolean loadVariablesFlag;


    public String toString(ConstantPool constants) {
        String methodStr = "";
        if (method == 1) methodStr = ",\"GET\"";
        if (method == 2) methodStr = ",\"POST\"";
        String prefix = "getUrl";
        if (loadVariablesFlag) prefix = "loadVariables";
        if (loadTargetFlag && (!loadVariablesFlag)) prefix = "loadMovie";

        return prefix + "(" + urlString.toString(constants) + "," + targetString.toString(constants) + methodStr + ");";
    }

    public GetURL2TreeItem(Action instruction, TreeItem urlString, TreeItem targetString, int method, boolean loadTargetFlag, boolean loadVariablesFlag) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.targetString = targetString;
        this.method = method;
        this.loadTargetFlag = loadTargetFlag;
        this.loadVariablesFlag = loadVariablesFlag;
    }
}