/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class BinaryOpItem extends GraphTargetItem implements BinaryOp {

    public GraphTargetItem leftSide;

    public GraphTargetItem rightSide;

    protected final String operator;

    protected String coerceLeft;
    protected String coerceRight;

    @Override
    public GraphPart getFirstPart() {
        GraphPart fp = leftSide.getFirstPart();
        if (fp == null) {
            return super.getFirstPart();
        }
        return fp;
    }

    public BinaryOpItem(GraphSourceItem instruction, GraphSourceItem lineStartItem, int precedence, GraphTargetItem leftSide, GraphTargetItem rightSide, String operator, String coerceLeft, String coerceRight) {
        super(instruction, lineStartItem, precedence);
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
        return simplifySomething(r, implicitCoerce);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        int leftPrecedence = leftSide.getPrecedence();
        if (leftPrecedence > precedence && leftPrecedence != GraphTargetItem.NOPRECEDENCE) {
            writer.append("(");
            leftSide.toString(writer, localData, coerceLeft);
            writer.append(")");
        } else {
            leftSide.toString(writer, localData, coerceLeft);
        }

        writer.append(" ");
        writer.append(operator);
        writer.append(" ");

        int rightPrecedence = rightSide.getPrecedence();
        if (rightPrecedence >= precedence && rightPrecedence != GraphTargetItem.NOPRECEDENCE) {
            writer.append("(");
            rightSide.toString(writer, localData, coerceRight);
            writer.append(")");
        } else {
            rightSide.toString(writer, localData, coerceRight);
        }
        return writer;
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
        dependencies.add(leftSide);
        if (leftSide != rightSide && dependencies.contains(rightSide)) {
            return false;
        }
        dependencies.add(rightSide);
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

    public GraphTargetItem getLeftMostItem(GraphTargetItem item) {
        GraphTargetItem ret = item;
        if (ret instanceof BinaryOpItem) {
            ret = ((BinaryOpItem) ret).getLeftMostItem();
        }
        return ret;
    }

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
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(getLeftSide());
        ret.add(getRightSide());
        return ret;
    }
}
