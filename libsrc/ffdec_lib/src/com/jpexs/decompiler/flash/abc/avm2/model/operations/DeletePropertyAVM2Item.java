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
package com.jpexs.decompiler.flash.abc.avm2.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.IndexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.NamespacedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.PropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.UnresolvedAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
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
public class DeletePropertyAVM2Item extends AVM2Item {

    public GraphTargetItem object;

    public GraphTargetItem propertyName;

    private int line;

    //Constructor for compiler
    public DeletePropertyAVM2Item(GraphTargetItem property, int line) {
        this(null, null, property, null);
        this.line = line;
    }

    public DeletePropertyAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, GraphTargetItem propertyName) {
        super(instruction, lineStartIns, PRECEDENCE_UNARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("delete ");
        formatProperty(writer, object, propertyName, localData);
        return writer;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        GraphTargetItem p = object;
        if (p instanceof UnresolvedAVM2Item) {
            p = ((UnresolvedAVM2Item) p).resolved;
        }
        if (p instanceof PropertyAVM2Item) {
            PropertyAVM2Item prop = (PropertyAVM2Item) p;
            return toSourceMerge(localData, generator, prop.resolveObject(localData, generator),
                    ins(AVM2Instructions.DeleteProperty, prop.resolveProperty(localData))
            );
        }
        if (p instanceof IndexAVM2Item) {
            IndexAVM2Item ind = (IndexAVM2Item) p;
            return ind.toSource(localData, generator, true, false, new ArrayList<>(), true, false);
        }
        if (p instanceof NamespacedAVM2Item) {
            NamespacedAVM2Item n = (NamespacedAVM2Item) p;
            return n.toSource(localData, generator, true, false, new ArrayList<>(), true, false);
        }

        throw new CompilationException("Not a property", line); //TODO: handle line better way
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(DottedChain.BOOLEAN);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
