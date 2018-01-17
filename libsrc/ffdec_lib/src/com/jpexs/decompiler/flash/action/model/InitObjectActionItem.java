/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf5.ActionInitObject;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class InitObjectActionItem extends ActionItem {

    public List<GraphTargetItem> names;

    public List<GraphTargetItem> values;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.addAll(names);
        ret.addAll(values);
        return ret;
    }

    public InitObjectActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, List<GraphTargetItem> names, List<GraphTargetItem> values) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.values = values;
        this.names = names;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("{");
        for (int i = values.size() - 1; i >= 0; i--) {
            if (i < values.size() - 1) {
                writer.append(",");
            }
            names.get(i).toStringNoQuotes(writer, localData); //AS1/2 do not allow quotes in name here
            writer.append(":");
            if (values.get(i) instanceof TernarOpItem) { //Ternar operator contains ":"
                writer.append("(");
                values.get(i).toString(writer, localData);
                writer.append(")");
            } else {
                values.get(i).toString(writer, localData);
            }
        }
        return writer.append("}");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        for (GraphTargetItem name : names) {
            ret.addAll(name.getNeededSources());
        }
        for (GraphTargetItem value : values) {
            ret.addAll(value.getNeededSources());
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (int i = values.size() - 1; i >= 0; i--) {
            ret.addAll(names.get(i).toSource(localData, generator));
            ret.addAll(values.get(i).toSource(localData, generator));
        }
        ret.add(new ActionPush((Long) (long) values.size()));
        ret.add(new ActionInitObject());
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
