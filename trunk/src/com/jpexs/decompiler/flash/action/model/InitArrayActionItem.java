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

import com.jpexs.decompiler.flash.action.swf5.ActionInitArray;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

public class InitArrayActionItem extends ActionItem {

    public List<GraphTargetItem> values;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.addAll(values);
        return ret;
    }

    public InitArrayActionItem(GraphSourceItem instruction, List<GraphTargetItem> values) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.values = values;
    }

    @Override
    protected GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        writer.append("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                writer.append(",");
            }
            values.get(i).toString(writer, localData);
        }
        return writer.append("]");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        for (GraphTargetItem value : values) {
            ret.addAll(value.getNeededSources());
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, toSourceCall(localData, generator, values), new ActionInitArray());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
