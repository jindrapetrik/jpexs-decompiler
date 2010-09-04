/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.clauses;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;


public class TernarOpTreeItem extends TreeItem {
    public TreeItem expression;
    public TreeItem onTrue;
    public TreeItem onFalse;

    public TernarOpTreeItem(Action instruction, TreeItem expression, TreeItem onTrue, TreeItem onFalse) {
        super(instruction, PRECEDENCE_CONDITIONAL);
        this.expression = expression;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    public String toString(ConstantPool constants) {
        return expression.toString(constants) + hilight("?") + onTrue.toString(constants) + hilight(":") + onFalse.toString(constants);
    }


}
