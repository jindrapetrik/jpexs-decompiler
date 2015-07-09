/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Sxi16Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Sxi1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Sxi8Ins;
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
public class AlchemySignExtendAVM2Item extends AVM2Item {

    private final int size;

    public AlchemySignExtendAVM2Item(GraphSourceItem instruction, GraphTargetItem value, int size) {
        super(instruction, PRECEDENCE_PRIMARY, value);
        this.size = size;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("si").append(size).append("(");
        value.toString(writer, localData);
        return writer.append(")");
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("int");
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        InstructionDefinition def = null;
        switch (size) {
            case 1:
                def = new Sxi1Ins();
                break;
            case 8:
                def = new Sxi8Ins();
                break;
            case 16:
                def = new Sxi16Ins();
                break;
        }
        return toSourceMerge(localData, generator, value, ins(def));
    }
}
