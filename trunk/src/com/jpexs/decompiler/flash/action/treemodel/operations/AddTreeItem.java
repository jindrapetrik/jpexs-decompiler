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
package com.jpexs.decompiler.flash.action.treemodel.operations;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.ecma.*;
import com.jpexs.decompiler.flash.graph.BinaryOpItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;

public class AddTreeItem extends BinaryOpItem {

    boolean version2;

    public AddTreeItem(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide, boolean version2) {
        super(instruction, PRECEDENCE_ADDITIVE, leftSide, rightSide, "+");
        this.version2 = version2;
    }

    @Override
    public Object getResult() {
        if (version2) {
            if (EcmaScript.type(leftSide.getResult()) == EcmaType.STRING || EcmaScript.type(rightSide.getResult()) == EcmaType.STRING) {
                return leftSide.getResult().toString() + rightSide.getResult().toString();
            }
            return EcmaScript.toNumber(leftSide.getResult()) + EcmaScript.toNumber(rightSide.getResult());
        } else {
            return Action.toFloatPoint(leftSide.getResult()) + Action.toFloatPoint(rightSide.getResult());
        }
    }
}
