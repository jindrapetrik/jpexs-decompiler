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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class PropertyAVM2Item extends AssignableAVM2Item {

    public String propertyName;

    public GraphTargetItem object;

    public AbcIndexing abcIndex;

    private final List<NamespaceItem> openedNamespaces;

    private final List<MethodBody> callStack;

    public List<GraphTargetItem> scopeStack = new ArrayList<>();

    @Override
    public AssignableAVM2Item copy() {
        PropertyAVM2Item p = new PropertyAVM2Item(object, propertyName, abcIndex, openedNamespaces, callStack);
        return p;
    }

    public PropertyAVM2Item(GraphTargetItem object, String propertyName, AbcIndexing abcIndex, List<NamespaceItem> openedNamespaces, List<MethodBody> callStack) {
        this.propertyName = propertyName;
        this.object = object;
        this.abcIndex = abcIndex;
        this.openedNamespaces = openedNamespaces;
        this.callStack = callStack;
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

    private static GraphTargetItem multinameToType(Set<Integer> visited, int m_index, AVM2ConstantPool constants) {
        if (visited.contains(m_index)) {
            Logger.getLogger(PropertyAVM2Item.class.getName()).log(Level.WARNING, "Recursive typename detected");
            return null;
        }
        if (m_index == 0) {
            return TypeItem.UNBOUNDED;
        }
        Multiname m = constants.getMultiname(m_index);
        if (m.kind == Multiname.TYPENAME) {
            visited.add(m_index);
            GraphTargetItem obj = multinameToType(visited, m.qname_index, constants);
            if (obj == null) {
                return null;
            }
            List<GraphTargetItem> params = new ArrayList<>();
            for (int pm : m.params) {
                GraphTargetItem r = multinameToType(visited, pm, constants);
                if (r == null) {
                    return null;
                }
                if (pm == 0) {
                    r = new NullAVM2Item(null, null);
                }
                params.add(r);
            }
            return new ApplyTypeAVM2Item(null, null, obj, params);
        } else {
            return new TypeItem(m.getNameWithNamespace(constants, true));
        }
    }

    public static GraphTargetItem multinameToType(int m_index, AVM2ConstantPool constants) {
        return multinameToType(new HashSet<>(), m_index, constants);
    }

    public void resolve(boolean mustExist, SourceGeneratorLocalData localData, Reference<GraphTargetItem> objectType, Reference<GraphTargetItem> propertyType, Reference<Integer> propertyIndex, Reference<ValueKind> propertyValue, Reference<ABC> propertyValueABC) throws CompilationException {
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
        int propIndex = 0;
        ABC abc = abcIndex.getSelectedAbc();
        AVM2ConstantPool constants = abc.constants;
        if (!propertyName.startsWith("@")) {

            if (scopeStack.isEmpty()) { //Everything is multiname when with command
                if (objType == null) {

                    /*for (GraphTargetItem s : scopeStack) {
                     String oType = s.returnType().toString();
                     String name = oType;
                     String nsname = "";
                     if(name.contains(".")){
                     nsname = name.substring(0,name.lastIndexOf("."));
                     name = name.substring(name.lastIndexOf(".")+1);
                     }

                     List<ABC> abcs = new ArrayList<>();
                     abcs.add(abc);
                     abcs.addAll(otherABCs);
                     loopabc:
                     for (ABC a : abcs) {
                     for (int h = 0; h < a.instance_info.size(); h++) {
                     InstanceInfo ii = a.instance_info.get(h);
                     Multiname n = a.constants.constant_multiname.get(ii.name_index);
                     if (name.equals(n.getName(a.constants, new ArrayList<>())) && n.getNamespace(a.constants).hasName(nsname,a.constants)) {
                     Reference<String> outName = new Reference<>("");
                     Reference<String> outNs = new Reference<>("");
                     Reference<String> outPropNs = new Reference<>("");
                     Reference<Integer> outPropNsKind = new Reference<>(1);
                     Reference<String> outPropType = new Reference<>("");
                     if (AVM2SourceGenerator.searchPrototypeChain(false, abcs, nsname, name, propertyName, outName, outNs, outPropNs, outPropNsKind, outPropType)) {
                     objType = "".equals(outNs.getVal()) ? outName.getVal() : outNs.getVal() + "." + outName.getVal();
                     propType = outPropType.getVal();
                     propIndex = abc.constants.getMultinameId(new Multiname(Multiname.QNAME,
                     abc.constants.getStringId(propertyName, true),
                     abc.constants.getNamespaceId(new Namespace(outPropNsKind.getVal(), abc.constants.getStringId(outPropNs.getVal(), true)), 0, true), 0, 0, new ArrayList<Integer>()), true
                     );

                     break loopabc;
                     }
                     }
                     }
                     }
                     }*/
                }

                GraphTargetItem ttype = objType;
                if (ttype == null) {
                    ttype = thisType;
                }

                {
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
                    /*                    AbcIndexing.TraitIndex p = abc.findProperty(new AbcIndexing.PropertyDef(propertyName, ttype), false, true);
                     if(p!=null){
                     objType = new TypeItem(outNs.getVal().isEmpty() ? outName.getVal() : outNs.getVal().toRawString() + "." + outName.getVal());
                     propType = p.type;
                     propIndex = abc.getLastAbc().constants.getMultinameId(new Multiname(Multiname.QNAME,
                     abc.getLastAbc().getStringId(propertyName, true),
                     abc.getLastAbc().getNamespaceId(new Namespace(outPropNsKind.getVal(), abc.constants.getStringId(outPropNs.getVal(), true)), outPropNsIndex.getVal(), true), 0, 0, new ArrayList<>()), true
                     );
                     propValue = outPropValue.getVal();
                     }*/
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
                        List<Integer> otherNs = new ArrayList<>();
                        for (NamespaceItem n : openedNamespaces) {
                            if (n.isResolved()) {
                                otherNs.add(n.getCpoolIndex(abcIndex));
                            }
                        }
                        if (AVM2SourceGenerator.searchPrototypeChain(otherNs, localData.privateNs, localData.protectedNs, false, abcIndex, ftn.getWithoutLast(), ftn.getLast(), propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc)) {
                            objType = new TypeItem(outNs.getVal().addWithSuffix(outName.getVal()));
                            propType = outPropType.getVal();
                            propIndex = constants.getMultinameId(Multiname.createQName(false,
                                    constants.getStringId(propertyName, true),
                                    constants.getNamespaceId(outPropNsKind.getVal(), outPropNs.getVal(), outPropNsIndex.getVal(), true)), true
                            );
                            propValue = outPropValue.getVal();
                            propValueAbc = outPropValueAbc.getVal();
                        }
                    }

                    if (objType == null) {
                        for (MethodBody b : callStack) {
                            for (int i = 0; i < b.traits.traits.size(); i++) {
                                Trait t = b.traits.traits.get(i);
                                if (t.getName(abc).getName(constants, null, true, true).equals(propertyName)) {
                                    if (t instanceof TraitSlotConst) {
                                        TraitSlotConst tsc = (TraitSlotConst) t;
                                        objType = new TypeItem(DottedChain.FUNCTION);
                                        propType = multinameToType(tsc.type_index, constants);
                                        propIndex = tsc.name_index;
                                        if (!localData.traitUsages.containsKey(b)) {
                                            localData.traitUsages.put(b, new ArrayList<>());
                                        }
                                        localData.traitUsages.get(b).add(i);
                                    }
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
                                int name_index = 0;
                                for (int m = 1; m < constants.getMultinameCount(); m++) {
                                    Multiname mname = constants.getMultiname(m);
                                    if (mname.kind == Multiname.QNAME && mname.getName(constants, null, true, true).equals(propertyName) && mname.namespace_index == nsindex) {
                                        name_index = m;
                                        break;
                                    }
                                }
                                if (name_index > 0) {
                                    for (int c = 0; c < abc.instance_info.size(); c++) {
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
                                    }

                                    for (ScriptInfo si : abc.script_info) {
                                        if (si.deleted) {
                                            continue;
                                        }
                                        for (Trait t : si.traits.traits) {
                                            if (t.name_index == name_index) {
                                                objType = new TypeItem(DottedChain.OBJECT);
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
                                    if (p != null && (p.objType instanceof TypeItem)) {
                                        List<Integer> otherns = new ArrayList<>();
                                        for (NamespaceItem n : openedNamespaces) {
                                            if (n.isResolved()) {
                                                otherns.add(n.getCpoolIndex(abcIndex));
                                            }
                                        }
                                        if (AVM2SourceGenerator.searchPrototypeChain(otherns, localData.privateNs, localData.protectedNs, false, abcIndex, nsname, (((TypeItem) p.objType).fullTypeName.getLast()), propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc)) {
                                            objType = new TypeItem(outNs.getVal().addWithSuffix(outName.getVal()));
                                            propType = p.returnType;
                                            propIndex = constants.getMultinameId(Multiname.createQName(false,
                                                    constants.getStringId(propertyName, true),
                                                    constants.getNamespaceId(outPropNsKind.getVal(), outPropNs.getVal(), outPropNsIndex.getVal(), true)), true
                                            );
                                            propValue = p.value;
                                            propValueAbc = outPropValueAbc.getVal();
                                            break loopobjType;
                                        }
                                    }

                                    //if (propertyName != null && AVM2SourceGenerator.searchPrototypeChain(false, abcs, nsname, n.getName(a.constants, null, true), propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue)) {
                                    //
                                    //}
                                }
                            }
                        }
                    }
                }

            }
        }

        if (propIndex == 0 && !mustExist) {
            String pname = propertyName;
            boolean attr = pname.startsWith("@");
            if (attr) {
                pname = pname.substring(1);
            }
            Multiname multiname;
            if (attr && pname.isEmpty()) {
                multiname = Multiname.createMultinameL(true,
                        constants.getNamespaceSetId(new int[]{constants.getNamespaceId(Namespace.KIND_PACKAGE_INTERNAL, localData.pkg, 0, true)}, true));
            } else {
                int name_index = constants.getStringId("*".equals(pname) ? null : pname, true); //Note: name = * is for .@* attribute
                multiname = Multiname.createMultiname(attr, name_index, allNsSet(abcIndex));
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
    }

    public int resolveProperty(SourceGeneratorLocalData localData) throws CompilationException {
        Reference<GraphTargetItem> objType = new Reference<>(null);
        Reference<GraphTargetItem> propType = new Reference<>(null);
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        Reference<ABC> outPropValueAbc = new Reference<>(null);
        resolve(false, localData, objType, propType, propIndex, outPropValue, outPropValueAbc);
        return propIndex.getVal();
    }

    /*
     private String resolveObjectType() {
     String objType = object == null ? null : object.returnType().toString();
     if (objType == null) {
     loopo:
     for (int i = 0; i < openedNamespaces.size(); i++) {
     int nsindex = openedNamespaces.get(i);
     int nsKind = abc.constants.constant_namespace.get(openedNamespaces.get(i)).kind;
     String nsname = abc.constants.constant_namespace.get(openedNamespaces.get(i)).getName(abc.constants);
     int name_index = 0;
     for (int m = 1; m < abc.constants.constant_multiname.size(); m++) {
     Multiname mname = abc.constants.constant_multiname.get(m);
     if (mname.kind == Multiname.QNAME && mname.getName(abc.constants, new ArrayList<>()).equals(propertyName) && mname.namespace_index == nsindex) {
     name_index = m;
     break;
     }
     }
     if (name_index > 0) {
     for (int s = 0; s < abc.script_info.size(); s++) {
     for (Trait t : abc.script_info.get(s).traits.traits) {
     if (t.name_index == name_index) {
     return getTraitReturnType(abc, t).toString();
     }
     }
     }
     for (int c = 0; c < abc.instance_info.size(); c++) {
     for (Trait t : abc.instance_info.get(c).instance_traits.traits) {
     if (t.name_index == name_index) {
     return getTraitReturnType(abc, t).toString();
     }
     }
     for (Trait t : abc.class_info.get(c).static_traits.traits) {
     if (t.name_index == name_index) {
     return getTraitReturnType(abc, t).toString();
     }
     }
     }
     }
     if (nsKind == Namespace.KIND_PACKAGE) {
     List<ABC> abcs = new ArrayList<>();
     abcs.add(abc);
     abcs.addAll(otherABCs);
     loopabc:
     for (ABC a : otherABCs) {
     for (int h = 0; h < a.instance_info.size(); h++) {
     InstanceInfo ii = a.instance_info.get(h);
     Multiname n = a.constants.constant_multiname.get(ii.name_index);
     if (n.getNamespace(a.constants).kind == Namespace.KIND_PACKAGE && n.getNamespace(a.constants).getName(a.constants).equals(nsname)) {
     Reference<String> outName = new Reference<>("");
     Reference<String> outNs = new Reference<>("");
     Reference<String> outPropNs = new Reference<>("");
     Reference<Integer> outPropNsKind = new Reference<>(1);
     if (AVM2SourceGenerator.searchPrototypeChain(abcs, nsname, n.getName(a.constants, new ArrayList<>()), propertyName, outName, outNs, outPropNs, outPropNsKind)) {
     return "".equals(outNs.getVal()) ? outName.getVal() : outNs.getVal() + "." + outName.getVal();
     }
     }
     }
     }
     }
     }
     }
     if (objType == null) {
     throw new RuntimeException("Unresolved object type");
     }
     return objType;
     }*/

 /*
     public GraphTargetItem resolvePropertyType() {
     if (index != null) {
     return TypeItem.UNBOUNDED;
     }

     String objType = resolveObjectType();
     for (ABC a : abcs) {
     int ci = a.findClassByName(objType);
     if (ci != -1) {
     for (Trait t : a.instance_info.get(ci).instance_traits.traits) {
     String tnames = t.getName(a).getName(a.constants, new ArrayList<>());
     if (tnames.equals(propertyName)) {
     if (t instanceof TraitSlotConst) {
     TraitSlotConst tsc = (TraitSlotConst) t;
     if (tsc.type_index == 0) {
     return TypeItem.UNBOUNDED;
     }
     return new TypeItem(a.constants.constant_multiname.get(tsc.type_index).getNameWithNamespace(a.constants));
     }
     if (t instanceof TraitMethodGetterSetter) {
     TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
     if (tmgs.kindType == Trait.TRAIT_GETTER) {
     return new TypeItem(a.constants.constant_multiname.get(a.method_info.get(tmgs.method_info).ret_type).getNameWithNamespace(a.constants));
     }
     if (tmgs.kindType == Trait.TRAIT_SETTER) {
     return new TypeItem(a.constants.constant_multiname.get(a.method_info.get(tmgs.method_info).param_types[0]).getNameWithNamespace(a.constants));
     }
     }
     if (t instanceof TraitFunction) {
     return new TypeItem("Function");
     }
     return TypeItem.UNBOUNDED;
     }
     }
     break;
     }
     }
     return TypeItem.UNBOUNDED;
     }
     */
 /* public int resolveProperty() {
     if (index != null) {
     return abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL,
     abc.constants.getStringId(propertyName, true), 0,
     allNsSet(), 0, new ArrayList<Integer>()), true);
     }

     String objType = resolveObjectType();
     for (ABC a : abcs) {
     int ci = a.findClassByName(objType);
     if (ci != -1) {
     for (Trait t : a.instance_info.get(ci).instance_traits.traits) {
     Multiname tname = t.getName(a);
     String tnames = t.getName(a).getName(a.constants, new ArrayList<>());
     if (tnames.equals(propertyName)) {
     return abc.constants.getMultinameId(new Multiname(tname.kind,
     abc.constants.getStringId(tnames, true),
     abc.constants.getNamespaceId(new Namespace(tname.getNamespace(a.constants).kind, abc.constants.getStringId(tname.getNamespace(a.constants).getName(a.constants), true)), 0, true), 0, 0, new ArrayList<Integer>()), true);
     }
     }
     for (Trait t : a.class_info.get(ci).static_traits.traits) {
     Multiname tname = t.getName(a);
     String tnames = t.getName(a).getName(a.constants, new ArrayList<>());
     if (tnames.equals(propertyName)) {
     return abc.constants.getMultinameId(new Multiname(tname.kind,
     abc.constants.getStringId(tnames, true),
     abc.constants.getNamespaceId(new Namespace(tname.getNamespace(a.constants).kind, abc.constants.getStringId(tname.getNamespace(a.constants).getName(a.constants), true)), 0, true), 0, 0, new ArrayList<Integer>()), true);
     }
     }
     break;
     }
     }

     for (ABC a : abcs) {
     for (ScriptInfo si : a.script_info) {
     for (Trait t : si.traits.traits) {
     Multiname tname = t.getName(a);
     String tnames = t.getName(a).getName(a.constants, new ArrayList<>());
     if (tnames.equals(propertyName)) {
     return abc.constants.getMultinameId(new Multiname(tname.kind,
     abc.constants.getStringId(tnames, true),
     abc.constants.getNamespaceId(new Namespace(tname.getNamespace(a.constants).kind, abc.constants.getStringId(tname.getNamespace(a.constants).getName(a.constants), true)), 0, true), 0, 0, new ArrayList<Integer>()), true);
     }
     }
     }
     }

     return abc.constants.getMultinameId(new Multiname(Multiname.MULTINAME,
     abc.constants.getStringId(propertyName, true), 0,
     allNsSet(), 0, new ArrayList<Integer>()), true);
     }*/
    @Override
    public GraphTargetItem returnType() {

        Reference<GraphTargetItem> objType = new Reference<>(null);
        Reference<GraphTargetItem> propType = new Reference<>(null);
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        Reference<ABC> outPropValueAbc = new Reference<>(null);
        try {
            resolve(false, new SourceGeneratorLocalData(new HashMap<>(), 0, false, 0)/*???*/, objType, propType, propIndex, outPropValue, outPropValueAbc);

            return propType.getVal();
        } catch (CompilationException ex) {
            Logger.getLogger(PropertyAVM2Item.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {

        Reference<GraphTargetItem> objType = new Reference<>(null);
        Reference<GraphTargetItem> propType = new Reference<>(null);
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        Reference<ABC> outPropValueAbc = new Reference<>(null);

        resolve(false, localData, objType, propType, propIndex, outPropValue, outPropValueAbc);

        int propertyId = propIndex.getVal();
        Object obj = resolveObject(localData, generator);
        Reference<Integer> ret_temp = new Reference<>(-1);
        if (assignedValue != null) {
            GraphTargetItem targetType = propType.getVal();
            String srcType = assignedValue.returnType().toString();
            GraphTargetItem coerced = assignedValue;
            if (!targetType.toString().equals(srcType) && !propertyName.startsWith("@")) {
                coerced = makeCoerced(assignedValue, targetType);
            }
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
    public String toString() {
        return "" + object + "." + propertyName;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    public Object resolveObject(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        Object obj = object;

        if (obj == null) {
            String cname = localData.currentClass;
            DottedChain pkgName = localData.pkg;
            Reference<String> outName = new Reference<>("");
            Reference<DottedChain> outNs = new Reference<>(DottedChain.EMPTY);
            Reference<DottedChain> outPropNs = new Reference<>(DottedChain.EMPTY);
            Reference<Integer> outPropNsKind = new Reference<>(1);
            Reference<Integer> outPropNsIndex = new Reference<>(0);
            Reference<GraphTargetItem> outPropType = new Reference<>(null);
            Reference<ValueKind> outPropValue = new Reference<>(null);
            Reference<ABC> outPropValueAbc = new Reference<>(null);

            /*List<ABC> abcs = new ArrayList<>();
             abcs.add(abc);
             abcs.addAll(otherABCs);*/
            List<Integer> otherNs = new ArrayList<>();
            for (NamespaceItem n : openedNamespaces) {
                if (n.isResolved()) {
                    otherNs.add(n.getCpoolIndex(abcIndex));
                }
            }
            if (!localData.subMethod && cname != null && AVM2SourceGenerator.searchPrototypeChain(otherNs, localData.privateNs, localData.protectedNs, true, abcIndex, pkgName, cname, propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc) && (localData.getFullClass().equals(outNs.getVal().addWithSuffix(outName.getVal()).toRawString()))) {
                NameAVM2Item nobj = new NameAVM2Item(new TypeItem(localData.getFullClass()), 0, "this", null, false, openedNamespaces);
                nobj.setRegNumber(0);
                obj = nobj;
            } else {
                Reference<GraphTargetItem> objType = new Reference<>(null);
                Reference<GraphTargetItem> propType = new Reference<>(null);
                Reference<Integer> propIndex = new Reference<>(0);
                Reference<ValueKind> propValue = new Reference<>(null);
                Reference<ABC> propValueAbc = new Reference<>(null);

                resolve(false, localData, objType, propType, propIndex, outPropValue, propValueAbc);
                obj = ins(AVM2Instructions.FindPropertyStrict, propIndex.getVal());
            }
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

        resolve(false, localData, objType, propType, propIndex, outPropValue, outPropValueAbc);

        int propertyId = propIndex.getVal();
        Object obj = resolveObject(localData, generator);

        Reference<Integer> ret_temp = new Reference<>(-1);
        Reference<Integer> obj_temp = new Reference<>(-1);

        boolean isInteger = propType.getVal().toString().equals("int");

        List<GraphSourceItem> ret = toSourceMerge(localData, generator, obj, dupSetTemp(localData, generator, obj_temp),
                //Start get original
                //getTemp(localData, generator, obj_temp),
                //index!=null?getTemp(localData, generator, index_temp):null,
                ins(AVM2Instructions.GetProperty, propertyId),
                (!isInteger && post) ? ins(AVM2Instructions.ConvertD) : null,
                //End get original
                (!post) ? (decrement ? ins(isInteger ? AVM2Instructions.DecrementI : AVM2Instructions.Decrement) : ins(isInteger ? AVM2Instructions.IncrementI : AVM2Instructions.Increment)) : null,
                needsReturn ? ins(AVM2Instructions.Dup) : null,
                (post) ? (decrement ? ins(isInteger ? AVM2Instructions.DecrementI : AVM2Instructions.Decrement) : ins(isInteger ? AVM2Instructions.IncrementI : AVM2Instructions.Increment)) : null,
                setTemp(localData, generator, ret_temp),
                getTemp(localData, generator, obj_temp),
                getTemp(localData, generator, ret_temp),
                ins(AVM2Instructions.SetProperty, propertyId),
                //needsReturn?getTemp(localData, generator, ret_temp):null,
                killTemp(localData, generator, Arrays.asList(ret_temp, obj_temp)));
        return ret;
    }
}
