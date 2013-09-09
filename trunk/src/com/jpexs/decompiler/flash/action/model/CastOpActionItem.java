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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.action.swf7.ActionCastOp;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;

public class CastOpActionItem extends ActionItem {

    public GraphTargetItem constructor;
    public GraphTargetItem object;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(constructor);
        ret.add(object);
        return ret;
    }

    public CastOpActionItem(GraphSourceItem instruction, GraphTargetItem constructor, GraphTargetItem object) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.constructor = constructor;
        this.object = object;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        return hilight("(", highlight) + stripQuotes(constructor, constants, highlight) + hilight(")", highlight) + object.toString(highlight, Helper.toList(constants));
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(constructor.getNeededSources());
        ret.addAll(object.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, constructor, object, new ActionCastOp());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
