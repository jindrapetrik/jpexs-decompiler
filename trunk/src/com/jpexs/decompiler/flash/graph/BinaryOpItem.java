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
package com.jpexs.decompiler.flash.graph;

import java.util.List;

public abstract class BinaryOpItem extends GraphTargetItem {

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
    public String toString(List<Object> localData) {
        String ret = "";
        if (leftSide.getPrecedence() > precedence) {
            ret += "(" + leftSide.toString(localData) + ")";
        } else {
            ret += leftSide.toString(localData);
        }
        ret += hilight(operator);

        if (rightSide.getPrecedence() > precedence) {
            ret += "(" + rightSide.toString(localData) + ")";
        } else {
            ret += rightSide.toString(localData);
        }
        return ret;
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
}
