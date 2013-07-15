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
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf5.ActionLess2;
import com.jpexs.decompiler.flash.ecma.*;
import com.jpexs.decompiler.flash.graph.BinaryOpItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.LogicalOpItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import java.util.List;

public class GeTreeItem extends BinaryOpItem implements LogicalOpItem, Inverted {

    boolean version2;

    public GeTreeItem(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide, boolean version2) {
        super(instruction, PRECEDENCE_RELATIONAL, leftSide, rightSide, ">=");
        this.version2 = version2;
    }

    @Override
    public Object getResult() {
        if (version2) {
            Object ret = EcmaScript.compare(leftSide.getResult(), rightSide.getResult());
            if (ret == Boolean.TRUE) {
                return Boolean.FALSE;
            }
            if (ret == Boolean.FALSE) {
                return Boolean.TRUE;
            }
            return ret;//undefined
        } else {
            //For SWF 4 and older, it should return 1 or 0
            return Action.toFloatPoint(leftSide.getResult()) >= Action.toFloatPoint(rightSide.getResult());
        }
    }

    @Override
    public GraphTargetItem invert() {
        return new LtTreeItem(src, leftSide, rightSide, version2);
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, leftSide, rightSide, new ActionLess2(), new ActionNot());
    }
}
