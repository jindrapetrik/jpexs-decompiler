/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SimpleValue;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Binary operation. Operation on two operands.
 *
 * @author JPEXS
 */
public abstract class BinaryOpItem extends GraphTargetItem implements BinaryOp {

    /**
     * Left side of the operation
     */
    public GraphTargetItem leftSide;

    /**
     * Right side of the operation
     */
    public GraphTargetItem rightSide;

    /**
     * Operator
     */
    protected final String operator;

    /**
     * Coerce left side to this type
     */
    protected String coerceLeft;

    /**
     * Coerce right side to this type
     */
    protected String coerceRight;

    @Override
    public GraphPart getFirstPart() {
        GraphPart fp = leftSide.getFirstPart();
        if (fp == null) {
            return super.getFirstPart();
        }
        return fp;
    }

    /**
     * Constructor.
     *
     * @param dialect Dialect
     * @param instruction Instruction
     * @param lineStartItem Line start item
     * @param precedence Precedence
     * @param leftSide Left side
     * @param rightSide Right side
     * @param operator Operator
     * @param coerceLeft Coerce left
     * @param coerceRight Coerce right
     */
    public BinaryOpItem(GraphTargetDialect dialect, GraphSourceItem instruction, GraphSourceItem lineStartItem, int precedence, GraphTargetItem leftSide, GraphTargetItem rightSide, String operator, String coerceLeft, String coerceRight) {
        super(dialect, instruction, lineStartItem, precedence);
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.operator = operator;
        this.coerceLeft = coerceLeft;
        this.coerceRight = coerceRight;
    }

    @Override
    public GraphTargetItem simplify(String implicitCoerce) {
        BinaryOpItem r = (BinaryOpItem) clone();
        r.leftSide = r.leftSide.simplify(coerceLeft);                
        r.rightSide = r.rightSide.simplify(coerceRight);
        
        if (r.leftSide == this.leftSide
                && r.rightSide == this.rightSide) {
            r = this;
        }
        
        return simplifySomething(r, implicitCoerce);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        int leftPrecedence = leftSide.getPrecedence();
        if (leftPrecedence > precedence && leftPrecedence != GraphTargetItem.NOPRECEDENCE) {
            writer.append("(");
            operandToString(leftSide, writer, localData);
            writer.append(")");
        } else {
            operandToString(leftSide, writer, localData);
        }

        writer.append(" ");
        writer.append(operator);
        writer.append(" ");

        int rightPrecedence = rightSide.getPrecedence();
        if (rightPrecedence >= precedence && rightPrecedence != GraphTargetItem.NOPRECEDENCE) {
            writer.append("(");
            operandToString(rightSide, writer, localData);
            writer.append(")");
        } else {
            operandToString(rightSide, writer, localData);
        }
        return writer;
    }

    /**
     * Converts operand to string
     *
     * @param operand Operand
     * @param writer Writer
     * @param localData Local data
     * @throws InterruptedException On interrupt
     */
    protected void operandToString(GraphTargetItem operand, GraphTextWriter writer, LocalData localData) throws InterruptedException {
        operand.toString(writer, localData, "");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(leftSide.getNeededSources());
        ret.addAll(rightSide.getNeededSources());
        return ret;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(leftSide)) {
            return false;
        }
        if (!((leftSide instanceof SimpleValue) && ((SimpleValue) leftSide).isSimpleValue())) {
            dependencies.add(leftSide);
        }
        if (leftSide != rightSide && dependencies.contains(rightSide)) {
            return false;
        }
        if (!((rightSide instanceof SimpleValue) && ((SimpleValue) rightSide).isSimpleValue())) {
            dependencies.add(rightSide);
        }
        return leftSide.isConvertedCompileTime(dependencies) && rightSide.isConvertedCompileTime(dependencies);
    }

    @Override
    public boolean isVariableComputed() {
        return leftSide.isVariableComputed() || rightSide.isVariableComputed();
    }

    @Override
    public boolean hasSideEffect() {
        return leftSide.hasSideEffect() || rightSide.hasSideEffect();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(leftSide);
        hash = 71 * hash + Objects.hashCode(rightSide);
        hash = 71 * hash + Objects.hashCode(operator);
        return hash;
    }

    /**
     * Gets left most item
     *
     * @param item Item
     * @return Left most item
     */
    public GraphTargetItem getLeftMostItem(GraphTargetItem item) {
        GraphTargetItem ret = item;
        if (ret instanceof BinaryOpItem) {
            ret = ((BinaryOpItem) ret).getLeftMostItem();
        }
        return ret;
    }

    /**
     * Gets left most item
     *
     * @return Left most item
     */
    public GraphTargetItem getLeftMostItem() {
        GraphTargetItem ret = leftSide;
        if (ret instanceof BinaryOpItem) {
            ret = ((BinaryOpItem) ret).getLeftMostItem();
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BinaryOpItem other = (BinaryOpItem) obj;
        if (!Objects.equals(leftSide, other.leftSide)) {
            return false;
        }
        if (!Objects.equals(rightSide, other.rightSide)) {
            return false;
        }
        return (Objects.equals(operator, other.operator));
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BinaryOpItem other = (BinaryOpItem) obj;
        if (!GraphTargetItem.objectsValueEquals(leftSide, other.leftSide)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(rightSide, other.rightSide)) {
            return false;
        }
        return GraphTargetItem.objectsValueEquals(operator, other.operator);
    }

    /*@Override
     public boolean toBoolean() {
     double val=toNumber();
     if(Double.isNaN(val)){
     return false;
     }
     if(Double.compare(val, 0)==0){
     return false;
     }
     return true;
     }*/
    @Override
    public GraphTargetItem getLeftSide() {
        return leftSide;
    }

    @Override
    public GraphTargetItem getRightSide() {
        return rightSide;
    }

    @Override
    public void setLeftSide(GraphTargetItem value) {
        leftSide = value;
    }

    @Override
    public void setRightSide(GraphTargetItem value) {
        rightSide = value;
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(getLeftSide());
        visitor.visit(getRightSide());
    }

    @Override
    public String getOperator() {
        return operator;
    }
}
