/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.operations;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public abstract class BinaryOpTreeItem extends TreeItem {

    public TreeItem leftSide;
    public TreeItem rightSide;
    protected String operator = "";

    public BinaryOpTreeItem(AVM2Instruction instruction, int precedence, TreeItem leftSide, TreeItem rightSide, String operator) {
        super(instruction, precedence);
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.operator = operator;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        if (leftSide.precedence > precedence) {
            ret += "(" + leftSide.toString(constants) + ")";
        } else {
            ret += leftSide.toString(constants);
        }
        ret += hilight(operator);
        if (rightSide.precedence > precedence) {
            ret += "(" + rightSide.toString(constants) + ")";
        } else {
            ret += rightSide.toString(constants);
        }
        return ret;
    }


}
