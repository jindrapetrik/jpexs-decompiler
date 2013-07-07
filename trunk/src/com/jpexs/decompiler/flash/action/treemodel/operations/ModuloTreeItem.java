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

import com.jpexs.decompiler.flash.ecma.*;
import com.jpexs.decompiler.flash.graph.BinaryOpItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;

public class ModuloTreeItem extends BinaryOpItem {

    public ModuloTreeItem(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, PRECEDENCE_MULTIPLICATIVE, leftSide, rightSide, "%");
    }

    @Override
    public Object getResult() {
        if (Double.compare(EcmaScript.toNumber(rightSide.getResult()), 0) == 0) {
            return Double.NaN;
        }
        return ((long) (double) EcmaScript.toNumber(leftSide.getResult())) % ((long) (double) EcmaScript.toNumber(rightSide.getResult()));
    }
}
