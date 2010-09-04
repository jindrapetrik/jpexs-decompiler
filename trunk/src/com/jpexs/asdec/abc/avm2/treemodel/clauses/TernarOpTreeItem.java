/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public class TernarOpTreeItem extends TreeItem {
    public TreeItem expression;
    public TreeItem onTrue;
    public TreeItem onFalse;

    public TernarOpTreeItem(AVM2Instruction instruction, TreeItem expression, TreeItem onTrue, TreeItem onFalse) {
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
