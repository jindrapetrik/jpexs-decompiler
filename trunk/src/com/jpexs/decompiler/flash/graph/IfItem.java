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

import java.util.ArrayList;
import java.util.List;

public class IfItem extends GraphTargetItem implements Block {

    public GraphTargetItem expression;
    public List<GraphTargetItem> onTrue;
    public List<GraphTargetItem> onFalse;

    @Override
    public boolean isCompileTime() {
        return expression.isCompileTime();
    }

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(onTrue);
        ret.add(onFalse);
        return ret;
    }

    public IfItem(GraphSourceItem src, GraphTargetItem expression, List<GraphTargetItem> onTrue, List<GraphTargetItem> onFalse) {
        super(src, NOPRECEDENCE);
        this.expression = expression;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    public String toString(List<Object> localData) {
        String ret;
        GraphTargetItem expr = expression;
        List<GraphTargetItem> ifBranch = onTrue;
        List<GraphTargetItem> elseBranch = onFalse;
        if (onTrue.isEmpty()) {
            if (expr instanceof LogicalOpItem) {
                expr = ((LogicalOpItem) expr).invert();
            } else {
                expr = new NotItem(null, expr);
            }
            ifBranch = onFalse;
            elseBranch = onTrue;
        }
        ret = hilight("if(") + expr.toString(localData) + hilight(")") + "\r\n{\r\n";
        for (GraphTargetItem ti : ifBranch) {
            if (!ti.isEmpty()) {
                ret += ti.toStringSemicoloned(localData) + "\r\n";
            }
        }
        ret += hilight("}");
        if (onFalse.size() > 0) {
            ret += "\r\n" + hilight("else") + "\r\n" + hilight("{") + "\r\n";
            for (GraphTargetItem ti : elseBranch) {
                if (!ti.isEmpty()) {
                    ret += ti.toStringSemicoloned(localData) + "\r\n";
                }
            }
            ret += hilight("}");
        }
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        for (GraphTargetItem ti : onTrue) {
            if (ti instanceof ContinueItem) {
                ret.add((ContinueItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        for (GraphTargetItem ti : onFalse) {
            if (ti instanceof ContinueItem) {
                ret.add((ContinueItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        return ret;
    }
}
