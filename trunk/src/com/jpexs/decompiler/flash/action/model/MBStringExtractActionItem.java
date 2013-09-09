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

import com.jpexs.decompiler.flash.action.swf4.ActionMBStringExtract;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class MBStringExtractActionItem extends ActionItem {

    //public GraphTargetItem value;
    public GraphTargetItem index;
    public GraphTargetItem count;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(value);
        ret.add(index);
        ret.add(count);
        return ret;
    }

    public MBStringExtractActionItem(GraphSourceItem instruction, GraphTargetItem value, GraphTargetItem index, GraphTargetItem count) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
        this.index = index;
        this.count = count;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        return hilight("mbsubstring(", highlight) + value.toString(highlight, constants) + hilight(",", highlight) + index.toString(highlight, constants) + hilight(",", highlight) + count.toString(highlight, constants) + hilight(")", highlight);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        ret.addAll(index.getNeededSources());
        ret.addAll(count.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, value, index, count, new ActionMBStringExtract());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
