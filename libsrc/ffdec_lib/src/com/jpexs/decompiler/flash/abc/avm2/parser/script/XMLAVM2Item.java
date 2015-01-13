/*
 *  Copyright (C) 2014-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class XMLAVM2Item extends AVM2Item {

    public XMLAVM2Item(GraphTargetItem value) {
        super(null, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return value.appendTo(writer, localData);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("XML");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        return toSourceMerge(localData, generator,
                ins(new GetLexIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.QNAME, g.abc.constants.getStringId("XML", true), g.abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, g.abc.constants.getStringId("", true)), 0, true), 0, 0, new ArrayList<Integer>()), true)),
                value,
                ins(new ConstructIns(), 1)
        );
    }

}
