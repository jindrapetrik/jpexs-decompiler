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
package com.jpexs.decompiler.flash.action.model.operations;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.swf5.ActionAdd2;
import com.jpexs.decompiler.flash.ecma.*;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import java.util.List;

public class AddActionItem extends BinaryOpItem {

    boolean version2;

    public AddActionItem(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide, boolean version2) {
        super(instruction, PRECEDENCE_ADDITIVE, leftSide, rightSide, "+");
        this.version2 = version2;
    }

    @Override
    public String toString(List<Object> localData) {
        if (rightSide.precedence >= precedence) { //string + vs number +
            String ret = "";
            if (leftSide.precedence > precedence) {
                ret += "(" + leftSide.toString(localData) + ")";
            } else {
                ret += leftSide.toString(localData);
            }
            ret += " ";
            ret += hilight(operator);
            ret += " ";
            ret += "(" + rightSide.toString(localData) + ")";
            return ret;
        } else {
            return super.toString(localData);
        }
    }

    @Override
    public Object getResult() {
        if (version2) {
            if (EcmaScript.type(leftSide.getResult()) == EcmaType.STRING || EcmaScript.type(rightSide.getResult()) == EcmaType.STRING) {
                return ""+leftSide.getResult() + rightSide.getResult();
            }
            return EcmaScript.toNumber(leftSide.getResult()) + EcmaScript.toNumber(rightSide.getResult());
        } else {
            return Action.toFloatPoint(leftSide.getResult()) + Action.toFloatPoint(rightSide.getResult());
        }
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, leftSide, rightSide, new ActionAdd2());
    }
}
