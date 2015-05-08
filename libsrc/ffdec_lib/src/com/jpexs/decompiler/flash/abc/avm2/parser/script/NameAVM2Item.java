/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetScopeObjectIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetSlotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceAIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceSIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertBIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertUIns;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
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

    public List<Integer> openedNamespaces;

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

    public NameAVM2Item(GraphTargetItem type, int line, String variableName, GraphTargetItem storeValue, boolean definition, List<Integer> openedNamespaces) {
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

    private int allNsSet(ABC abc) {
        int[] nssa = new int[openedNamespaces.size()];
        for (int i = 0; i < openedNamespaces.size(); i++) {
            nssa[i] = openedNamespaces.get(i);
        }
        return abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);
    }

    public static GraphTargetItem getDefaultValue(String type) {
        switch (type) {
            case "*":
                return new UndefinedAVM2Item(null);
            case "int":
                return new IntegerValueAVM2Item(null, 0L);
            case "Number":
                return new NanAVM2Item(null);
            default:
                return new NullAVM2Item(null);
        }
    }

    public static AVM2Instruction generateCoerce(SourceGeneratorLocalData localData, SourceGenerator generator, GraphTargetItem ttype) throws CompilationException {
        if (ttype instanceof UnresolvedAVM2Item) {
            ttype = ((UnresolvedAVM2Item) ttype).resolved;
        }
        AVM2Instruction ins;
        if (ttype instanceof UnboundedTypeItem) {
            ins = ins(new CoerceAIns());
        } else {
            switch (ttype.toString()) {
                case "int":
                    ins = ins(new ConvertIIns());
                    break;
                case "*":
                    ins = ins(new CoerceAIns());
                    break;
                case "String":
                    ins = ins(new CoerceSIns());
                    break;
                case "Boolean":
                    ins = ins(new ConvertBIns());
                    break;
                case "uint":
                    ins = ins(new ConvertUIns());
                    break;
                default:
                    int type_index = AVM2SourceGenerator.resolveType(localData, ttype, ((AVM2SourceGenerator) generator).abc, ((AVM2SourceGenerator) generator).allABCs);
                    ins = ins(new CoerceIns(), type_index);
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
            name = name.substring(1);
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
                        ins(new GetScopeObjectIns(), slotScope),
                        assignedValue, !(("" + assignedValue.returnType()).equals("" + type) && (basicTypes.contains("" + type))) ? generateCoerce(localData, generator, type) : null, needsReturn
                                ? dupSetTemp(localData, generator, ret_temp) : null, generateSetLoc(regNumber), slotNumber > -1
                                ? ins(new SetSlotIns(), slotNumber)
                                : null,
                        needsReturn ? getTemp(localData, generator, ret_temp) : null,
                        killTemp(localData, generator, Arrays.asList(ret_temp)));
            } else {

                return toSourceMerge(localData, generator, assignedValue, !(("" + assignedValue.returnType()).equals("" + type) && (basicTypes.contains("" + type))) ? generateCoerce(localData, generator, type) : null, needsReturn
                        ? ins(new DupIns()) : null, generateSetLoc(regNumber));
            }
        } else {
            return toSourceMerge(localData, generator, generateGetLoc(regNumber), generateGetSlot(slotScope, slotNumber),
                    needsReturn ? null : ins(new PopIns()));
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
        if (definition) {
            return false;
        }
        return true;
    }

    @Override
    public boolean needsSemicolon() {
        if (definition) {
            return true;
        }
        return false;
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
                        ins(new GetScopeObjectIns(), slotScope),
                        generateGetSlot(slotScope, slotNumber),
                        (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())),
                        ins(new SetSlotIns(), slotNumber)
                );
            } else {
                return toSourceMerge(localData, generator,
                        (decrement ? ins(isInteger ? new DecLocalIIns() : new DecLocalIns(), regNumber) : ins(isInteger ? new IncLocalIIns() : new IncLocalIns(), regNumber)));
            }
        }
        return toSourceMerge(localData, generator,
                slotNumber > -1 ? ins(new GetScopeObjectIns(), slotScope) : null,
                //Start get original
                generateGetLoc(regNumber), generateGetSlot(slotScope, slotNumber),
                //End get original
                //!isInteger ? ins(new ConvertDIns()) : null,
                //End get original
                (!post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                needsReturn ? ins(new DupIns()) : null,
                (post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                generateCoerce(localData, generator, returnType()),
                generateSetLoc(regNumber),
                slotNumber > -1 ? ins(new SetSlotIns(), slotNumber) : null
        );
    }
}
