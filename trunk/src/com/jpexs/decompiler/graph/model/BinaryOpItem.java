/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.ArrayList;
import java.util.List;

public abstract class BinaryOpItem extends GraphTargetItem implements BinaryOp {

    public GraphTargetItem leftSide;
    public GraphTargetItem rightSide;
    protected String operator = "";

    @Override
    public GraphPart getFirstPart() {
        GraphPart fp = leftSide.getFirstPart();
        if (fp == null) {
            return super.getFirstPart();
        }
        return fp;
    }

    public BinaryOpItem(GraphSourceItem instruction, int precedence, GraphTargetItem leftSide, GraphTargetItem rightSide, String operator) {
        super(instruction, precedence);
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.operator = operator;
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        if (leftSide.getPrecedence() > precedence) {
            hilight("(", writer);
            leftSide.toString(writer, localData);
            hilight(")", writer);
        } else {
            leftSide.toString(writer, localData);
        }

        hilight(" ", writer);
        hilight(operator, writer);
        hilight(" ", writer);

        if (rightSide.getPrecedence() > precedence) {
            hilight("(", writer);
            rightSide.toString(writer, localData);
            hilight(")", writer);
        } else {
            rightSide.toString(writer, localData);
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
    public boolean isCompileTime() {
        return leftSide.isCompileTime() && rightSide.isCompileTime();
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
        hash = 71 * hash + (this.leftSide != null ? this.leftSide.hashCode() : 0);
        hash = 71 * hash + (this.rightSide != null ? this.rightSide.hashCode() : 0);
        hash = 71 * hash + (this.operator != null ? this.operator.hashCode() : 0);
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
        if (this.leftSide != other.leftSide && (this.leftSide == null || !this.leftSide.equals(other.leftSide))) {
            return false;
        }
        if (this.rightSide != other.rightSide && (this.rightSide == null || !this.rightSide.equals(other.rightSide))) {
            return false;
        }
        if ((this.operator == null) ? (other.operator != null) : !this.operator.equals(other.operator)) {
            return false;
        }
        return true;
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
