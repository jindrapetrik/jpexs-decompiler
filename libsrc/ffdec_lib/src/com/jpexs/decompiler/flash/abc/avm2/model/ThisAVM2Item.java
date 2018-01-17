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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.ecma.ObjectType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ThisAVM2Item extends AVM2Item {

    public DottedChain className;

    public boolean basicObject;

    public Multiname classMultiname;

    public ThisAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, Multiname classMultiname, DottedChain className, boolean basicObject) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.className = className;
        this.classMultiname = classMultiname;
        this.basicObject = basicObject;
        getSrcData().localName = "this";
    }

    public boolean isBasicObject() {
        return basicObject;
    }

    @Override
    public boolean isConvertedCompileTime(Set<GraphTargetItem> dependencies) {
        return isBasicObject();
    }

    @Override
    public Object getResult() {
        if (basicObject) {
            return new ObjectType(new HashMap<>()) {
                @Override
                public String toString() {
                    return "[object " + className.getLast() + "]";
                }

            };
        }
        return null;
    }

    @Override
    public Boolean getResultAsBoolean() {
        return true;
    }

    @Override
    public String toString() {
        return "this";
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        return writer.append("this");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, new AVM2Instruction(0, AVM2Instructions.GetLocal0, null)
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
