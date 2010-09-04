/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.ArrayList;
import java.util.List;


public class WithTreeItem extends TreeItem {
    public TreeItem scope;
    public List<TreeItem> items;

    public WithTreeItem(AVM2Instruction instruction, TreeItem scope, List<TreeItem> items) {
        super(instruction, NOPRECEDENCE);
        this.scope = scope;
        this.items = items;
    }

    public WithTreeItem(AVM2Instruction instruction, TreeItem scope) {
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
