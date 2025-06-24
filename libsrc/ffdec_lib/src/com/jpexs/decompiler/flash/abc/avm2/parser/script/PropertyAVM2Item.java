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

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Reference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Property.
 *
 * @author JPEXS
 */
public class PropertyAVM2Item extends AssignableAVM2Item {

    /**
     * Attribute
     */
    public boolean attribute;

    /**
     * Property name
     */
    public String propertyName;

    /**
     * Object
     */
    public GraphTargetItem object;

    /**
     * ABC indexing
     */
    public AbcIndexing abcIndex;

    /**
     * Namespace suffix
     */
    public String namespaceSuffix;

    /**
     * Opened namespaces
     */
    private final List<NamespaceItem> openedNamespaces;

    /**
     * Call stack
     */
    private final List<MethodBody> callStack;

    /**
     * Scope stack
     */
    public List<GraphTargetItem> scopeStack = new ArrayList<>();
    private final boolean nullish;

    @Override
    public AssignableAVM2Item copy() {
        PropertyAVM2Item p = new PropertyAVM2Item(object, attribute, propertyName, namespaceSuffix, abcIndex, openedNamespaces, callStack, nullish);
        return p;
    }

    /**
     * Constructor.
     *
     * @param object Object
     * @param attribute Attribute
     * @param propertyName Property name
     * @param namespaceSuffix Namespace suffix
     * @param abcIndex ABC indexing
     * @param openedNamespaces Opened namespaces
     * @param callStack Call stack
     * @param nullish Nullish
     */
    public PropertyAVM2Item(GraphTargetItem object, boolean attribute, String propertyName, String namespaceSuffix, AbcIndexing abcIndex, List<NamespaceItem> openedNamespaces, List<MethodBody> callStack, boolean nullish) {
        this.attribute = attribute;
        this.propertyName = propertyName;
        this.namespaceSuffix = namespaceSuffix;
        this.object = object;
        this.abcIndex = abcIndex;
        this.openedNamespaces = openedNamespaces;
        this.callStack = callStack;
        this.nullish = nullish;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    private int allNsSet(AbcIndexing abc) throws CompilationException {
        int[] nssa = new int[openedNamespaces.size()];
        for (int i = 0; i < nssa.length; i++) {
            nssa[i] = openedNamespaces.get(i).getCpoolIndex(abc);
        }

        return abc.getSelectedAbc().constants.getNamespaceSetId(nssa, true);
    }

    /**
     * Resolves property.
     * @param mustExist Must exist
     * @param localData Local data
     * @param isType Is type
     * @param objectType Object type
     * @param propertyType Property type
     * @param propertyIndex Property index
     * @param propertyValue Property value
     * @param propertyValueABC Property value ABC
     * @throws CompilationException On compilation error
     */
    public void resolve(boolean mustExist, SourceGeneratorLocalData localData, Reference<Boolean> isType, Reference<GraphTargetItem> objectType, Reference<GraphTargetItem> propertyType, Reference<Integer> propertyIndex, Reference<ValueKind> propertyValue, Reference<ABC> propertyValueABC, Reference<Trait> propertyTrait) throws CompilationException {
        Integer namespaceSuffixInt = null;
        if (!"".equals(namespaceSuffix)) {
            namespaceSuffixInt = Integer.parseInt(namespaceSuffix.substring(1));
        }
        isType.setVal(false);
        GraphTargetItem thisType = new TypeItem(localData.getFullClass());
        GraphTargetItem objType = null;
        GraphTargetItem objSubType = null;
        ValueKind propValue = null;
        ABC propValueAbc = null;
        if (object != null) {
            GraphTargetItem oretType = object.returnType();
            if (oretType instanceof UnresolvedAVM2Item) {
                UnresolvedAVM2Item ur = (UnresolvedAVM2Item) oretType;
                oretType = ur.resolved;
            }
            if (oretType instanceof ApplyTypeAVM2Item) {
                ApplyTypeAVM2Item t = (ApplyTypeAVM2Item) oretType;
                objType = t;
            } else if (oretType instanceof TypeItem) {
                TypeItem t = (TypeItem) oretType;
                objType = t;
            } else {
                objType = new TypeItem(oretType.toString());
            }
        }
        GraphTargetItem propType = null;
        Trait propTrait = null;
        int propIndex = 0;
        ABC abc = abcIndex.getSelectedAbc();
        AVM2ConstantPool constants = abc.constants;
        if (!attribute) {

            if (scopeStack.isEmpty()) { //Everything is multiname when with command                  
                GraphTargetItem ttype = objType;
                if (ttype == null) {
                    ttype = thisType;
                }

                if (ttype.equals(new TypeItem(InitVectorAVM2Item.VECTOR_FQN))) {
                    switch ("" + objSubType) {
                        case "int":
                            ttype = new TypeItem(InitVectorAVM2Item.VECTOR_INT);
                            break;
                        case "Number":
                            ttype = new TypeItem(InitVectorAVM2Item.VECTOR_DOUBLE);
                            break;
                        case "uint":
                            ttype = new TypeItem(InitVectorAVM2Item.VECTOR_UINT);
                            break;
                        default:
                            ttype = new TypeItem(InitVectorAVM2Item.VECTOR_OBJECT);
                    }
                }                                                
                
                boolean foundInCallStack = false;
                
                if (objType == null) {
                    for (MethodBody b : callStack) {
                        for (int i = 0; i < b.traits.traits.size(); i++) {
                            Trait t = b.traits.traits.get(i);
                            if (t.getName(abc).getName(constants, null, true, true).equals(propertyName)) {
                                if (t instanceof TraitSlotConst) {
                                    TraitSlotConst tsc = (TraitSlotConst) t;
                                    objType = new TypeItem(DottedChain.FUNCTION);
                                    propType = AbcIndexing.multinameToType(tsc.type_index, constants);
                                    propIndex = tsc.name_index;
                                    if (!localData.traitUsages.containsKey(b)) {
                                        localData.traitUsages.put(b, new ArrayList<>());
                                    }
                                    localData.traitUsages.get(b).add(i);
                                    foundInCallStack = true;                                    
                                }
                            }
                        }
                    }
                }
                
                if (!foundInCallStack) {
                    if (ttype instanceof TypeItem) {
                        DottedChain ftn = ((TypeItem) ttype).fullTypeName;
                        Reference<String> outName = new Reference<>("");
                        Reference<DottedChain> outNs = new Reference<>(DottedChain.EMPTY);
                        Reference<DottedChain> outPropNs = new Reference<>(DottedChain.EMPTY);
                        Reference<Integer> outPropNsKind = new Reference<>(1);
                        Reference<Integer> outPropNsIndex = new Reference<>(0);
                        Reference<GraphTargetItem> outPropType = new Reference<>(null);
                        Reference<ValueKind> outPropValue = new Reference<>(null);
                        Reference<ABC> outPropValueAbc = new Reference<>(null);
                        Reference<Trait> outPropTrait = new Reference<>(null);
                        List<Integer> otherNs = new ArrayList<>();
                        for (NamespaceItem n : openedNamespaces) {
                            if (n.isResolved()) {
                                otherNs.add(n.getCpoolIndex(abcIndex));
                            }
                        }
                        if ((object instanceof NameAVM2Item) && "super".equals(((NameAVM2Item) object).getVariableName())) {
                            // super is special cause its static type is the super class, but it still allows access to protected members
                            // so for super to work we need to also allow the protected namespace of the super class
                            // however this namespace is in the ABC of the super class and not in abcIndex.getSelectedAbc()
                            AbcIndexing.ClassIndex ci = abcIndex.findClass(objType, null, null/*FIXME?*/);
                            int superProtectedNs = ci.abc.instance_info.get(ci.index).protectedNS;
                            Reference<Boolean> foundStatic = new Reference<>(null);                
                            AbcIndexing.TraitIndex sp = abcIndex.findProperty(new AbcIndexing.PropertyDef(propertyName, objType, ci.abc, superProtectedNs), false, true, true, foundStatic);
                            if (sp != null) {
                                objType = sp.objType;
                                Namespace ns = sp.trait.getName(sp.abc).getNamespace(sp.abc.constants);
                                propIndex = constants.getMultinameId(Multiname.createQName(false,
                                        constants.getStringId(propertyName, true),
                                        constants.getNamespaceId(ns.kind, ns.getName(sp.abc.constants), sp.abc == abc ? abc.constants.getNamespaceSubIndex(sp.trait.getName(sp.abc).namespace_index) : 0, true)), true
                                );
                                propType = sp.returnType;
                                propValue = sp.value;
                                propValueAbc = sp.abc;
                                propTrait = sp.trait;
                            }
                        }
                        if (propType == null && AVM2SourceGenerator.searchPrototypeChain(namespaceSuffixInt, otherNs, localData.privateNs, localData.protectedNs, false, abcIndex, ftn.getWithoutLast(), ftn.getLast(), propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc, isType, outPropTrait)) {
                            objType = new TypeItem(outNs.getVal().addWithSuffix(outName.getVal()));
                            propType = outPropType.getVal();
                            propIndex = constants.getMultinameId(Multiname.createQName(false,
                                    constants.getStringId(propertyName, true),
                                    namespaceSuffixInt != null ? namespaceSuffixInt : constants.getNamespaceId(outPropNsKind.getVal(), outPropNs.getVal(), outPropNsIndex.getVal(), true)), true
                            );
                            propValue = outPropValue.getVal();
                            propValueAbc = outPropValueAbc.getVal();
                            propTrait = outPropTrait.getVal();
                        }
                    }
                }

                if (objType == null) {
                    loopobjType:
                    for (int i = 0; i < openedNamespaces.size(); i++) {
                        if (!openedNamespaces.get(i).isResolved()) {
                            continue;
                        }
                        int nsindex = openedNamespaces.get(i).getCpoolIndex(abcIndex);

                        int nsKind = openedNamespaces.get(i).kind;
                        DottedChain nsname = openedNamespaces.get(i).name;

                        if (nsname.isTopLevel()) {
                            continue;
                        }

                        int name_index = 0;
                        int string_property_index = constants.getStringId(propertyName, false);
                        if (string_property_index > -1) {
                            for (int m = 1; m < constants.getMultinameCount(); m++) {
                                Multiname mname = constants.getMultiname(m);
                                if (mname.kind == Multiname.QNAME
                                        && mname.name_index == string_property_index
                                        && mname.namespace_index == nsindex) {
                                    name_index = m;
                                    break;
                                }
                            }
                        }
                        if (name_index > 0) {
                            //I believe these can be commented out... as it breaks #1840
                            /*for (int c = 0; c < abc.instance_info.size(); c++) {
                                    if (abc.instance_info.get(c).deleted) {
                                        continue;
                                    }
                                    for (Trait t : abc.instance_info.get(c).instance_traits.traits) {
                                        if (t.name_index == name_index) {
                                            objType = multinameToType(abc.instance_info.get(c).name_index, constants);
                                            propType = AVM2SourceGenerator.getTraitReturnType(abcIndex, t);
                                            propIndex = t.name_index;
                                            if (t instanceof TraitSlotConst) {
                                                TraitSlotConst tsc = (TraitSlotConst) t;
                                                propValue = new ValueKind(tsc.value_index, tsc.value_kind);
                                                propValueAbc = abc;
                                            }
                                            break loopobjType;
                                        }
                                    }
                                    for (Trait t : abc.class_info.get(c).static_traits.traits) {
                                        if (t.name_index == name_index) {
                                            objType = multinameToType(abc.instance_info.get(c).name_index, constants);
                                            propType = AVM2SourceGenerator.getTraitReturnType(abcIndex, t);
                                            propIndex = t.name_index;
                                            if (t instanceof TraitSlotConst) {
                                                TraitSlotConst tsc = (TraitSlotConst) t;
                                                propValue = new ValueKind(tsc.value_index, tsc.value_kind);
                                                propValueAbc = abc;
                                            }
                                            break loopobjType;
                                        }
                                    }
                                }*/

                            for (ScriptInfo si : abc.script_info) {
                                if (si.deleted) {
                                    continue;
                                }
                                for (Trait t : si.traits.traits) {
                                    if (t.name_index == name_index) {
                                        isType.setVal(t instanceof TraitClass);
                                        objType = new TypeItem(DottedChain.OBJECT);
                                        propType = AVM2SourceGenerator.getTraitReturnType(abcIndex, t);
                                        propIndex = t.name_index;
                                        if (t instanceof TraitSlotConst) {
                                            TraitSlotConst tsc = (TraitSlotConst) t;
                                            propValue = new ValueKind(tsc.value_index, tsc.value_kind);
                                            propValueAbc = abc;
                                            propTrait = t;
                                        }
                                        break loopobjType;
                                    }
                                }
                            }
                        }
                        if (nsKind == Namespace.KIND_PACKAGE && propertyName != null) {
                            AbcIndexing.TraitIndex p = abcIndex.findNsProperty(new AbcIndexing.PropertyNsDef(propertyName, nsname, abc, openedNamespaces.get(i).getCpoolIndex(abcIndex)), true, true);

                            Reference<String> outName = new Reference<>("");
                            Reference<DottedChain> outNs = new Reference<>(DottedChain.EMPTY);
                            Reference<DottedChain> outPropNs = new Reference<>(DottedChain.EMPTY);
                            Reference<Integer> outPropNsKind = new Reference<>(1);
                            Reference<Integer> outPropNsIndex = new Reference<>(0);
                            Reference<GraphTargetItem> outPropType = new Reference<>(null);
                            Reference<ValueKind> outPropValue = new Reference<>(null);
                            Reference<ABC> outPropValueAbc = new Reference<>(null);
                            Reference<Trait> outPropTrait = new Reference<>(null);
                            if (p != null && (p.objType instanceof TypeItem)) {
                                List<Integer> otherns = new ArrayList<>();
                                for (NamespaceItem n : openedNamespaces) {
                                    if (n.isResolved()) {
                                        otherns.add(n.getCpoolIndex(abcIndex));
                                    }
                                }
                                if (AVM2SourceGenerator.searchPrototypeChain(namespaceSuffixInt, otherns, localData.privateNs, localData.protectedNs, false, abcIndex, nsname, (((TypeItem) p.objType).fullTypeName.getLast()), propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc, isType, outPropTrait)) {
                                    objType = new TypeItem(outNs.getVal().addWithSuffix(outName.getVal()));
                                    propType = p.returnType;
                                    propIndex = constants.getMultinameId(Multiname.createQName(false,
                                            constants.getStringId(propertyName, true),
                                            namespaceSuffixInt != null ? namespaceSuffixInt : constants.getNamespaceId(outPropNsKind.getVal(), outPropNs.getVal(), outPropNsIndex.getVal(), true)), true
                                    );
                                    propValue = p.value;
                                    propValueAbc = outPropValueAbc.getVal();
                                    propTrait = outPropTrait.getVal();
                                    break loopobjType;
                                }
                            }
                        }
                    }                    
                }
            }
        }

        if (propIndex == 0 && !mustExist) {
            String pname = propertyName;
            Multiname multiname;
            if (attribute && pname.isEmpty()) {
                multiname = Multiname.createMultinameL(true,
                        constants.getNamespaceSetId(new int[]{constants.getNamespaceId(Namespace.KIND_PACKAGE_INTERNAL, localData.pkg, 0, true)}, true));
            } else {
                int name_index = constants.getStringId("*".equals(pname) ? null : pname, true); //Note: name = * is for .@* attribute
                multiname = Multiname.createMultiname(attribute, name_index, allNsSet(abcIndex));
            }
            propIndex = constants.getMultinameId(multiname, true);
            propType = TypeItem.UNBOUNDED;
            objType = TypeItem.UNBOUNDED;
            propValue = null;
            propValueAbc = null;

        }
        propertyValue.setVal(propValue);
        propertyValueABC.setVal(propValueAbc);
        propertyIndex.setVal(propIndex);
        propertyType.setVal(propType);
        objectType.setVal(objType);
        propertyTrait.setVal(propTrait);
    }

    /**
     * Resolves property.
     * @param localData Local data
     * @return Property index
     * @throws CompilationException On compilation error
     */
    public int resolveProperty(SourceGeneratorLocalData localData) throws CompilationException {
        Reference<GraphTargetItem> objType = new Reference<>(null);
        Reference<GraphTargetItem> propType = new Reference<>(null);
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        Reference<ABC> outPropValueAbc = new Reference<>(null);
        Reference<Boolean> isType = new Reference<>(false);
        Reference<Trait> outPropTrait = new Reference<>(null);
        resolve(false, localData, isType, objType, propType, propIndex, outPropValue, outPropValueAbc, outPropTrait);
        return propIndex.getVal();
    }

    @Override
    public GraphTargetItem returnType() {

        Reference<GraphTargetItem> objType = new Reference<>(null);
        Reference<GraphTargetItem> propType = new Reference<>(null);
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        Reference<ABC> outPropValueAbc = new Reference<>(null);
        Reference<Boolean> isType = new Reference<>(false);
        Reference<Trait> outPropTrait = new Reference<>(null);
        try {
            resolve(false, new SourceGeneratorLocalData(new HashMap<>(), 0, false, 0)/*???*/, isType, objType, propType, propIndex, outPropValue, outPropValueAbc, outPropTrait);

            return propType.getVal();
        } catch (CompilationException ex) {
            Logger.getLogger(PropertyAVM2Item.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Converts to source.
     * @param localData Local data
     * @param generator Source generator
     * @param needsReturn Needs return
     * @return Source
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {

        
        AVM2SourceGenerator a2Generator = (AVM2SourceGenerator) generator;
        Reference<GraphTargetItem> objType = new Reference<>(null);
        Reference<GraphTargetItem> propType = new Reference<>(null);
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        Reference<ABC> outPropValueAbc = new Reference<>(null);
        Reference<Boolean> isType = new Reference<>(false);
        Reference<Trait> outPropTrait = new Reference<>(null);
        
        resolve(false, localData, isType, objType, propType, propIndex, outPropValue, outPropValueAbc, outPropTrait);

        int propertyId = propIndex.getVal();
        Object obj = resolveObject(localData, generator, assignedValue == null);
        Reference<Integer> ret_temp = new Reference<>(-1);
        boolean isSuper = (obj instanceof NameAVM2Item) && "super".equals(((NameAVM2Item) obj).getVariableName());
        if (assignedValue != null) {
            
            assignedValue = AVM2SourceGenerator.handleAndOrCoerce(assignedValue, propType.getVal());
            
            GraphTargetItem targetType = propType.getVal();
            String srcType = assignedValue.returnType().toString();
            GraphTargetItem coerced = assignedValue;
            if (!targetType.toString().equals(srcType) && !propertyName.startsWith("@")) {
                //coerced = makeCoerced(assignedValue, targetType);
            }
            return toSourceMerge(localData, generator,
                    isSuper ? null : obj,
                    coerced,
                    isSuper ? ins(AVM2Instructions.FindProperty, propertyId) : null,
                    isSuper ? ins(AVM2Instructions.Swap) : null,
                    needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                    ins(isSuper ? AVM2Instructions.SetSuper : AVM2Instructions.SetProperty, propertyId),
                    needsReturn ? getTemp(localData, generator, ret_temp) : null,
                    killTemp(localData, generator, Collections.singletonList(ret_temp)));
        } else {
            if (obj instanceof AVM2Instruction && (((AVM2Instruction) obj).definition instanceof FindPropertyStrictIns)) {
                return toSourceMerge(localData, generator, ins(AVM2Instructions.GetLex, propertyId),
                        needsReturn ? null : ins(AVM2Instructions.Pop)
                );
            }
            
            List<GraphSourceItem> onFalse = toSourceMerge(localData, generator,
                    isSuper ? ins(AVM2Instructions.FindPropertyStrict, propertyId) : obj,
                    ins(isSuper ? AVM2Instructions.GetSuper : AVM2Instructions.GetProperty, propertyId),
                    needsReturn ? null : ins(AVM2Instructions.Pop)
            );
            
            if (!nullish) {
                return onFalse;
            }
            
            
            AVM2Instruction ifFalse = ins(AVM2Instructions.IfFalse, 0);            
            List<GraphSourceItem> result = toSourceMerge(localData, generator, obj, 
                    ins(AVM2Instructions.PushNull),
                    ins(AVM2Instructions.Equals),
                     ifFalse
                    );
            
            List<GraphSourceItem> onTrue = new ArrayList<>();
            onTrue.add(ins(AVM2Instructions.PushNull));
            AVM2Instruction jump = ins(AVM2Instructions.Jump, 0);
            onTrue.add(jump);
            ifFalse.operands[0] = getBytesLen(onTrue);
            jump.operands[0] = getBytesLen(onFalse);
            result.addAll(onTrue);
            result.addAll(onFalse);                        
            return result;
        }
    }
    
    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, true);
    }
    
    private int getBytesLen(List<GraphSourceItem> code) {
        int len = 0;
        for (GraphSourceItem instruction : code) {
            AVM2Instruction ins = (AVM2Instruction) instruction;
            len += ins.getBytes().length;
        }
        return len;        
    }
    


    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, false);
    }

    @Override
    public String toString() {
        return "" + object + "." + propertyName;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    /**
     * Resolves object.
     * @param localData Local data
     * @param generator Source generator
     * @param mustExist Must exist
     * @return Object
     * @throws CompilationException On compilation error
     */
    public Object resolveObject(SourceGeneratorLocalData localData, SourceGenerator generator, boolean mustExist) throws CompilationException {
        Object obj = object;

        if (obj == null) {
            String cname = localData.currentClassBaseName;
            DottedChain pkgName = localData.pkg;
            Reference<String> outName = new Reference<>("");
            Reference<DottedChain> outNs = new Reference<>(DottedChain.EMPTY);
            Reference<DottedChain> outPropNs = new Reference<>(DottedChain.EMPTY);
            Reference<Integer> outPropNsKind = new Reference<>(1);
            Reference<Integer> outPropNsIndex = new Reference<>(0);
            Reference<GraphTargetItem> outPropType = new Reference<>(null);
            Reference<ValueKind> outPropValue = new Reference<>(null);
            Reference<ABC> outPropValueAbc = new Reference<>(null);
            Reference<Boolean> isType = new Reference<>(false);

            /*List<ABC> abcs = new ArrayList<>();
             abcs.add(abc);
             abcs.addAll(otherABCs);*/
            List<Integer> otherNs = new ArrayList<>();
            for (NamespaceItem n : openedNamespaces) {
                if (n.isResolved()) {
                    otherNs.add(n.getCpoolIndex(abcIndex));
                }
            }

            Integer namespaceSuffixInt = null;
            if (!"".equals(namespaceSuffix)) {
                namespaceSuffixInt = Integer.parseInt(namespaceSuffix.substring(1));
            }

            //For using this when appropriate (Non ASC2 approach):
            /*if (!localData.subMethod && cname != null && AVM2SourceGenerator.searchPrototypeChain(namespaceSuffixInt, otherNs, localData.privateNs, localData.protectedNs, true, abcIndex, pkgName, cname, propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc, isType) && (localData.getFullClass().equals(outNs.getVal().addWithSuffix(outName.getVal()).toRawString()))) {
                NameAVM2Item nobj = new NameAVM2Item(new TypeItem(localData.getFullClass()), 0, false, "this", "", null, false, openedNamespaces, abcIndex);
                nobj.setRegNumber(0);
                obj = nobj;
            } else {*/
            Reference<GraphTargetItem> objType = new Reference<>(null);
            Reference<GraphTargetItem> propType = new Reference<>(null);
            Reference<Integer> propIndex = new Reference<>(0);
            Reference<ValueKind> propValue = new Reference<>(null);
            Reference<ABC> propValueAbc = new Reference<>(null);
            Reference<Trait> propTrait = new Reference<>(null);

            resolve(false, localData, isType, objType, propType, propIndex, propValue, propValueAbc, propTrait);
            obj = ins(mustExist ? AVM2Instructions.FindPropertyStrict : AVM2Instructions.FindProperty, propIndex.getVal());
            //}
        }
        return obj;
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {

        Reference<GraphTargetItem> objType = new Reference<>(null);
        Reference<GraphTargetItem> propType = new Reference<>(null);
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        Reference<ABC> outPropValueAbc = new Reference<>(null);
        Reference<Boolean> isType = new Reference<>(false);
        Reference<Trait> outPropTrait = new Reference<>(null);

        resolve(false, localData, isType, objType, propType, propIndex, outPropValue, outPropValueAbc, outPropTrait);

        int propertyId = propIndex.getVal();
        Object obj = resolveObject(localData, generator, false);

        Reference<Integer> ret_temp = new Reference<>(-1);
        Reference<Integer> obj_temp = new Reference<>(-1);

        boolean isInteger = propType.getVal().toString().equals("int");

        AVM2Instruction changeIns;
        if (isInteger) {
            changeIns = ins(decrement ? AVM2Instructions.DecrementI : AVM2Instructions.IncrementI);
        } else if (localData.numberContext != null) {
            changeIns = ins(decrement ? AVM2Instructions.DecrementP : AVM2Instructions.IncrementP, localData.numberContext);
        } else {
            changeIns = ins(decrement ? AVM2Instructions.Decrement : AVM2Instructions.Increment);
        }
        
        List<GraphSourceItem> ret = toSourceMerge(localData, generator, obj, dupSetTemp(localData, generator, obj_temp),
                //Start get original
                //getTemp(localData, generator, obj_temp),
                //index!=null?getTemp(localData, generator, index_temp):null,
                ins(AVM2Instructions.GetProperty, propertyId),
                (!isInteger && post) ? ins(AVM2Instructions.ConvertD) : null,
                //End get original
                (!post) ? changeIns : null,
                needsReturn ? ins(AVM2Instructions.Dup) : null,
                (post) ? changeIns : null,
                setTemp(localData, generator, ret_temp),
                getTemp(localData, generator, obj_temp),
                getTemp(localData, generator, ret_temp),
                ins(AVM2Instructions.SetProperty, propertyId),
                //needsReturn?getTemp(localData, generator, ret_temp):null,
                killTemp(localData, generator, Arrays.asList(ret_temp, obj_temp)));
        return ret;
    }
}
