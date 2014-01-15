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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf4.ActionAnd;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import java.util.List;

public class AndActionItem extends BinaryOpItem {

    public AndActionItem(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, PRECEDENCE_LOGICALAND, leftSide, rightSide, "&&");
    }

    @Override
    public Object getResult() {
        return EcmaScript.toBoolean(leftSide.getResult()) && EcmaScript.toBoolean(rightSide.getResult());
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, leftSide, rightSide, new ActionAnd());
    }
}
