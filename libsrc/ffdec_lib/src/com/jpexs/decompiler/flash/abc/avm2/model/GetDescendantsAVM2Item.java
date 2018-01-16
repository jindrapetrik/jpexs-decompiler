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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.NamespaceItem;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GetDescendantsAVM2Item extends AVM2Item {

    public GraphTargetItem object;

    public GraphTargetItem multiname;

    public List<NamespaceItem> openedNamespaces;

    public String nameStr;

    //constructor for compiler
    public GetDescendantsAVM2Item(GraphTargetItem object, String nameStr, List<NamespaceItem> openedNamespaces) {
        super(null, null, PRECEDENCE_PRIMARY);
        this.object = object;
        this.nameStr = nameStr;
        this.openedNamespaces = openedNamespaces;
    }

    public GetDescendantsAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, GraphTargetItem multiname) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.object = object;
        this.multiname = multiname;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        object.toString(writer, localData);
        writer.append("..");
        return multiname.toString(writer, localData);
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return ((AVM2SourceGenerator) generator).generate(localData, this);
    }
}
