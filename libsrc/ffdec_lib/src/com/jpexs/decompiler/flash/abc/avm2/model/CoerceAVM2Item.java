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
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class CoerceAVM2Item extends AVM2Item {

    public GraphTargetItem typeObj;

    public CoerceAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value, GraphTargetItem typeObj) {
        super(instruction, lineStartIns, value.getPrecedence(), value);
        this.typeObj = typeObj;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        //return hilight("(" + type + ")", highlight)+
        return value.toString(writer, localData);
    }

    @Override
    public GraphTargetItem getNotCoerced() {
        return value.getNotCoerced();
    }

    @Override
    public GraphTargetItem getNotCoercedNoDup() {
        return value.getNotCoercedNoDup();
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        dependencies.add(value);
        return value.isConvertedCompileTime(dependencies);
    }

    @Override
    public Object getResult() {
        Object ret = value.getResult();
        switch (typeObj.toString()) {
            case "String":
                if (ret == Null.INSTANCE) {
                    return ret;
                }
                if (ret == Undefined.INSTANCE) {
                    return Null.INSTANCE;
                }
                return value.getResultAsString();
            case "*":
                break;
        }
        return ret;

    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(typeObj.toString());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {

        /*if (value.returnType().toString().equals(type)) {
         return toSourceMerge(localData, generator, value);
         }*/
        AVM2Instruction ins;
        switch (typeObj.toString()) {
            case "*":
                ins = new AVM2Instruction(0, AVM2Instructions.CoerceA, null);
                break;
            case "String":
                ins = new AVM2Instruction(0, AVM2Instructions.CoerceS, null);
                break;
            case "Boolean":
                ins = new AVM2Instruction(0, AVM2Instructions.ConvertB, null);
                break;
            case "int":
                ins = new AVM2Instruction(0, AVM2Instructions.ConvertI, null);
                break;
            case "uint":
                ins = new AVM2Instruction(0, AVM2Instructions.ConvertU, null);
                break;
            case "Number":
                ins = new AVM2Instruction(0, AVM2Instructions.ConvertD, null);
                break;
            default:
                int type_index = AVM2SourceGenerator.resolveType(localData, typeObj, ((AVM2SourceGenerator) generator).abcIndex);
                ins = new AVM2Instruction(0, AVM2Instructions.Coerce, new int[]{type_index});
                break;
        }
        return toSourceMerge(localData, generator, value, ins);
    }
}
