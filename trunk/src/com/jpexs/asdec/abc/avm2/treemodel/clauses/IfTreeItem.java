/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.ContinueTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;


public class IfTreeItem extends TreeItem implements Block {
    public TreeItem expression;
    public List<TreeItem> onTrue;
    public List<TreeItem> onFalse;

    public IfTreeItem(AVM2Instruction instruction, TreeItem expression, List<TreeItem> onTrue, List<TreeItem> onFalse) {
        super(instruction, NOPRECEDENCE);
        this.expression = expression;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        ret = hilight("if(") + expression.toString(constants) + hilight(")\r\n{\r\n");
        for (TreeItem ti : onTrue) {
            ret += ti.toString(constants) + "\r\n";
        }
        ret += hilight("}");
        if (onFalse.size() > 0) {
            ret += hilight("\r\nelse\r\n{\r\n");
            for (TreeItem ti : onFalse) {
                ret += ti.toString(constants) + "\r\n";
            }
            ret += hilight("}");
        }
        return ret;
    }

    public List<ContinueTreeItem> getContinues() {
        List<ContinueTreeItem> ret = new ArrayList<ContinueTreeItem>();
        for (TreeItem ti : onTrue) {
            if (ti instanceof ContinueTreeItem) {
                ret.add((ContinueTreeItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        for (TreeItem ti : onFalse) {
            if (ti instanceof ContinueTreeItem) {
                ret.add((ContinueTreeItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        return ret;
    }


}
