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
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Lf32Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Lf64Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Li16Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Li32Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Li8Ins;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
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
public class AlchemyLoadAVM2Item extends AVM2Item {

    private final char type;

    private final int size;

    private final GraphTargetItem ofs;

    public AlchemyLoadAVM2Item(GraphSourceItem instruction, GraphTargetItem ofs, char type, int size) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.ofs = ofs;
        this.type = type;
        this.size = size;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("l").append(type).append(size).append("(");
        ofs.toString(writer, localData);
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        String ts = "" + type + size;
        InstructionDefinition def = null;
        switch (ts) {
            case "i8":
                def = new Li8Ins();
                break;
            case "i16":
                def = new Li16Ins();
                break;
            case "i32":
                def = new Li32Ins();
                break;
            case "f":
                def = new Lf32Ins();
                break;
            case "f32":
                def = new Lf64Ins();
                break;
        }
        return toSourceMerge(localData, generator, ofs, ins(def));
    }

    @Override
    public GraphTargetItem returnType() {
        switch (type) {
            case 'i':
                return new TypeItem(new DottedChain("int"));
            case 'f':
                return new TypeItem(new DottedChain("Number"));
        }
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
