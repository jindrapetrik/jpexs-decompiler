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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.model.BooleanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Name.
 *
 * @author JPEXS
 */
public class NameAVM2Item extends AssignableAVM2Item {

    private boolean attribute;

    private String variableName;

    private boolean definition;

    private int nsKind = -1;

    /**
     * Opened namespaces
     */
    public List<NamespaceItem> openedNamespaces;

    /**
     * Line
     */
    public int line;

    /**
     * Type
     */
    public GraphTargetItem type;

    private GraphTargetItem ns = null;

    private int regNumber = -1;

    /**
     * Unresolved
     */
    public boolean unresolved = false;

    private int slotNumber = -1;

    private int slotScope = 0;

    /**
     * Redirect
     */
    public GraphTargetItem redirect;

    private AbcIndexing abcIndex;

    private String namespaceSuffix;
    
    private final boolean isConst;

    @Override
    public AssignableAVM2Item copy() {
        NameAVM2Item c = new NameAVM2Item(type, line, attribute, variableName, namespaceSuffix, assignedValue, definition, openedNamespaces, abcIndex, isConst);
        c.setNs(ns);
        c.regNumber = regNumber;
        c.unresolved = unresolved;
        c.nsKind = nsKind;
        return c;
    }
    
    /**
     * Checks whether this name is const.
     * @return Whether this name is const
     */
    public boolean isConst() {
        return isConst;
    }

    /**
     * Is attribute.
     * @return Is attribute
     */
    public boolean isAttribute() {
        return attribute;
    }

    /**
     * Sets slot scope.
     * @param slotScope Slot scope
     */
    public void setSlotScope(int slotScope) {
        this.slotScope = slotScope;
    }

    /**
     * Gets slot scope.
     * @return Slot scope
     */
    public int getSlotScope() {
        return slotScope;
    }

    /**
     * Sets namespace.
     * @param ns Namespace
     */
    public void setNs(GraphTargetItem ns) {
        this.ns = ns;
    }

    /**
     * Sets register number.
     * @param regNumber Register number
     */
    public void setRegNumber(int regNumber) {
        this.regNumber = regNumber;
    }

    /**
     * Gets slot number.
     * @return Slot number
     */
    public int getSlotNumber() {
        return slotNumber;
    }

    /**
     * Sets slot number.
     * @param slotNumber Slot number
     */
    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    /**
     * Gets register number.
     * @return Register number
     */
    public int getRegNumber() {
        return regNumber;
    }

    /**
     * Gets namespace.
     * @return Namespace
     */
    public GraphTargetItem getNs() {
        return ns;
    }

    /**
     * Appends name.
     * @param name Name
     */
    public void appendName(String name) {
        this.variableName += "." + name;
    }

    /**
     * Sets definition.
     * @param definition Definition
     */
    public void setDefinition(boolean definition) {
        this.definition = definition;
    }

    /**
     * Sets namespace kind.
     * @param nsKind Namespace kind
     */
    public void setNsKind(int nsKind) {
        this.nsKind = nsKind;
    }

    /**
     * Gets namespace kind.
     * @return Namespace kind
     */
    public int getNsKind() {
        return nsKind;
    }

    /**
     * Gets variable name.
     * @return Variable name
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Get namespace suffix.
     * @return Namespace suffix
     */
    public String getNamespaceSuffix() {
        return namespaceSuffix;
    }

    /**
     * Constructor.
     * @param type Type
     * @param line Line
     * @param attribute Is attribute
     * @param variableName Variable name
     * @param namespaceSuffix Namespace suffix
     * @param storeValue Store value
     * @param definition Is definition
     * @param openedNamespaces Opened namespaces
     * @param abcIndex ABC index
     * @param isConst Is const
     */
    public NameAVM2Item(GraphTargetItem type, int line, boolean attribute, String variableName, String namespaceSuffix, GraphTargetItem storeValue, boolean definition, List<NamespaceItem> openedNamespaces, AbcIndexing abcIndex, boolean isConst) {
        super(storeValue);
        
        if (storeValue != null && type != null) {
            storeValue = AVM2SourceGenerator.handleAndOrCoerce(storeValue, type);
        }
        this.attribute = attribute;
        this.variableName = variableName;
        this.namespaceSuffix = namespaceSuffix;
        this.assignedValue = storeValue;
        this.definition = definition;
        this.line = line;
        this.type = type;
        this.openedNamespaces = openedNamespaces;
        this.abcIndex = abcIndex;
        this.isConst = isConst;
    }

    /**
     * Is definition.
     * @return Is definition
     */
    public boolean isDefinition() {
        return definition;
    }

    /**
     * Gets store value.
     * @return Store value
     */
    public GraphTargetItem getStoreValue() {
        return assignedValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    /**
     * Gets default value for a type.
     * @param type Type
     * @return Default value
     */
    public static GraphTargetItem getDefaultValue(String type) {
        switch (type) {
            case "*":
                return new UndefinedAVM2Item(null, null);
            case "int":
                return new IntegerValueAVM2Item(null, null, 0);
            case "Boolean":
                return new BooleanAVM2Item(null, null, Boolean.FALSE);
            case "Number":
                return new NanAVM2Item(null, null);
            default:
                return new NullAVM2Item(null, null);
        }
    }

    /**
     * Generates coerce.
     * @param localData Local data
     * @param generator Generator
     * @param ttype Target type
     * @return Coerce instruction
     * @throws CompilationException On compilation error
     */
    public static AVM2Instruction generateCoerce(SourceGeneratorLocalData localData, SourceGenerator generator, GraphTargetItem ttype) throws CompilationException {
        if (ttype instanceof UnresolvedAVM2Item) {
            ttype = ((UnresolvedAVM2Item) ttype).resolved;
        }
        AVM2Instruction ins;
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
            case "Number":
                ins = ins(AVM2Instructions.ConvertD);
                break;
            default:
                int type_index = AVM2SourceGenerator.resolveType(localData, ttype, ((AVM2SourceGenerator) generator).abcIndex);
                ins = ins(AVM2Instructions.Coerce, type_index);
                break;
        }
        return ins;
    }

    private List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {
        addTraitUsage(localData, localData.callStack);
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

                return toSourceMerge(localData, generator, assignedValue, generateCoerce(localData, generator, type), needsReturn
                        ? ins(AVM2Instructions.Dup) : null, generateSetLoc(regNumber));
            }
        } else {
            return toSourceMerge(localData, generator, generateGetLoc(regNumber), generateGetSlot(slotScope, slotNumber),
                    needsReturn ? null : ins(AVM2Instructions.Pop));
        }
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        addTraitUsage(localData, localData.callStack);
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

    private void addTraitUsage(SourceGeneratorLocalData localData, List<MethodBody> callStack) {
        ABC abcV = abcIndex.getSelectedAbc();
        AVM2ConstantPool constants = abcV.constants;
        for (MethodBody b : callStack) {
            for (int i = 0; i < b.traits.traits.size(); i++) {
                Trait t = b.traits.traits.get(i);
                if (t.getName(abcV).getName(constants, null, true, true).equals(variableName)) {
                    if (t instanceof TraitSlotConst) {
                        if (!localData.traitUsages.containsKey(b)) {
                            localData.traitUsages.put(b, new ArrayList<>());
                        }
                        localData.traitUsages.get(b).add(i);
                    }
                }
            }
        }
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {
        addTraitUsage(localData, localData.callStack);
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
                AVM2Instruction changeIns;
                if (isInteger) {
                    changeIns = ins(decrement ? AVM2Instructions.DecrementI : AVM2Instructions.IncrementI);
                } else if (localData.numberContext != null) {
                    changeIns = ins(decrement ? AVM2Instructions.DecrementP : AVM2Instructions.IncrementP, localData.numberContext);
                } else {
                    changeIns = ins(decrement ? AVM2Instructions.Decrement : AVM2Instructions.Increment);
                }
                return toSourceMerge(localData, generator,
                        ins(AVM2Instructions.GetScopeObject, slotScope),
                        generateGetSlot(slotScope, slotNumber),
                        changeIns,
                        ins(AVM2Instructions.SetSlot, slotNumber)
                );
            } else {
                AVM2Instruction changeIns;
                if (isInteger) {
                    changeIns = ins(decrement ? AVM2Instructions.DecLocalI : AVM2Instructions.IncLocalI, regNumber);
                } else if (localData.numberContext != null) {
                    changeIns = ins(decrement ? AVM2Instructions.DecLocalP : AVM2Instructions.IncLocalP, localData.numberContext, regNumber);
                } else {
                    changeIns = ins(decrement ? AVM2Instructions.DecLocal : AVM2Instructions.IncLocal, regNumber);
                }
                
                return toSourceMerge(localData, generator,
                        changeIns);
            }
        }
        return toSourceMerge(localData, generator,
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
                slotNumber > -1 ? Arrays.asList(
                                ins(AVM2Instructions.GetScopeObject, slotScope),
                                ins(AVM2Instructions.Swap),
                                ins(AVM2Instructions.SetSlot, slotNumber)
                        ) : null
        );
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.attribute ? 1 : 0);
        hash = 29 * hash + Objects.hashCode(this.variableName);
        hash = 29 * hash + (this.definition ? 1 : 0);
        hash = 29 * hash + this.regNumber;
        hash = 29 * hash + this.slotNumber;
        hash = 29 * hash + this.slotScope;
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
        final NameAVM2Item other = (NameAVM2Item) obj;
        if (this.attribute != other.attribute) {
            return false;
        }
        if (this.definition != other.definition) {
            return false;
        }
        if (this.regNumber != other.regNumber) {
            return false;
        }
        if (this.slotNumber != other.slotNumber) {
            return false;
        }
        if (this.slotScope != other.slotScope) {
            return false;
        }
        return Objects.equals(this.variableName, other.variableName);
    }

}
