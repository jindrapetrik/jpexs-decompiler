/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.clauses;


import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;


public class WithTreeItem extends TreeItem {
    public TreeItem scope;
    public List<TreeItem> items;

    public WithTreeItem(Action instruction, TreeItem scope, List<TreeItem> items) {
        super(instruction, NOPRECEDENCE);
        this.scope = scope;
        this.items = items;
    }

    public WithTreeItem(Action instruction, TreeItem scope) {
        super(instruction, NOPRECEDENCE);
        this.scope = scope;
        this.items = new ArrayList<TreeItem>();
    }


    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        ret = hilight("with(") + scope.toString(constants) + hilight(")\r\n{\r\n");
        for (TreeItem ti : items) {
            ret += ti.toString(constants) + "\r\n";
        }
        ret += hilight("}");
        return ret;
    }

}