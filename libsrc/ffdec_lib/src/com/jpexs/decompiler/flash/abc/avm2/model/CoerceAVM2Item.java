/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
                        ; //&& !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "Number":
                displayCoerce = !valueReturnType.equals(TypeItem.INT)
                        && !valueReturnType.equals(TypeItem.NUMBER)
                        && !valueReturnType.equals(TypeItem.UINT)
                        ; //&& !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "float":
                displayCoerce = !valueReturnType.equals(TypeItem.INT)
                        && !valueReturnType.equals(new TypeItem("float"))
                        && !valueReturnType.equals(TypeItem.UINT)
                        ; //&& !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "int":
                displayCoerce = !valueReturnType.equals(TypeItem.INT)
                        ; //&& !valueReturnType.equals(TypeItem.UNBOUNDED);
                break;
            case "uint":
                if (valueReturnType.equals(TypeItem.INT) && (value instanceof IntegerValueAVM2Item)) {
                    displayCoerce = (((IntegerValueAVM2Item) value).value < 0);
                } else {
                    displayCoerce = !valueReturnType.equals(TypeItem.UINT)
                           ; // && !valueReturnType.equals(TypeItem.UNBOUNDED);
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
                //Haxe casts with a coerce that AS3 does not do implicitly
                displayCoerce = isDowncast(localData.abcIndex, value, valueReturnType, typeObj);
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

    /**
     * Checks whether coercing a value to a type is a downcast, which AS3 does
     * not do implicitly.
     *
     * @param abcIndex ABC indexing
     * @param value Value
     * @param valueType Value type
     * @param type Type coerced to
     * @return True when the value type is known and is not the type nor one of
     * its subtypes
     */
    private static boolean isDowncast(AbcIndexing abcIndex, GraphTargetItem value, GraphTargetItem valueType, GraphTargetItem type) {
        if (abcIndex == null || !(valueType instanceof TypeItem) || !(type instanceof TypeItem)) {
            return false;
        }
        GraphTargetItem notCoerced = value.getNotCoerced();
        if (notCoerced instanceof OrItem || notCoerced instanceof AndItem || notCoerced instanceof TernarOpItem) {
            //AS3 gives them any type
            return false;
        }
        DottedChain typeName = ((TypeItem) type).fullTypeName;
        DottedChain valueTypeName = ((TypeItem) valueType).fullTypeName;
        if (typeName.equals(valueTypeName) || typeName.toRawString().equals("Object")) {
            return false;
        }
        if (valueTypeName.toRawString().equals("Class")) {
            //new on a Class value, whose return type is its Class, gives any type
            return false;
        }
        if (valueTypeName.toRawString().equals("Object")) {
            return true;
        }
        AbcIndexing.ClassIndex ci = abcIndex.findClass(valueType, null, null);
        if (ci == null) {
            return false;
        }
        Boolean assignable = isAssignableTo(abcIndex, ci, typeName, new HashSet<>());
        return assignable != null && !assignable;
    }

    /**
     * Checks whether a class is a type or one of its subtypes.
     *
     * @param abcIndex ABC indexing
     * @param ci Class
     * @param typeName Type name
     * @param visited Visited classes
     * @return True or false, null when part of its hierarchy is unknown
     */
    private static Boolean isAssignableTo(AbcIndexing abcIndex, AbcIndexing.ClassIndex ci, DottedChain typeName, Set<AbcIndexing.ClassIndex> visited) {
        if (!visited.add(ci)) {
            return false;
        }
        InstanceInfo ii = ci.abc.instance_info.get(ci.index);
        if (typeName.equals(ii.getName(ci.abc.constants).getNameWithNamespace(new LinkedHashSet<>(), ci.abc, ci.abc.constants, false))) {
            return true;
        }
        List<Integer> supers = new ArrayList<>();
        if (ii.super_index > 0) {
            supers.add(ii.super_index);
        }
        for (int i : ii.interfaces) {
            supers.add(i);
        }
        boolean unknown = false;
        for (int s : supers) {
            DottedChain superName = ci.abc.constants.getMultiname(s).getNameWithNamespace(new LinkedHashSet<>(), ci.abc, ci.abc.constants, false);
            if (typeName.equals(superName)) {
                return true;
            }
            AbcIndexing.ClassIndex sci = abcIndex.findClass(new TypeItem(superName), ci.abc, null);
            if (sci == null) {
                unknown = true;
                continue;
            }
            Boolean r = isAssignableTo(abcIndex, sci, typeName, visited);
            if (r == null) {
                unknown = true;
            } else if (r) {
                return true;
            }
        }
        return unknown ? null : false;
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
