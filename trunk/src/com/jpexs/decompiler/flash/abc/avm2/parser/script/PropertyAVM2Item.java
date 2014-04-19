/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertDIns;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class PropertyAVM2Item extends AssignableAVM2Item {

    public String propertyName;
    public GraphTargetItem object;
    public ABC abc;
    public List<ABC> otherABCs;
    public GraphTargetItem index;
    private List<Integer> openedNamespaces;
    private List<MethodBody> callStack;
    public List<GraphTargetItem> scopeStack = new ArrayList<GraphTargetItem>();

    @Override
    public AssignableAVM2Item copy() {
        PropertyAVM2Item p = new PropertyAVM2Item(object, propertyName, index, abc, otherABCs, openedNamespaces, callStack);
        return p;
    }

    public PropertyAVM2Item(GraphTargetItem object, String propertyName, GraphTargetItem index, ABC abc, List<ABC> otherABCs, List<Integer> openedNamespaces, List<MethodBody> callStack) {
        this.propertyName = propertyName;
        this.object = object;
        this.otherABCs = otherABCs;
        this.abc = abc;
        this.index = index;
        this.openedNamespaces = openedNamespaces;
        this.callStack = callStack;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    private int allNsSet() {
        int nssa[] = new int[openedNamespaces.size()];
        for (int i = 0; i < openedNamespaces.size(); i++) {
            nssa[i] = openedNamespaces.get(i);
        }
        return abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);
    }

    public void resolve(SourceGeneratorLocalData localData, Reference<String> objectType, Reference<String> propertyType, Reference<Integer> propertyIndex, Reference<ValueKind> propertyValue) {
        String objType = null;
        String objSubType = null;
        ValueKind propValue = null;
        if (object != null) {
            GraphTargetItem oretType = object.returnType();
            if (oretType instanceof UnresolvedAVM2Item) {
                UnresolvedAVM2Item ur = (UnresolvedAVM2Item) oretType;
                oretType = ur.resolved;
            }
            if (oretType instanceof TypeItem) {
                TypeItem t = (TypeItem) oretType;
                objType = t.fullTypeName;
                if (!t.subtypes.isEmpty()) {
                    objSubType = t.subtypes.get(0);
                }
            } else {
                objType = oretType.toString();
            }
        }
        String propType = "";
        int propIndex = 0;
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
                     if (name.equals(n.getName(a.constants, new ArrayList<String>())) && n.getNamespace(a.constants).hasName(nsname,a.constants)) {
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
                if (objType == null) {
                    for (MethodBody b : callStack) {
                        for (int i = 0; i < b.traits.traits.size(); i++) {
                            Trait t = b.traits.traits.get(i);
                            if (t.getName(abc).getName(abc.constants, new ArrayList<String>()).equals(propertyName)) {
                                if (t instanceof TraitSlotConst) {
                                    TraitSlotConst tsc = (TraitSlotConst) t;
                                    objType = "Function";
                                    propType = tsc.type_index == 0 ? "*" : abc.constants.constant_multiname.get(tsc.type_index).getNameWithNamespace(abc.constants);
                                    propIndex = tsc.name_index;
                                    if (!localData.traitUsages.containsKey(b)) {
                                        localData.traitUsages.put(b, new ArrayList<Integer>());
                                    }
                                    localData.traitUsages.get(b).add(i);
                                }
                            }
                        }
                    }
                    if (objType == null) {
                        loopobjType:
                        for (int i = 0; i < openedNamespaces.size(); i++) {
                            int nsindex = openedNamespaces.get(i);
                            int nsKind = abc.constants.constant_namespace.get(openedNamespaces.get(i)).kind;
                            String nsname = abc.constants.constant_namespace.get(openedNamespaces.get(i)).getName(abc.constants);
                            int name_index = 0;
                            for (int m = 1; m < abc.constants.constant_multiname.size(); m++) {
                                Multiname mname = abc.constants.constant_multiname.get(m);
                                if (mname.kind == Multiname.QNAME && mname.getName(abc.constants, new ArrayList<String>()).equals(propertyName) && mname.namespace_index == nsindex) {
                                    name_index = m;
                                    break;
                                }
                            }
                            if (name_index > 0) {
                                for (int c = 0; c < abc.instance_info.size(); c++) {
                                    for (Trait t : abc.instance_info.get(c).instance_traits.traits) {
                                        if (t.name_index == name_index) {
                                            objType = abc.instance_info.get(c).getName(abc.constants).getNameWithNamespace(abc.constants);
                                            propType = AVM2SourceGenerator.getTraitReturnType(abc, t).toString();
                                            propIndex = t.name_index;
                                            if (t instanceof TraitSlotConst) {
                                                TraitSlotConst tsc = (TraitSlotConst) t;
                                                propValue = new ValueKind(tsc.value_index, tsc.value_kind);
                                            }
                                            break loopobjType;
                                        }
                                    }
                                    for (Trait t : abc.class_info.get(c).static_traits.traits) {
                                        if (t.name_index == name_index) {
                                            objType = abc.instance_info.get(c).getName(abc.constants).getNameWithNamespace(abc.constants);
                                            propType = AVM2SourceGenerator.getTraitReturnType(abc, t).toString();
                                            propIndex = t.name_index;
                                            if (t instanceof TraitSlotConst) {
                                                TraitSlotConst tsc = (TraitSlotConst) t;
                                                propValue = new ValueKind(tsc.value_index, tsc.value_kind);
                                            }
                                            break loopobjType;
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
                                            Reference<String> outPropType = new Reference<>("");
                                            Reference<ValueKind> outPropValue = new Reference<>(null);
                                            if (AVM2SourceGenerator.searchPrototypeChain(false, abcs, nsname, n.getName(a.constants, new ArrayList<String>()), propertyName, outName, outNs, outPropNs, outPropNsKind, outPropType, outPropValue)) {
                                                objType = "".equals(outNs.getVal()) ? outName.getVal() : outNs.getVal() + "." + outName.getVal();
                                                propType = outPropType.getVal();
                                                propIndex = abc.constants.getMultinameId(new Multiname(Multiname.QNAME,
                                                        abc.constants.getStringId(propertyName, true),
                                                        abc.constants.getNamespaceId(new Namespace(outPropNsKind.getVal(), abc.constants.getStringId(outPropNs.getVal(), true)), 0, true), 0, 0, new ArrayList<Integer>()), true
                                                );
                                                propValue = outPropValue.getVal();
                                                break loopobjType;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    List<ABC> abcs = new ArrayList<>();
                    abcs.add(abc);
                    abcs.addAll(otherABCs);
                    if (objType.equals("__AS3__.vec.Vector")) {
                        if ("int".equals(objSubType)) {
                            objType = "__AS3__.vec.Vector$int";
                        } else if ("Number".equals(objSubType)) {
                            objType = "__AS3__.vec.Vector$double";
                        } else if ("uint".equals(objSubType)) {
                            objType = "__AS3__.vec.Vector$uint";
                        } else {
                            objType = "__AS3__.vec.Vector$object";
                        }
                    }
                    loopa:
                    for (ABC a : abcs) {
                        for (InstanceInfo ii : a.instance_info) {
                            Multiname m = ii.getName(a.constants);
                            if (m.getNameWithNamespace(a.constants).equals(objType)) {
                                Reference<String> outName = new Reference<>("");
                                Reference<String> outNs = new Reference<>("");
                                Reference<String> outPropNs = new Reference<>("");
                                Reference<Integer> outPropNsKind = new Reference<>(1);
                                Reference<String> outPropType = new Reference<>("");
                                Reference<ValueKind> outPropValue = new Reference<>(null);
                                if (AVM2SourceGenerator.searchPrototypeChain(false, abcs, m.getNamespace(a.constants).getName(a.constants), m.getName(a.constants, new ArrayList<String>()), propertyName, outName, outNs, outPropNs, outPropNsKind, outPropType, outPropValue)) {
                                    objType = "".equals(outNs.getVal()) ? outName.getVal() : outNs.getVal() + "." + outName.getVal();
                                    propType = outPropType.getVal();
                                    propIndex = abc.constants.getMultinameId(new Multiname(Multiname.QNAME,
                                            abc.constants.getStringId(propertyName, true),
                                            abc.constants.getNamespaceId(new Namespace(outPropNsKind.getVal(), abc.constants.getStringId(outPropNs.getVal(), true)), 0, true), 0, 0, new ArrayList<Integer>()), true
                                    );
                                    propValue = outPropValue.getVal();

                                    break loopa;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (propIndex == 0) {
            String pname = propertyName;
            boolean attr = pname.startsWith("@");
            if (attr) {
                pname = pname.substring(1);
            }
            propIndex = abc.constants.getMultinameId(new Multiname(attr ? (pname.isEmpty() ? Multiname.MULTINAMELA : Multiname.MULTINAMEA) : Multiname.MULTINAME,
                    abc.constants.getStringId(pname, true), 0,
                    attr && pname.isEmpty() ? abc.constants.getNamespaceSetId(new NamespaceSet(new int[]{abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE_INTERNAL, abc.constants.getStringId(localData.pkg, true)), 0, true)}), true) : allNsSet(), 0, new ArrayList<Integer>()), true);
            propType = "*";
            objType = "*";
            propValue = null;

        }
        propertyValue.setVal(propValue);
        propertyIndex.setVal(propIndex);
        propertyType.setVal(propType);
        objectType.setVal(objType);
    }

    public int resolveProperty(SourceGeneratorLocalData localData) {
        Reference<String> objType = new Reference<>("");
        Reference<String> propType = new Reference<>("");
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        resolve(localData, objType, propType, propIndex, outPropValue);
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
     if (mname.kind == Multiname.QNAME && mname.getName(abc.constants, new ArrayList<String>()).equals(propertyName) && mname.namespace_index == nsindex) {
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
     if (AVM2SourceGenerator.searchPrototypeChain(abcs, nsname, n.getName(a.constants, new ArrayList<String>()), propertyName, outName, outNs, outPropNs, outPropNsKind)) {
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
     String tnames = t.getName(a).getName(a.constants, new ArrayList<String>());
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
     String tnames = t.getName(a).getName(a.constants, new ArrayList<String>());
     if (tnames.equals(propertyName)) {
     return abc.constants.getMultinameId(new Multiname(tname.kind,
     abc.constants.getStringId(tnames, true),
     abc.constants.getNamespaceId(new Namespace(tname.getNamespace(a.constants).kind, abc.constants.getStringId(tname.getNamespace(a.constants).getName(a.constants), true)), 0, true), 0, 0, new ArrayList<Integer>()), true);
     }
     }
     for (Trait t : a.class_info.get(ci).static_traits.traits) {
     Multiname tname = t.getName(a);
     String tnames = t.getName(a).getName(a.constants, new ArrayList<String>());
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
     String tnames = t.getName(a).getName(a.constants, new ArrayList<String>());
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
        if (index != null) {
            return TypeItem.UNBOUNDED;
        }
        Reference<String> objType = new Reference<>("");
        Reference<String> propType = new Reference<>("");
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        resolve(null, objType, propType, propIndex, outPropValue);

        return new TypeItem(propType.getVal());
    }

    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {

        Reference<String> objType = new Reference<>("");
        Reference<String> propType = new Reference<>("");
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        resolve(localData, objType, propType, propIndex, outPropValue);

        int propertyId = propIndex.getVal();
        Object obj = resolveObject(localData, generator);
        Reference<Integer> ret_temp = new Reference<>(-1);
        if (assignedValue != null) {
            String targetType = propType.getVal();
            String srcType = assignedValue.returnType().toString();
            GraphTargetItem st = assignedValue;
            if (!targetType.equals(srcType) && !propertyName.startsWith("@")) {
                st = new CoerceAVM2Item(null, assignedValue, targetType);
            }
            return toSourceMerge(localData, generator, obj, index, st,
                    needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                    ins(new SetPropertyIns(), propertyId),
                    needsReturn ? getTemp(localData, generator, ret_temp) : null,
                    killTemp(localData, generator, Arrays.asList(ret_temp)));
        } else {
            if (obj instanceof AVM2Instruction && (((AVM2Instruction) obj).definition instanceof FindPropertyStrictIns) && index == null) {
                return toSourceMerge(localData, generator, ins(new GetLexIns(), propertyId),
                        needsReturn ? null : ins(new PopIns())
                );
            }
            return toSourceMerge(localData, generator, obj, index,
                    ins(new GetPropertyIns(), propertyId),
                    needsReturn ? null : ins(new PopIns())
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

    private Object resolveObject(SourceGeneratorLocalData localData, SourceGenerator generator) {
        Object obj = object;

        if (obj == null) {
            String cname;
            String pkgName = "";
            cname = localData.currentClass;
            if (cname != null && cname.contains(".")) {
                pkgName = cname.substring(0, cname.lastIndexOf('.'));
                cname = cname.substring(cname.lastIndexOf('.') + 1);
            }
            Reference<String> outName = new Reference<>("");
            Reference<String> outNs = new Reference<>("");
            Reference<String> outPropNs = new Reference<>("");
            Reference<Integer> outPropNsKind = new Reference<>(1);
            Reference<String> outPropType = new Reference<>("");
            Reference<ValueKind> outPropValue = new Reference<>(null);
            List<ABC> abcs = new ArrayList<>();
            abcs.add(abc);
            abcs.addAll(otherABCs);
            if (cname != null && AVM2SourceGenerator.searchPrototypeChain(true, abcs, pkgName, cname, propertyName, outName, outNs, outPropNs, outPropNsKind, outPropType, outPropValue)) {
                NameAVM2Item nobj = new NameAVM2Item(new TypeItem(localData.currentClass), 0, "this", null, false, openedNamespaces);
                nobj.setRegNumber(0);
                obj = nobj;
            } else {
                Reference<String> objType = new Reference<>("");
                Reference<String> propType = new Reference<>("");
                Reference<Integer> propIndex = new Reference<>(0);
                Reference<ValueKind> propValue = new Reference<>(null);
                resolve(localData, objType, propType, propIndex, outPropValue);
                obj = ins(new FindPropertyStrictIns(), propIndex.getVal());
            }
        }
        return obj;
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {

        Reference<String> objType = new Reference<>("");
        Reference<String> propType = new Reference<>("");
        Reference<Integer> propIndex = new Reference<>(0);
        Reference<ValueKind> outPropValue = new Reference<>(null);
        resolve(localData, objType, propType, propIndex, outPropValue);

        int propertyId = propIndex.getVal();
        Object obj = resolveObject(localData, generator);

        Reference<Integer> ret_temp = new Reference<>(-1);
        Reference<Integer> obj_temp = new Reference<>(-1);
        Reference<Integer> index_temp = new Reference<>(-1);

        boolean isInteger = propType.getVal().equals("int");

        List<GraphSourceItem> ret = toSourceMerge(localData, generator, obj, dupSetTemp(localData, generator, obj_temp), index, index != null ? dupSetTemp(localData, generator, index_temp) : null,
                //Start get original
                //getTemp(localData, generator, obj_temp),
                //index!=null?getTemp(localData, generator, index_temp):null,
                ins(new GetPropertyIns(), propertyId),
                !isInteger ? ins(new ConvertDIns()) : null,
                //End get original
                (!post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                needsReturn ? ins(new DupIns()) : null,
                (post) ? (decrement ? ins(isInteger ? new DecrementIIns() : new DecrementIns()) : ins(isInteger ? new IncrementIIns() : new IncrementIns())) : null,
                setTemp(localData, generator, ret_temp),
                getTemp(localData, generator, obj_temp),
                index != null ? getTemp(localData, generator, index_temp) : null,
                getTemp(localData, generator, ret_temp),
                ins(new SetPropertyIns(), propertyId),
                //needsReturn?getTemp(localData, generator, ret_temp):null,
                killTemp(localData, generator, Arrays.asList(ret_temp, obj_temp, index_temp)));
        return ret;
    }

}
