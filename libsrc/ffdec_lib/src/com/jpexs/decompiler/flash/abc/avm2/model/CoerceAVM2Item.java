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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Coerce value to another type.
 *
 * @author JPEXS
 */
public class CoerceAVM2Item extends AVM2Item {

    /**
     * Type
     */
    public GraphTargetItem typeObj;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param value Value
     * @param typeObj Type
     */
    public CoerceAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value, GraphTargetItem typeObj) {
        super(instruction, lineStartIns, value.getPrecedence(), value);
        this.typeObj = typeObj;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(typeObj);
        if (value != null) {
            visitor.visit(value);
        }
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        //Same for ConvertAVM2Item
        boolean displayCoerce = true;
        GraphTargetItem valueReturnType = value.returnType();
        switch (typeObj.toString()) {
            case "*":
                displayCoerce = false;
                break;
            case "Boolean":
                displayCoerce = !valueReturnType.equals(TypeItem.BOOLEAN)
                        && !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "Number":
                displayCoerce = !valueReturnType.equals(TypeItem.INT)
                        && !valueReturnType.equals(TypeItem.NUMBER)
                        && !valueReturnType.equals(TypeItem.UINT)
                        && !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "float":
                displayCoerce = !valueReturnType.equals(TypeItem.INT)
                        && !valueReturnType.equals(new TypeItem("float"))
                        && !valueReturnType.equals(TypeItem.UINT)
                        && !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "int":
                displayCoerce = !valueReturnType.equals(TypeItem.INT)
                        && !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "uint":
                if (valueReturnType.equals(TypeItem.INT) && (value instanceof IntegerValueAVM2Item)) {
                    displayCoerce = (((IntegerValueAVM2Item) value).value < 0);
                } else {
                    displayCoerce = !valueReturnType.equals(TypeItem.UINT)
                            && !valueReturnType.equals(TypeItem.UNBOUNDED);
                }
                break;
            case "String":
                /*displayCoerce = !valueReturnType.equals(TypeItem.STRING)
                        && !valueReturnType.equals(new TypeItem("XML"))
                        && !valueReturnType.equals(new TypeItem("XMLList"))
                        && !valueReturnType.equals(new TypeItem("null"))
                        && !valueReturnType.equals(TypeItem.UNBOUNDED);*/
                displayCoerce = false;
                break;
            default:
                displayCoerce = false;
                break;
            //default:
            // there should be something like instanceof, or not, just comment it out...
            //    displayCoerce = !valueReturnType.equals(typeObj);
        }
        if (displayCoerce) {
            typeObj.toString(writer, localData).append("(");
        }
        value.toString(writer, localData);
        if (displayCoerce) {
            writer.append(")");
        }
        return writer;
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
        switch (typeObj.toString()) {
            case "String":
            case "Boolean":
            case "int":
            case "uint":
            case "Number":
            case "float":
            case "float4":
                return value.isConvertedCompileTime(dependencies);
        }
        return false;
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
        if (typeObj instanceof UnboundedTypeItem) {
            return typeObj;
        }
        if (typeObj instanceof ApplyTypeAVM2Item) {
            return typeObj;
        }
        if (typeObj instanceof TypeItem) {
            return typeObj;
        }
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
            case "float":
                ins = new AVM2Instruction(0, AVM2Instructions.ConvertF, null);
                break;
            case "float4":
                ins = new AVM2Instruction(0, AVM2Instructions.ConvertF4, null);
                break;
            case "decimal":
                if (localData.numberContext != null) {
                    ins = new AVM2Instruction(0, AVM2Instructions.ConvertMP, new int[] {localData.numberContext});
                } else {
                    ins = new AVM2Instruction(0, AVM2Instructions.ConvertM, null);
                }
                break;
            default:
                int type_index = AVM2SourceGenerator.resolveType(localData, typeObj, ((AVM2SourceGenerator) generator).abcIndex);
                ins = new AVM2Instruction(0, AVM2Instructions.Coerce, new int[]{type_index});
                break;
        }
        return toSourceMerge(localData, generator, value, ins);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.typeObj);
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
        final CoerceAVM2Item other = (CoerceAVM2Item) obj;
        if (!Objects.equals(this.typeObj, other.typeObj)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

}
