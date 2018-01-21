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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class InterfaceActionItem extends ActionItem {

    public GraphTargetItem name;

    public List<GraphTargetItem> superInterfaces;

    public InterfaceActionItem(GraphTargetItem name, List<GraphTargetItem> superInterfaces) {
        super(null, null, NOPRECEDENCE);
        this.name = name;
        this.superInterfaces = superInterfaces;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.startClass(name.toStringNoQuotes(localData));
        writer.append("interface ");
        name.toStringNoQuotes(writer, localData);
        boolean first = true;
        if (!superInterfaces.isEmpty()) {
            writer.append(" extends ");
        }
        for (GraphTargetItem ti : superInterfaces) {
            if (!first) {
                writer.append(", ");
            }
            first = false;
            Action.getWithoutGlobal(ti).toStringNoQuotes(writer, localData);
        }
        return writer.startBlock().endBlock().endClass();
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        ret.addAll(asGenerator.generateTraits(localData, true, name, null, superInterfaces, new ArrayList<MyEntry<GraphTargetItem, GraphTargetItem>>(), new ArrayList<>()));
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
