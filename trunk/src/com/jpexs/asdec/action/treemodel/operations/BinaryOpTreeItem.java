/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;


public abstract class BinaryOpTreeItem extends TreeItem {

    public TreeItem leftSide;
    public TreeItem rightSide;
    protected String operator = "";

    public BinaryOpTreeItem(Action instruction, int precedence, TreeItem leftSide, TreeItem rightSide, String operator) {
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
