/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.operations;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public abstract class UnaryOpTreeItem extends TreeItem {
    public TreeItem value;
    public String operator;

    public UnaryOpTreeItem(AVM2Instruction instruction, int precedence, TreeItem value, String operator) {
        super(instruction, precedence);
        this.value = value;
        this.operator = operator;
    }

    @Override
    public String toString(ConstantPool constants) {
        String s = value.toString(constants);
        if (value.precedence > precedence) s = "(" + s + ")";
        return hilight(operator) + s;
    }
}
