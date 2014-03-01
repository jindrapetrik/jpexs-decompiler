/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf5.ActionDelete;
import com.jpexs.decompiler.flash.action.swf5.ActionDelete2;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

public class DeleteActionItem extends ActionItem {

    public GraphTargetItem object;
    public GraphTargetItem propertyName;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(object);
        ret.add(propertyName);
        return ret;
    }

    public DeleteActionItem(GraphSourceItem instruction, GraphTargetItem object, GraphTargetItem propertyName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("delete ");
        if (object == null) {
            return propertyName.toStringNoQuotes(writer, localData);
        }
        object.toStringNoQuotes(writer, localData);
        writer.append(".");
        return propertyName.toStringNoQuotes(writer, localData);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(object.getNeededSources());
        ret.addAll(propertyName.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if(object == null){
            return toSourceMerge(localData, generator, propertyName, new ActionDelete2());
        }
        return toSourceMerge(localData, generator, object, propertyName, new ActionDelete());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
