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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.EcmaType;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.UnaryOpItem;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class TypeOfAVM2Item extends UnaryOpItem {

    public TypeOfAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_UNARY, value, "typeof ", "");
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        dependencies.add(value);
        return value.isCompileTime(dependencies);
    }

    @Override
    public Object getResult() {
        Object res = value.getResult();
        EcmaType type = EcmaScript.type(res);
        switch (type) {
            case UNDEFINED:
                return "undefined";
            case NULL:
                return "object";
            case BOOLEAN:
                return "Boolean";
            case NUMBER:
                return "number";
            case STRING:
                return "string";
            case OBJECT:
                return "object";

        }
        //TODO: function,xml
        return "object";
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, value,
                new AVM2Instruction(0, AVM2Instructions.TypeOf, null)
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new UnboundedTypeItem();
    }
}
