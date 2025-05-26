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
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Deobfuscation;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.AlchemyTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewClassIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetOuterScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.List;

/**
 * Dependency parser.
 */
public class DependencyParser {

    /**
     * Parses dependencies from namespace.
     * @param abcIndex AbcIndexing
     * @param ignoredCustom Ignored custom
     * @param abc ABC
     * @param dependencies Dependencies
     * @param namespace_index Namespace index
     * @param ignorePackage Ignore package
     * @param name Name
     * @param dependencyType Dependency type
     * @param uses Uses
     */
    public static void parseDependenciesFromNS(AbcIndexing abcIndex, String ignoredCustom, ABC abc, List<Dependency> dependencies, int namespace_index, DottedChain ignorePackage, String name, DependencyType dependencyType, List<String> uses) {
        Namespace ns = abc.constants.getNamespace(namespace_index);
        if (name.isEmpty()) {
            name = "*";
        }
        DottedChain newimport = ns.getName(abc.constants);

        if (ns.kind == Namespace.KIND_NAMESPACE || ns.kind == Namespace.KIND_PACKAGE_INTERNAL) {
            String nsVal = ns.getName(abc.constants).toRawString();
            DottedChain nsimport = abcIndex.nsValueToName(nsVal);
            if (nsimport != null) {
                if (nsimport.equals(AVM2Deobfuscation.BUILTIN)) {
                    return; //builtin, no dependency
                }
                if (!uses.contains(nsimport.getLast())) {
                    uses.add(nsimport.getLast());
                }
                if (!nsimport.isEmpty()) {
                    Dependency depNs = new Dependency(nsimport, DependencyType.NAMESPACE);
                    if ((ignorePackage == null || !nsimport.getWithoutLast().equals(ignorePackage)) && !dependencies.contains(depNs)) {
                        dependencies.add(depNs);
                    }
                    if (ignoredCustom != null && nsVal.equals(ignoredCustom)) {
                        return;
                    }
                    return;
                }
            }
        }

        if (dependencyType == DependencyType.NAMESPACE) {
            return;
        }
        if (ns.kind != Namespace.KIND_PACKAGE) { // && (ns.kind != Namespace.KIND_PACKAGE_INTERNAL)) {
            return;
        }
        newimport = newimport.addWithSuffix(name);
        Dependency dep = new Dependency(newimport, dependencyType);

        if (!dependencies.contains(dep)) {
            DottedChain pkg = newimport.getWithoutLast(); //.substring(0, newimport.lastIndexOf('.'));
            if (pkg.equals(InitVectorAVM2Item.VECTOR_PACKAGE)) { //special case - is imported always
                return;
            }
            dependencies.add(dep);
        }
    }

    /**
     * Parses dependencies from multiname.
     * @param abcIndex AbcIndexing
     * @param ignoredCustom Ignored custom
     * @param abc ABC
     * @param dependencies Dependencies
     * @param m Multiname
     * @param ignorePackage Ignore package
     * @param fullyQualifiedNames Fully qualified names
     * @param dependencyType Dependency type
     * @param uses Uses
     */
    public static void parseDependenciesFromMultiname(AbcIndexing abcIndex, String ignoredCustom, ABC abc, List<Dependency> dependencies, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, DependencyType dependencyType, List<String> uses) {
        if (m != null) {
            if (m.kind == Multiname.TYPENAME) {
                if (m.qname_index != 0) {
                    parseDependenciesFromMultiname(abcIndex, ignoredCustom, abc, dependencies, abc.constants.getMultiname(m.qname_index), ignorePackage, fullyQualifiedNames, dependencyType, uses);
                }
                for (Integer i : m.params) {
                    if (i != 0) {
                        parseDependenciesFromMultiname(abcIndex, ignoredCustom, abc, dependencies, abc.constants.getMultiname(i), ignorePackage, fullyQualifiedNames, dependencyType, uses);
                    }
                }
                return;
            }
            Namespace ns = m.getNamespace(abc.constants);
            String name = m.getName(abc.constants, fullyQualifiedNames, true, true);
            NamespaceSet nss = m.getNamespaceSet(abc.constants);
            if (ns != null) {
                parseDependenciesFromNS(abcIndex, ignoredCustom, abc, dependencies, m.namespace_index, ignorePackage, name, dependencyType, uses);
            }
            if (nss != null) {
                for (int n : nss.namespaces) {
                    parseDependenciesFromNS(abcIndex, ignoredCustom, abc, dependencies, n, ignorePackage, nss.namespaces.length > 1 ? "" : name, dependencyType, uses);
                }
            }
        }
    }

    /**
     * Parses dependencies from method info.
     * @param abcIndex AbcIndexing
     * @param trait Trait
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static
     * @param ignoredCustom Ignored custom
     * @param abc ABC
     * @param method_index Method index
     * @param dependencies Dependencies
     * @param ignorePackage Ignore package
     * @param fullyQualifiedNames Fully qualified names
     * @param visitedMethods Visited methods
     * @param uses Uses
     * @param numberContextRef Number context reference
     * @throws InterruptedException On interrupt
     */
    public static void parseDependenciesFromMethodInfo(AbcIndexing abcIndex, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String ignoredCustom, ABC abc, int method_index, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<Integer> visitedMethods, List<String> uses, Reference<Integer> numberContextRef) throws InterruptedException {
        if ((method_index < 0) || (method_index >= abc.method_info.size())) {
            return;
        }
        visitedMethods.add(method_index);
        if (abc.method_info.get(method_index).ret_type != 0) {
            parseDependenciesFromMultiname(abcIndex, ignoredCustom, abc, dependencies, abc.constants.getMultiname(abc.method_info.get(method_index).ret_type), ignorePackage, fullyQualifiedNames, DependencyType.SIGNATURE, uses);
        }
        for (int t : abc.method_info.get(method_index).param_types) {
            if (t != 0) {
                parseDependenciesFromMultiname(abcIndex, ignoredCustom, abc, dependencies, abc.constants.getMultiname(t), ignorePackage, fullyQualifiedNames, DependencyType.SIGNATURE, uses);
            }
        }
        MethodBody body = abc.findBody(method_index);
        if (body != null && body.convertException == null) {
            body = body.convertMethodBodyCanUseLast(Configuration.autoDeobfuscate.get(), "", isStatic, scriptIndex, classIndex, abc, trait);
            body.traits.getDependencies(abcIndex, scriptIndex, classIndex, isStatic, ignoredCustom, abc, dependencies, ignorePackage, fullyQualifiedNames, uses, numberContextRef);
            for (ABCException ex : body.exceptions) {
                parseDependenciesFromMultiname(abcIndex, ignoredCustom, abc, dependencies, abc.constants.getMultiname(ex.type_index), ignorePackage, fullyQualifiedNames, DependencyType.EXPRESSION /* or signature?*/, uses);
            }
            
            boolean hasNewClass = false;
            
            if (classIndex == -1) {
                for (int i = 0; i < body.getCode().code.size(); i++) {
                    AVM2Instruction ins = body.getCode().code.get(i);
                    if (ins.definition instanceof NewClassIns) {
                        hasNewClass = true;
                        break;
                    }                
                }
            }
            boolean wasNewClass = false;
            for (int i = 0; i < body.getCode().code.size(); i++) {
                AVM2Instruction ins = body.getCode().code.get(i);
                
                
                //Do not parse dependencies from class parent chain
                if (classIndex == -1 && hasNewClass && !wasNewClass) {
                    
                    if (ins.definition instanceof NewClassIns) {
                        wasNewClass = true;
                    }
                    
                    continue;
                }
                
                //Ignore class parents in script initializer
                if (ins.definition instanceof GetLexIns) {
                    boolean foundNewClass = false;
                    for (int j = i + 1; j < body.getCode().code.size(); j++) {
                        AVM2Instruction insJ = body.getCode().code.get(j);
                        if (insJ.definition instanceof NewClassIns) {
                            foundNewClass = true;
                            break;
                        } else if (ins.definition instanceof GetLexIns) {
                            //continue
                        } else if (ins.definition instanceof PushScopeIns) {
                            //continue
                        } else {
                            break;
                        }
                    }
                    if (foundNewClass) {
                        continue;
                    }
                }
                
                if (ins.definition instanceof AlchemyTypeIns) {
                    DottedChain nimport = AlchemyTypeIns.ALCHEMY_PACKAGE.addWithSuffix(ins.definition.instructionName);
                    Dependency depExp = new Dependency(nimport, DependencyType.EXPRESSION);
                    if (!dependencies.contains(depExp)) {
                        dependencies.add(depExp);
                    }
                }
                if (ins.definition instanceof NewFunctionIns) {
                    if (ins.operands[0] != method_index) {
                        if (!visitedMethods.contains(ins.operands[0])) {
                            parseDependenciesFromMethodInfo(abcIndex, trait, scriptIndex, classIndex, isStatic, ignoredCustom, abc, ins.operands[0], dependencies, ignorePackage, fullyQualifiedNames, visitedMethods, uses, numberContextRef);
                        }
                    }
                }
                if (classIndex > -1 && ins.definition instanceof GetOuterScopeIns) {
                    if (ins.operands[0] > 0) { //first is global
                        DottedChain type = abc.instance_info.get(classIndex).getName(abc.constants).getNameWithNamespace(abc.constants, true);
                        AbcIndexing.ClassIndex cls = abcIndex.findClass(new TypeItem(type), abc, scriptIndex);
                        List<AbcIndexing.ClassIndex> clsList = new ArrayList<>();
                        cls = cls.parent;
                        while (cls != null) {
                            clsList.add(0, cls);
                            cls = cls.parent;
                        }
                        if (ins.operands[0] < 1 + clsList.size()) {
                            AbcIndexing.ClassIndex cls2 = clsList.get(ins.operands[0] - 1);
                            DottedChain nimport = cls2.abc.instance_info.get(cls2.index).getName(cls2.abc.constants).getNameWithNamespace(cls2.abc.constants, true);
                            Dependency depExp = new Dependency(nimport, DependencyType.EXPRESSION);
                            if (!dependencies.contains(depExp)) {
                                dependencies.add(depExp);
                            }
                        }
                    }

                    /*for (AbcIndexing.ClassIndex cls2: clsList) {
                        newScopeStack.push(new ClassAVM2Item(cls2.abc.instance_info.get(cls2.index).getName(cls2.abc.constants).getNameWithNamespace(cls2.abc.constants, true)));
                    } */
                }
                for (int k = 0; k < ins.definition.operands.length; k++) {
                    //this should probably handle only some subset of multiname instructions,
                    // like findproperty, findpropstrict, constructprop, not simple getproperty
                    if (ins.definition.operands[k] == AVM2Code.DAT_MULTINAME_INDEX) {
                        int m = ins.operands[k];
                        if (m < abc.constants.getMultinameCount()) {
                            parseDependenciesFromMultiname(abcIndex, ignoredCustom, abc, dependencies, abc.constants.getMultiname(m), ignorePackage, fullyQualifiedNames, DependencyType.EXPRESSION, uses);
                        }
                    }
                    if (ins.definition.operands[k] == AVM2Code.DAT_NUMBER_CONTEXT) {
                        numberContextRef.setVal(ins.operands[k]);
                    }
                }
            }
        }
    }
}
