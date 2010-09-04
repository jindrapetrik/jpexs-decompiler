package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.helpers.Helper;

public class GetURLTreeItem extends TreeItem {

    public String urlString;
    public String targetString;

    public String toString(ConstantPool constants) {
        return "getUrl(\"" + Helper.escapeString(urlString) + "\", \"" + Helper.escapeString(targetString) + "\");";
    }

    public GetURLTreeItem(Action instruction, String urlString, String targetString) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.targetString = targetString;
    }
}
