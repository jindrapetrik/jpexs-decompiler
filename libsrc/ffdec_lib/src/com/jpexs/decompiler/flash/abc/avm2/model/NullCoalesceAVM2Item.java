/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphTargetDialect;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Null coalesce operator (a ?? b).
 * @author JPEXS
 */
public class NullCoalesceAVM2Item extends BinaryOpItem {

    public NullCoalesceAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartItem, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(AVM2GraphTargetDialect.INSTANCE, instruction, lineStartItem, PRECEDENCE_NULLCOALESCE, leftSide, rightSide, "??", null, null);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2SourceGenerator a2generator = (AVM2SourceGenerator) generator;
        return a2generator.generate(localData, this);
    }   
    
    @Override
    public GraphTargetItem returnType() {
        return leftSide.returnType();
    }

    @Override
    public List<GraphSourceItem> getOperatorInstruction() {
        return new ArrayList<>();
    }  
}
