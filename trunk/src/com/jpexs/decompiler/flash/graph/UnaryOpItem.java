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

public abstract class UnaryOpItem extends GraphTargetItem implements UnaryOp {

    public GraphTargetItem value;
    public String operator;

    public UnaryOpItem(GraphSourceItem instruction, int precedence, GraphTargetItem value, String operator) {
        super(instruction, precedence);
        this.value = value;
        this.operator = operator;
    }

    @Override
    public String toString(List<Object> localData) {
        String s = (value == null ? "null" : value.toString(localData));
        if (value != null) {
            if (value.precedence > precedence) {
                s = "(" + s + ")";
            }
        }
        return hilight(operator) + s;
    }

    @Override
    public boolean isCompileTime() {
        return value.isCompileTime();
    }

    @Override
    public boolean isVariableComputed() {
        return value.isVariableComputed();
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean hasSideEffect() {
        return value.hasSideEffect();
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
