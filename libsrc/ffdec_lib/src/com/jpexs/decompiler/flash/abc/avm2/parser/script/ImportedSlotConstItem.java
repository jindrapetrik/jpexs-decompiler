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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Reference;
import java.util.Arrays;
import java.util.List;

/**
 * Imported slot.
 *
 * @author JPEXS
 */
public class ImportedSlotConstItem extends AssignableAVM2Item {

    /**
     * Type
     */
    public TypeItem type;

    /**
     * Constructor.
     * @param type Type
     */
    public ImportedSlotConstItem(TypeItem type) {
        this.type = type;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        return type.returnType();
    }

    @Override
    public AssignableAVM2Item copy() {
        return new ImportedSlotConstItem(type);
    }

    /**
     * Converts to source.
     * @param localData Local data
     * @param generator Generator
     * @param needsReturn Needs return
     * @return Source
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {
        int propertyId = ((AVM2SourceGenerator) generator).typeName(localData, type);
        Object obj = new FindPropertyAVM2Item(null, null, type);
        Reference<Integer> ret_temp = new Reference<>(-1);
        if (assignedValue != null) {
            GraphTargetItem coerced = assignedValue;
            return toSourceMerge(localData, generator, obj, coerced,
                    needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                    ins(AVM2Instructions.SetProperty, propertyId),
                    needsReturn ? getTemp(localData, generator, ret_temp) : null,
                    killTemp(localData, generator, Arrays.asList(ret_temp)));
        } else {
            if (obj instanceof AVM2Instruction && (((AVM2Instruction) obj).definition instanceof FindPropertyStrictIns)) {
                return toSourceMerge(localData, generator, ins(AVM2Instructions.GetLex, propertyId),
                        needsReturn ? null : ins(AVM2Instructions.Pop)
                );
            }
            return toSourceMerge(localData, generator, obj, ins(AVM2Instructions.GetProperty, propertyId),
                    needsReturn ? null : ins(AVM2Instructions.Pop)
            );
        }
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, true);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, false);
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {

        int propertyId = ((AVM2SourceGenerator) generator).typeName(localData, type);
        Object obj = new FindPropertyAVM2Item(null, null, type);

        Reference<Integer> ret_temp = new Reference<>(-1);
        Reference<Integer> obj_temp = new Reference<>(-1);

        boolean isInteger = false;
        
        AVM2Instruction changeIns;
        if (isInteger) {
            changeIns = ins(decrement ? AVM2Instructions.DecrementI : AVM2Instructions.IncrementI);
        } else if (localData.numberContext != null) {
            changeIns = ins(decrement ? AVM2Instructions.DecrementP : AVM2Instructions.IncrementP, localData.numberContext);
        } else {
            changeIns = ins(decrement ? AVM2Instructions.Decrement : AVM2Instructions.Increment);
        }

        List<GraphSourceItem> ret = toSourceMerge(localData, generator, obj, dupSetTemp(localData, generator, obj_temp),
                ins(AVM2Instructions.GetProperty, propertyId),
                (!isInteger && post) ? ins(AVM2Instructions.ConvertD) : null,
                (!post) ? changeIns : null,
                needsReturn ? ins(AVM2Instructions.Dup) : null,
                (post) ? changeIns : null,
                setTemp(localData, generator, ret_temp),
                getTemp(localData, generator, obj_temp),
                getTemp(localData, generator, ret_temp),
                ins(AVM2Instructions.SetProperty, propertyId),
                killTemp(localData, generator, Arrays.asList(ret_temp, obj_temp)));
        return ret;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public String toString(LocalData localData) throws InterruptedException {
        return super.toString(localData); //To change body of generated methods, choose Tools | Templates.
    }

}
