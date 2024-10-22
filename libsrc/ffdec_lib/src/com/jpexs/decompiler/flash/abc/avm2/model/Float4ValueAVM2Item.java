/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Float4 value.
 *
 * @author JPEXS
 */
public class Float4ValueAVM2Item extends NumberValueAVM2Item {

    /**
     * Value
     */
    public Float4 value;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param value Value
     */
    public Float4ValueAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, Float4 value) {
        super(instruction, lineStartIns);
        this.value = value;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {       
        writer.append("float4");
        writer.spaceBeforeCallParenthesis(precedence);
        writer.append("(");
        writer.append(EcmaScript.toString(value.values[0]));
        if (Float.isFinite(value.values[0])) {
            writer.append("f");
        } 
        writer.append(",");
        writer.append(EcmaScript.toString(value.values[1]));
        if (Float.isFinite(value.values[1])) {
            writer.append("f");
        }         
        writer.append(",");
        writer.append(EcmaScript.toString(value.values[2]));
        if (Float.isFinite(value.values[2])) {
            writer.append("f");
        }         
        writer.append(",");
        writer.append(EcmaScript.toString(value.values[3]));
        if (Float.isFinite(value.values[3])) {
            writer.append("f");
        }         
        writer.append(")");
        return writer;
    }

    @Override
    public Object getResult() {
        return value;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator,
                new AVM2Instruction(0, AVM2Instructions.PushFloat4, new int[]{((AVM2SourceGenerator) generator).abcIndex.getSelectedAbc().constants.getFloat4Id(value, true)})
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.NUMBER;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Float4ValueAVM2Item other = (Float4ValueAVM2Item) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

}
