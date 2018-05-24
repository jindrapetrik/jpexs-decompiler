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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.OrItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class AssignableAVM2Item extends AVM2Item {

    protected GraphTargetItem assignedValue;

    protected GraphTargetItem makeCoerced(GraphTargetItem assignedValue, GraphTargetItem targetType) {
        if (assignedValue instanceof OrItem) {
            OrItem oi = (OrItem) assignedValue;
            return new OrItem(assignedValue.getSrc(), assignedValue.getLineStartItem(), makeCoerced(oi.leftSide, targetType), makeCoerced(oi.rightSide, targetType));
        }
        //TODO: Is it needed for AndItem too?
        return new CoerceAVM2Item(null, null, assignedValue, targetType);
    }

    public AssignableAVM2Item() {
        this(null);
    }

    public abstract AssignableAVM2Item copy();

    public AssignableAVM2Item(GraphTargetItem storeValue) {
        super(null, null, PRECEDENCE_PRIMARY);
        this.assignedValue = storeValue;
    }

    public abstract List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException;

    public GraphTargetItem getAssignedValue() {
        return assignedValue;
    }

    public void setAssignedValue(GraphTargetItem storeValue) {
        this.assignedValue = storeValue;
    }

    public static List<GraphSourceItem> dupSetTemp(SourceGeneratorLocalData localData, SourceGenerator generator, Reference<Integer> register) {
        register.setVal(getFreeRegister(localData, generator));
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(ins(AVM2Instructions.Dup));
        ret.add(generateSetLoc(register.getVal()));
        return ret;
    }

    public static List<GraphSourceItem> setTemp(SourceGeneratorLocalData localData, SourceGenerator generator, Reference<Integer> register) {
        register.setVal(getFreeRegister(localData, generator));
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(generateSetLoc(register.getVal()));
        return ret;
    }

    public static List<GraphSourceItem> getTemp(SourceGeneratorLocalData localData, SourceGenerator generator, Reference<Integer> register) {
        if (register.getVal() < 0) {
            return new ArrayList<>();
        }
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(generateGetLoc(register.getVal()));
        return ret;
    }

    /*protected List<GraphSourceItem> getAndKillTemp(SourceGeneratorLocalData localData, SourceGenerator generator, Reference<Integer> register) {
     killRegister(localData, generator, register.getVal());
     List<GraphSourceItem> ret = new ArrayList<>();
     ret.add(generateGetLoc(register.getVal()));
     ret.add(ins(AVM2Instructions.Kill, register.getVal()));
     return ret;
     }*/
    public static List<GraphSourceItem> killTemp(SourceGeneratorLocalData localData, SourceGenerator generator, List<Reference<Integer>> registers) {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (Reference<Integer> register : registers) {
            if (register.getVal() < 0) {
                continue;
            }
            killRegister(localData, generator, register.getVal());

            ret.add(ins(AVM2Instructions.Kill, register.getVal()));
        }
        return ret;
    }

    public static AVM2Instruction generateSetLoc(int regNumber) {
        switch (regNumber) {
            case -1:
                return null;
            case 0:
                return ins(AVM2Instructions.SetLocal0);
            case 1:
                return ins(AVM2Instructions.SetLocal1);
            case 2:
                return ins(AVM2Instructions.SetLocal2);
            case 3:
                return ins(AVM2Instructions.SetLocal3);
            default:
                return ins(AVM2Instructions.SetLocal, regNumber);
        }
    }

    public static AVM2Instruction generateGetLoc(int regNumber) {
        switch (regNumber) {
            case -1:
                return null;
            case 0:
                return ins(AVM2Instructions.GetLocal0);
            case 1:
                return ins(AVM2Instructions.GetLocal1);
            case 2:
                return ins(AVM2Instructions.GetLocal2);
            case 3:
                return ins(AVM2Instructions.GetLocal3);
            default:
                return ins(AVM2Instructions.GetLocal, regNumber);
        }
    }

    public static List<GraphSourceItem> generateGetSlot(int slotScope, int slotNumber) {
        if (slotNumber == -1) {
            return null;
        }
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(ins(AVM2Instructions.GetScopeObject, slotScope));
        ret.add(ins(AVM2Instructions.GetSlot, slotNumber));
        return ret;
    }

    public static List<GraphSourceItem> generateSetSlot(SourceGeneratorLocalData localData, SourceGenerator generator, GraphTargetItem val, int slotScope, int slotNumber) throws CompilationException {
        if (slotNumber == -1) {
            return null;
        }
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(ins(AVM2Instructions.GetScopeObject, slotScope));
        ret.addAll(val.toSource(localData, generator));
        ret.add(ins(AVM2Instructions.SetSlot, slotNumber));
        return ret;
    }
}
