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
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class NameAVM2Item extends AssignableAVM2Item {

    private String variableName;

    private boolean definition;

    private int nsKind = -1;

    public List<NamespaceItem> openedNamespaces;

    public int line;

    public GraphTargetItem type;

    private GraphTargetItem ns = null;

    private int regNumber = -1;

    public boolean unresolved = false;

    private int slotNumber = -1;

    private int slotScope = 0;

    public GraphTargetItem redirect;

    @Override
    public AssignableAVM2Item copy() {
        NameAVM2Item c = new NameAVM2Item(type, line, variableName, assignedValue, definition, openedNamespaces);
        c.setNs(ns);
        c.regNumber = regNumber;
        c.unresolved = unresolved;
        c.nsKind = nsKind;
        return c;
    }

    public void setSlotScope(int slotScope) {
        this.slotScope = slotScope;
    }

    public int getSlotScope() {
        return slotScope;
    }

    public void setNs(GraphTargetItem ns) {
        this.ns = ns;
    }

    public void setRegNumber(int regNumber) {
        this.regNumber = regNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public int getRegNumber() {
        return regNumber;
    }

    public GraphTargetItem getNs() {
        return ns;
    }

    public void appendName(String name) {
        this.variableName += "." + name;
    }

    public void setDefinition(boolean definition) {
        this.definition = definition;
    }

    public void setNsKind(int nsKind) {
        this.nsKind = nsKind;
    }

    public int getNsKind() {
        return nsKind;
    }

    public String getVariableName() {
        return variableName;
    }

    public NameAVM2Item(GraphTargetItem type, int line, String variableName, GraphTargetItem storeValue, boolean definition, List<NamespaceItem> openedNamespaces) {
        super(storeValue);
        this.variableName = variableName;
        this.assignedValue = storeValue;
        this.definition = definition;
        this.line = line;
        this.type = type;
        this.openedNamespaces = openedNamespaces;
    }

    public boolean isDefinition() {
        return definition;
    }

    public GraphTargetItem getStoreValue() {
        return assignedValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    public static GraphTargetItem getDefaultValue(String type) {
        switch (type) {
            case "*":
                return new UndefinedAVM2Item(null, null);
            case "int":
                return new IntegerValueAVM2Item(null, null, 0L);
            case "Number":
                return new NanAVM2Item(null, null);
            default:
                return new NullAVM2Item(null, null);
        }
    }

    public static AVM2Instruction generateCoerce(SourceGeneratorLocalData localData, SourceGenerator generator, GraphTargetItem ttype) throws CompilationException {
        if (ttype instanceof UnresolvedAVM2Item) {
            ttype = ((UnresolvedAVM2Item) ttype).resolved;
        }
        AVM2Instruction ins;
        if (ttype instanceof UnboundedTypeItem) {
            ins = ins(AVM2Instructions.CoerceA);
        } else {
            switch (ttype.toString()) {
                case "int":
                    ins = ins(AVM2Instructions.ConvertI);
                    break;
                case "*":
                    ins = ins(AVM2Instructions.CoerceA);
                    break;
                case "String":
                    ins = ins(AVM2Instructions.CoerceS);
                    break;
                case "Boolean":
                    ins = ins(AVM2Instructions.ConvertB);
                    break;
                case "uint":
                    ins = ins(AVM2Instructions.ConvertU);
                    break;
                default:
                    int type_index = AVM2SourceGenerator.resolveType(localData, ttype, ((AVM2SourceGenerator) generator).abcIndex);
                    ins = ins(AVM2Instructions.Coerce, type_index);
                    break;
            }
        }
        return ins;
    }

    private List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {
        if (variableName != null && regNumber == -1 && slotNumber == -1 && ns == null) {
            throw new CompilationException("No register or slot set for " + variableName, line);
        }
        if (definition && assignedValue == null) {
            return new ArrayList<>();
        }
        String name = variableName;
        boolean attr = false;
        if (name != null && name.startsWith("@")) {
            //name = name.substring(1);
            attr = true;
        }
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        Reference<Integer> ns_temp = new Reference<>(-1);
        Reference<Integer> index_temp = new Reference<>(-1);
        Reference<Integer> ret_temp = new Reference<>(-1);

        if (assignedValue != null) {
            List<String> basicTypes = Arrays.asList("int", "Number");
            if (slotNumber > -1) {
                return toSourceMerge(localData, generator,
                        ins(AVM2Instructions.GetScopeObject, slotScope),
                        assignedValue, !(("" + assignedValue.returnType()).equals("" + type) && (basicTypes.contains("" + type))) ? generateCoerce(localData, generator, type) : null, needsReturn
                        ? dupSetTemp(localData, generator, ret_temp) : null, generateSetLoc(regNumber), slotNumber > -1
                        ? ins(AVM2Instructions.SetSlot, slotNumber)
                        : null,
                        needsReturn ? getTemp(localData, generator, ret_temp) : null,
                        killTemp(localData, generator, Arrays.asList(ret_temp)));
            } else {

                return toSourceMerge(localData, generator, assignedValue, !(("" + assignedValue.returnType()).equals("" + type) && (basicTypes.contains("" + type))) ? generateCoerce(localData, generator, type) : null, needsReturn
                        ? ins(AVM2Instructions.Dup) : null, generateSetLoc(regNumber));
            }
        } else {
            return toSourceMerge(localData, generator, generateGetLoc(regNumber), generateGetSlot(slotScope, slotNumber),
                    needsReturn ? null : ins(AVM2Instructions.Pop));
        }
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (redirect != null) {
            return redirect.toSource(localData, generator);
        }
        return toSource(localData, generator, true);

    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (redirect != null) {
            return redirect.toSourceIgnoreReturnValue(localData, generator);
        }
        return toSource(localData, generator, false);
    }

    @Override
    public boolean hasReturnValue() {
        return !definition;
    }

    @Override
    public boolean needsSemicolon() {
        return definition;
    }

    @Override
    public String toString() {
        return variableName;
    }

    @Override
    public GraphTargetItem returnType() {
        if (type == null) {
            return TypeItem.UNBOUNDED;
        }
        return type;
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {
        if (redirect != null) {
            return ((AssignableAVM2Item) redirect).toSourceChange(localData, generator, post, decrement, needsReturn);
        }
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        Reference<Integer> ns_temp = new Reference<>(-1);
        Reference<Integer> name_temp = new Reference<>(-1);
        Reference<Integer> index_temp = new Reference<>(-1);
        Reference<Integer> ret_temp = new Reference<>(-1);
        boolean isInteger = returnType().toString().equals("int");
        /*


         */
        if (!needsReturn) {
            if (slotNumber > -1) {
                return toSourceMerge(localData, generator,
                        ins(AVM2Instructions.GetScopeObject, slotScope),
                        generateGetSlot(slotScope, slotNumber),
                        (decrement ? ins(isInteger ? AVM2Instructions.DecrementI : AVM2Instructions.Decrement) : ins(isInteger ? AVM2Instructions.IncrementI : AVM2Instructions.Increment)),
                        ins(AVM2Instructions.SetSlot, slotNumber)
                );
            } else {
                return toSourceMerge(localData, generator,
                        (decrement ? ins(isInteger ? AVM2Instructions.DecLocalI : AVM2Instructions.DecLocal, regNumber) : ins(isInteger ? AVM2Instructions.IncLocalI : AVM2Instructions.IncLocal, regNumber)));
            }
        }
        return toSourceMerge(localData, generator,
                slotNumber > -1 ? ins(AVM2Instructions.GetScopeObject, slotScope) : null,
                //Start get original
                generateGetLoc(regNumber), generateGetSlot(slotScope, slotNumber),
                //End get original
                //!isInteger ? ins(AVM2Instructions.ConvertD) : null,
                //End get original
                (!post) ? (decrement ? ins(isInteger ? AVM2Instructions.DecrementI : AVM2Instructions.Decrement) : ins(isInteger ? AVM2Instructions.IncrementI : AVM2Instructions.Increment)) : null,
                needsReturn ? ins(AVM2Instructions.Dup) : null,
                (post) ? (decrement ? ins(isInteger ? AVM2Instructions.DecrementI : AVM2Instructions.Decrement) : ins(isInteger ? AVM2Instructions.IncrementI : AVM2Instructions.Increment)) : null,
                generateCoerce(localData, generator, returnType()),
                generateSetLoc(regNumber),
                slotNumber > -1 ? ins(AVM2Instructions.SetSlot, slotNumber) : null
        );
    }
}
