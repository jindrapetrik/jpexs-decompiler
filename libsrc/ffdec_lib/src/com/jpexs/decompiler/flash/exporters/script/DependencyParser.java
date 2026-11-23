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
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Deobfuscation;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.AlchemyTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructPropIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewClassIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugFileIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugLineIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetOuterScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        DottedChain identifier = DottedChain.TOPLEVEL.addWithSuffix(name.isEmpty() ? "*" : name);
        parseDependenciesFromNS(abcIndex, abc, -1, -1, dependencies, namespace_index, ignorePackage,
                identifier.getLast(), identifier.getLastNamespaceSuffix(), dependencyType, new NamespaceUses(uses, false), false, false);
    }

    private static void parseDependenciesFromNS(AbcIndexing abcIndex, ABC abc, int scriptIndex, int classIndex, List<Dependency> dependencies,
            int namespace_index, DottedChain ignorePackage, String name, String namespaceSuffix, DependencyType dependencyType, NamespaceUses uses, boolean openNamespace, boolean memberAccess) {
        Namespace ns = abc.constants.getNamespace(namespace_index);
        DottedChain newimport = ns.getName(abc.constants);

        if (ns.kind == Namespace.KIND_NAMESPACE || ns.kind == Namespace.KIND_PACKAGE_INTERNAL) {
            String nsVal = ns.getRawName(abc.constants);
            DottedChain local = abc.findScriptPrivateNamespace(nsVal, scriptIndex);
            if (local != null) {
                uses.add(local.getLast());
                return;
            }
            DottedChain nsimport = abcIndex.nsValueToName(nsVal);
            if (nsimport != null && !nsimport.isEmpty()) {
                if (nsimport.equals(AVM2Deobfuscation.BUILTIN)) {
                    return; //builtin, no dependency
                }
                uses.add(nsimport.getLast());
                Dependency depNs = new Dependency(nsimport, DependencyType.NAMESPACE);
                if ((ignorePackage == null || !nsimport.getWithoutLast().equals(ignorePackage)) && !dependencies.contains(depNs)) {
                    dependencies.add(depNs);
                }
            } else if (uses.collectUndeclared && ns.kind == Namespace.KIND_NAMESPACE
                    && !nsVal.isEmpty() && abc.findClassNamespace(nsVal, classIndex) == null
                    && !isInterfaceNamespace(abcIndex, abc, scriptIndex, nsVal, memberAccess)) {
                uses.undeclared.merge(nsVal, openNamespace, (oldValue, newValue) -> oldValue || newValue);
            }
            return;
        }

        if (dependencyType == DependencyType.NAMESPACE) {
            return;
        }
        if (ns.kind != Namespace.KIND_PACKAGE) { // && (ns.kind != Namespace.KIND_PACKAGE_INTERNAL)) {
            return;
        }
        newimport = newimport.add(name, namespaceSuffix);
        Dependency dep = new Dependency(newimport, dependencyType);

        if (!dependencies.contains(dep)) {
            DottedChain pkg = newimport.getWithoutLast(); //.substring(0, newimport.lastIndexOf('.'));
            if (pkg.equals(InitVectorAVM2Item.VECTOR_PACKAGE)) { //special case - is imported always
                return;
            }
            dependencies.add(dep);
        }
    }

    private static boolean isInterfaceNamespace(AbcIndexing abcIndex, ABC abc, int scriptIndex, String uri, boolean memberAccess) {
        // AVM2 names interface members with an implicit package:Interface namespace.
        DottedChain name = DottedChain.parseNoSuffix(uri.replace(':', '.'));
        String interfaceUri = name.size() > 1 ? name.getWithoutLast().toRawString() + ":" + name.getLast() : name.getLast();
        if (!uri.equals(interfaceUri)) {
            return false;
        }
        AbcIndexing.ClassIndex type = abcIndex.findClass(new TypeItem(name), abc, scriptIndex < 0 ? null : scriptIndex);
        if (type != null) {
            return type.abc.instance_info.get(type.index).isInterface();
        }
        if (hasScriptInterfaceNamespace(abc, scriptIndex, uri)) {
            return true;
        }
        if (!memberAccess || name.size() < 2) {
            return false;
        }
        String pkg = name.getWithoutLast().toRawString();
        String typeName = name.getLast();
        if (abc.constants.getQnameId(typeName, Namespace.KIND_PACKAGE, pkg, false) > 0) {
            return true;
        }
        // A base interface from a linked library can survive only as its member
        // namespace. Recognize the interface naming convention in a known package.
        return typeName.length() > 1 && typeName.charAt(0) == 'I' && Character.isUpperCase(typeName.charAt(1))
                && IdentifiersDeobfuscation.isValidNameWithDot(true, name.toRawString())
                && abc.constants.getNamespaceId(Namespace.KIND_PACKAGE, pkg, 0, false) > 0;
    }

    private static boolean hasScriptInterfaceNamespace(ABC abc, int scriptIndex, String uri) {
        if (scriptIndex < 0 || scriptIndex >= abc.script_info.size()) {
            return false;
        }
        for (Trait trait : abc.script_info.get(scriptIndex).traits.traits) {
            if (!(trait instanceof TraitClass) || trait.deleted) {
                continue;
            }
            InstanceInfo instance = abc.instance_info.get(((TraitClass) trait).class_info);
            if (!instance.isInterface() || instance.deleted) {
                continue;
            }
            for (Trait member : instance.instance_traits.traits) {
                Multiname memberName = member.getName(abc);
                if (!member.deleted && memberName.getSimpleNamespaceKind(abc.constants) == Namespace.KIND_NAMESPACE
                        && uri.equals(memberName.getSimpleNamespaceName(abc.constants).toRawString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Parses dependencies from multiname.
     * 
     * @param usedDeobfuscations Used deobfuscations
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
    public static void parseDependenciesFromMultiname(Set<String> usedDeobfuscations, AbcIndexing abcIndex, String ignoredCustom, ABC abc, List<Dependency> dependencies, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, DependencyType dependencyType, List<String> uses) {
        parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, ignoredCustom, abc, -1, dependencies, m, ignorePackage, fullyQualifiedNames, dependencyType, uses);
    }

    /**
     * Parses multiname dependencies in the scope of the given script.
     *
     * @param usedDeobfuscations Used deobfuscations
     * @param abcIndex AbcIndexing
     * @param ignoredCustom Ignored custom
     * @param abc ABC
     * @param scriptIndex Script index, or -1 if unknown
     * @param dependencies Dependencies
     * @param m Multiname
     * @param ignorePackage Ignore package
     * @param fullyQualifiedNames Fully qualified names
     * @param dependencyType Dependency type
     * @param uses Uses
     */
    public static void parseDependenciesFromMultiname(Set<String> usedDeobfuscations, AbcIndexing abcIndex, String ignoredCustom, ABC abc, int scriptIndex, List<Dependency> dependencies, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, DependencyType dependencyType, List<String> uses) {
        parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, ignoredCustom, abc, scriptIndex, -1, dependencies, m, ignorePackage, fullyQualifiedNames, dependencyType, new NamespaceUses(uses, false));
    }

    public static void parseDependenciesFromMultiname(Set<String> usedDeobfuscations, AbcIndexing abcIndex, String ignoredCustom, ABC abc, int scriptIndex, int classIndex, List<Dependency> dependencies, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, DependencyType dependencyType, NamespaceUses uses) {
        parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, ignoredCustom, abc, scriptIndex, classIndex,
                dependencies, m, ignorePackage, fullyQualifiedNames, dependencyType, uses, false);
    }

    private static void parseDependenciesFromMultiname(Set<String> usedDeobfuscations, AbcIndexing abcIndex, String ignoredCustom, ABC abc, int scriptIndex, int classIndex, List<Dependency> dependencies, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, DependencyType dependencyType, NamespaceUses uses, boolean memberAccess) {
        if (m != null) {
            if (m.kind == Multiname.TYPENAME) {
                if (m.qname_index != 0) {
                    parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, ignoredCustom, abc, scriptIndex, classIndex, dependencies, abc.constants.getMultiname(m.qname_index), ignorePackage, fullyQualifiedNames, dependencyType, uses);
                }
                for (Integer i : m.params) {
                    if (i != 0) {
                        parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, ignoredCustom, abc, scriptIndex, classIndex, dependencies, abc.constants.getMultiname(i), ignorePackage, fullyQualifiedNames, dependencyType, uses);
                    }
                }
                return;
            }
            Namespace ns = m.getNamespace(abc.constants);
            // A raw ABC name such as #3 can look like a namespace suffix.
            String name = m.getName(usedDeobfuscations, abc, abc.constants, null, true, false);
            String namespaceSuffix = m.getNamespaceSuffix();
            if (name.isEmpty()) {
                name = "*";
                namespaceSuffix = "";
            }
            NamespaceSet nss = m.getNamespaceSet(abc.constants);
            if (ns != null && (!m.doesDisplayNamespace() || (uses.collectUndeclared && ns.kind == Namespace.KIND_NAMESPACE))) {
                parseDependenciesFromNS(abcIndex, abc, scriptIndex, classIndex, dependencies, m.namespace_index, ignorePackage, name, namespaceSuffix, dependencyType, uses, false, memberAccess);
            }
            if (nss != null) {
                boolean wildcard = nss.namespaces.length > 1;
                for (int n : nss.namespaces) {
                    parseDependenciesFromNS(abcIndex, abc, scriptIndex, classIndex, dependencies, n, ignorePackage,
                            wildcard ? "*" : name, wildcard ? "" : namespaceSuffix, dependencyType, uses, wildcard || m.needsName(), false);
                }
            }
        }
    }

    /**
     * Parses dependencies from nested newfunction bodies in a method only.
     * Used for script initializers: file-private script functions are wired via
     * newfunction there, while the rest of script_init only registers the public
     * class and must not contribute imports.
     *
     * @param usedDeobfuscations Used deobfuscations
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
     * @param uses Uses
     * @param numberContextRef Number context reference
     * @throws InterruptedException On interrupt
     */
    public static void parseDependenciesFromNewFunctionsInMethodInfo(Set<String> usedDeobfuscations, AbcIndexing abcIndex, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String ignoredCustom, ABC abc, int method_index, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<String> uses, Reference<Integer> numberContextRef) throws InterruptedException {
        parseDependenciesFromNewFunctionsInMethodInfo(usedDeobfuscations, abcIndex, trait, scriptIndex, classIndex, isStatic, ignoredCustom, abc, method_index, dependencies, ignorePackage, fullyQualifiedNames, new NamespaceUses(uses, false), numberContextRef);
    }

    public static void parseDependenciesFromNewFunctionsInMethodInfo(Set<String> usedDeobfuscations, AbcIndexing abcIndex, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String ignoredCustom, ABC abc, int method_index, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, NamespaceUses uses, Reference<Integer> numberContextRef) throws InterruptedException {
        if ((method_index < 0) || (method_index >= abc.method_info.size())) {
            return;
        }
        MethodBody body = abc.findBody(method_index);
        if (body == null || body.convertException != null) {
            return;
        }
        body = body.convertMethodBodyCanUseLast(Configuration.autoDeobfuscate.get(), "", isStatic, scriptIndex, classIndex, abc, trait);
        List<Integer> visitedMethods = new ArrayList<>();
        for (AVM2Instruction ins : body.getCode().code) {
            if (ins.definition instanceof NewFunctionIns) {
                if (ins.operands[0] != method_index && !visitedMethods.contains(ins.operands[0])) {
                    parseDependenciesFromMethodInfo(usedDeobfuscations, abcIndex, trait, scriptIndex, classIndex, isStatic, ignoredCustom, abc, ins.operands[0], dependencies, ignorePackage, fullyQualifiedNames, visitedMethods, uses, numberContextRef);
                }
            }
        }
    }

    /**
     * Parses dependencies from method info.
     * 
     * @param usedDeobfuscations Used deobfuscations
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
    public static void parseDependenciesFromMethodInfo(Set<String> usedDeobfuscations, AbcIndexing abcIndex, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String ignoredCustom, ABC abc, int method_index, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<Integer> visitedMethods, List<String> uses, Reference<Integer> numberContextRef) throws InterruptedException {
        parseDependenciesFromMethodInfo(usedDeobfuscations, abcIndex, trait, scriptIndex, classIndex, isStatic, ignoredCustom, abc, method_index, dependencies, ignorePackage, fullyQualifiedNames, visitedMethods, new NamespaceUses(uses, false), numberContextRef);
    }

    public static void parseDependenciesFromMethodInfo(Set<String> usedDeobfuscations, AbcIndexing abcIndex, Trait trait, int scriptIndex, int classIndex, boolean isStatic, String ignoredCustom, ABC abc, int method_index, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<Integer> visitedMethods, NamespaceUses uses, Reference<Integer> numberContextRef) throws InterruptedException {
        if ((method_index < 0) || (method_index >= abc.method_info.size())) {
            return;
        }
        visitedMethods.add(method_index);
        if (abc.method_info.get(method_index).ret_type != 0) {
            parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, ignoredCustom, abc, scriptIndex, classIndex, dependencies, abc.constants.getMultiname(abc.method_info.get(method_index).ret_type), ignorePackage, fullyQualifiedNames, DependencyType.SIGNATURE, uses);
        }
        for (int t : abc.method_info.get(method_index).param_types) {
            if (t != 0) {
                parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, ignoredCustom, abc, scriptIndex, classIndex, dependencies, abc.constants.getMultiname(t), ignorePackage, fullyQualifiedNames, DependencyType.SIGNATURE, uses);
            }
        }
        MethodBody body = abc.findBody(method_index);
        if (body != null && body.convertException == null) {
            body = body.convertMethodBodyCanUseLast(Configuration.autoDeobfuscate.get(), "", isStatic, scriptIndex, classIndex, abc, trait);
            body.traits.getDependencies(usedDeobfuscations, abcIndex, scriptIndex, classIndex, isStatic, ignoredCustom, abc, dependencies, ignorePackage, fullyQualifiedNames, uses, numberContextRef);
            for (ABCException ex : body.exceptions) {
                parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, ignoredCustom, abc, scriptIndex, classIndex, dependencies, abc.constants.getMultiname(ex.type_index), ignorePackage, fullyQualifiedNames, DependencyType.EXPRESSION /* or signature?*/, uses);
            }
            
            List<AVM2Instruction> instructions = body.getCode().code;
            // Each getlex used to scan the entire remaining method for a
            // newclass. A single suffix boundary answers all of those queries.
            int lastNewClass = -1;
            for (int i = instructions.size() - 1; i >= 0; i--) {
                if (instructions.get(i).definition instanceof NewClassIns) {
                    lastNewClass = i;
                    break;
                }
            }
            boolean hasNewClass = lastNewClass >= 0;
            boolean wasNewClass = false;
            AVM2Instruction prevIns = null;
            for (int i = 0; i < instructions.size(); i++) {
                AVM2Instruction ins = instructions.get(i);
                AVM2Instruction nextIns = i + 1 < instructions.size() ? instructions.get(i + 1) : null;
                
                
                //Do not parse dependencies from class parent chain
                if (classIndex == -1 && hasNewClass && !wasNewClass) {
                    
                    if (ins.definition instanceof NewClassIns) {
                        wasNewClass = true;
                    }
                    
                    continue;
                }
                
                //Ignore class parents in script initializer
                if (ins.definition instanceof GetLexIns && i < lastNewClass) {
                    continue;
                }

                if (ins.definition instanceof AlchemyTypeIns) {
                    DottedChain nimport = AlchemyTypeIns.ALCHEMY_PACKAGE.addWithSuffix(ins.definition.instructionName);
                    Dependency depExp = new Dependency(nimport, DependencyType.EXPRESSION);
                    if (!dependencies.contains(depExp)) {
                        dependencies.add(depExp);
                    }
                }
                if (ins.definition instanceof NewFunctionIns) {
                    if (nextIns == null || !(nextIns.definition instanceof PopIns)) { //in obfuscated code the newfunction is immediately popped, do not process such functions
                        if (ins.operands[0] != method_index) {
                            if (!visitedMethods.contains(ins.operands[0])) {
                                parseDependenciesFromMethodInfo(usedDeobfuscations, abcIndex, trait, scriptIndex, classIndex, isStatic, ignoredCustom, abc, ins.operands[0], dependencies, ignorePackage, fullyQualifiedNames, visitedMethods, uses, numberContextRef);
                            }
                        }
                    }                    
                }
                if (ins.definition instanceof ConstructPropIns) {
                    Multiname m = abc.constants.getMultiname(ins.operands[0]);
                    if (m != null) {
                        if (m.kind == Multiname.MULTINAMEL) {
                            Dependency dep = new Dependency(DottedChain.parseNoSuffix("flash.utils.getDefinitionByName"), DependencyType.NAMESPACE);
                            dependencies.add(dep);
                        }
                    }
                }
                
                //TODO: what if there's jump between getproperty and construct?
                if (ins.definition instanceof ConstructIns && prevIns != null && prevIns.definition instanceof GetPropertyIns) {
                    Multiname m = abc.constants.getMultiname(prevIns.operands[0]);
                    if (m != null) {
                        if (m.kind == Multiname.MULTINAMEL) {
                            Dependency dep = new Dependency(DottedChain.parseNoSuffix("flash.utils.getDefinitionByName"), DependencyType.NAMESPACE);
                            dependencies.add(dep);
                        }
                    }
                }
                // callproperty return types are often used to type locals (var x:Ret
                // = obj.method()) even when the ABC has no coerce to Ret. Only when
                // the result is stored in a local (setlocal) — casts like
                // iterator() as IMapIterator already expose their type via coerce.
                if (ins.definition instanceof CallPropertyIns && prevIns != null && classIndex > -1
                        && isFollowedBySetLocal(instructions, i)) {
                    parseDependenciesFromCallPropertyReturnType(usedDeobfuscations, abcIndex, ignoredCustom, abc, scriptIndex, classIndex, dependencies, ignorePackage, fullyQualifiedNames, uses, prevIns, ins);
                }
                if (classIndex > -1 && ins.definition instanceof GetOuterScopeIns) {
                    if (ins.operands[0] > 0) { //first is global
                        DottedChain type = abc.instance_info.get(classIndex).getName(abc.constants).getNameWithNamespace(usedDeobfuscations, abc, abc.constants, true);
                        AbcIndexing.ClassIndex cls = abcIndex.findClass(new TypeItem(type), abc, scriptIndex);
                        List<AbcIndexing.ClassIndex> clsList = new ArrayList<>();
                        cls = cls.parent;
                        while (cls != null) {
                            clsList.add(0, cls);
                            cls = cls.parent;
                        }
                        if (ins.operands[0] < 1 + clsList.size()) {
                            AbcIndexing.ClassIndex cls2 = clsList.get(ins.operands[0] - 1);
                            DottedChain nimport = cls2.abc.instance_info.get(cls2.index).getName(cls2.abc.constants).getNameWithNamespace(usedDeobfuscations, cls2.abc, cls2.abc.constants, true);
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
                        int multinameIndex = ins.operands[k];
                        if (multinameIndex < abc.constants.getMultinameCount()) {
                            Multiname multiname = abc.constants.getMultiname(multinameIndex);
                            boolean lexicalLookup = ins.definition instanceof GetLexIns
                                    || ins.definition instanceof FindPropertyIns || ins.definition instanceof FindPropertyStrictIns
                                    || ins.definition instanceof ConstructPropIns;
                            DottedChain packageProperty = lexicalLookup
                                    ? abcIndex.findLexicalPackageProperty(abc, scriptIndex, multiname) : null;
                            if (packageProperty != null) {
                                Dependency dependency = new Dependency(packageProperty, DependencyType.EXPRESSION);
                                if (!dependencies.contains(dependency)) {
                                    dependencies.add(dependency);
                                }
                            } else {
                                boolean memberAccess = ins.definition instanceof CallPropertyIns || ins.definition instanceof CallPropVoidIns
                                        || ins.definition instanceof GetPropertyIns || ins.definition instanceof SetPropertyIns
                                        || ins.definition instanceof ConstructPropIns;
                                parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, ignoredCustom, abc, scriptIndex, classIndex,
                                        dependencies, multiname, ignorePackage, fullyQualifiedNames, DependencyType.EXPRESSION, uses, memberAccess);
                            }
                        }
                    }
                    if (uses.collectUndeclared && ins.definition.operands[k] == AVM2Code.DAT_NAMESPACE_INDEX) {
                        int nsIndex = ins.operands[k];
                        if (nsIndex > 0 && nsIndex < abc.constants.getNamespaceCount()) {
                            parseDependenciesFromNS(abcIndex, abc, scriptIndex, classIndex, dependencies, nsIndex, ignorePackage,
                                    "", "", DependencyType.NAMESPACE, uses, false, false);
                        }
                    }
                    if (ins.definition.operands[k] == AVM2Code.DAT_NUMBER_CONTEXT) {
                        numberContextRef.setVal(ins.operands[k]);
                    }
                }
                prevIns = ins;
            }
        }
    }

    /**
     * When {@code getlex}/{@code getproperty} is followed by {@code callproperty},
     * the call's return type may appear in decompiled local declarations without
     * a matching {@code coerce} multiname. Resolve that return type and import it.
     */
    private static void parseDependenciesFromCallPropertyReturnType(Set<String> usedDeobfuscations, AbcIndexing abcIndex, String ignoredCustom, ABC abc, int scriptIndex, int classIndex, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, NamespaceUses uses, AVM2Instruction prevIns, AVM2Instruction callIns) {
        GraphTargetItem receiverType = null;
        if (prevIns.definition instanceof GetLexIns || prevIns.definition instanceof GetPropertyIns) {
            Multiname receiverMn = abc.constants.getMultiname(prevIns.operands[0]);
            if (receiverMn == null) {
                return;
            }
            String receiverName = receiverMn.getName(usedDeobfuscations, abc, abc.constants, fullyQualifiedNames, true, true);
            if (receiverName == null || receiverName.isEmpty()) {
                return;
            }
            DottedChain currentClass = abc.instance_info.get(classIndex).getName(abc.constants).getNameWithNamespace(usedDeobfuscations, abc, abc.constants, true);
            Reference<Boolean> foundStatic = new Reference<>(false);
            GraphTargetItem currentType = new TypeItem(currentClass);
            int receiverNs = receiverMn.namespace_index;
            receiverType = abcIndex.findPropertyType(abc, currentType, receiverName, receiverNs, true, true, true, foundStatic);
            if (receiverType == TypeItem.UNBOUNDED || receiverType == TypeItem.UNKNOWN) {
                // getlex of a class itself (static call): treat multiname as the type
                receiverType = AbcIndexing.multinameToType(usedDeobfuscations, prevIns.operands[0], abc, abc.constants);
            }
        }
        if (receiverType == null || receiverType == TypeItem.UNBOUNDED || receiverType == TypeItem.UNKNOWN) {
            return;
        }
        // AbcIndexing.findProperty* only accepts plain TypeItem (not ApplyTypeAVM2Item).
        GraphTargetItem lookupType = receiverType;
        if (lookupType instanceof ApplyTypeAVM2Item) {
            lookupType = ((ApplyTypeAVM2Item) lookupType).object;
        }
        if (!(lookupType instanceof TypeItem) || lookupType == TypeItem.UNBOUNDED || lookupType == TypeItem.UNKNOWN) {
            return;
        }
        Multiname callMn = abc.constants.getMultiname(callIns.operands[0]);
        if (callMn == null) {
            return;
        }
        String propName = callMn.getName(usedDeobfuscations, abc, abc.constants, fullyQualifiedNames, true, true);
        if (propName == null || propName.isEmpty()) {
            return;
        }
        Reference<Boolean> foundStatic = new Reference<>(false);
        GraphTargetItem callType = abcIndex.findPropertyCallType(abc, lookupType, propName, 0, true, true, true, foundStatic);
        parseDependenciesFromTypeItem(usedDeobfuscations, abcIndex, ignoredCustom, abc, dependencies, ignorePackage, fullyQualifiedNames, uses, callType);
    }

    /**
     * True if the next non-debug instruction after {@code callIndex} stores the
     * value in a local register.
     */
    private static boolean isFollowedBySetLocal(List<AVM2Instruction> code, int callIndex) {
        for (int j = callIndex + 1; j < code.size(); j++) {
            Object def = code.get(j).definition;
            if (def instanceof DebugLineIns || def instanceof DebugFileIns || def instanceof DebugIns) {
                continue;
            }
            return def instanceof SetLocalTypeIns;
        }
        return false;
    }

    private static void parseDependenciesFromTypeItem(Set<String> usedDeobfuscations, AbcIndexing abcIndex, String ignoredCustom, ABC abc, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, NamespaceUses uses, GraphTargetItem type) {
        if (type == null || type == TypeItem.UNBOUNDED || type == TypeItem.UNKNOWN) {
            return;
        }
        if (type instanceof ApplyTypeAVM2Item) {
            ApplyTypeAVM2Item at = (ApplyTypeAVM2Item) type;
            parseDependenciesFromTypeItem(usedDeobfuscations, abcIndex, ignoredCustom, abc, dependencies, ignorePackage, fullyQualifiedNames, uses, at.object);
            if (at.params != null) {
                for (GraphTargetItem p : at.params) {
                    parseDependenciesFromTypeItem(usedDeobfuscations, abcIndex, ignoredCustom, abc, dependencies, ignorePackage, fullyQualifiedNames, uses, p);
                }
            }
            return;
        }
        if (type instanceof TypeItem) {
            DottedChain full = ((TypeItem) type).fullTypeName;
            if (full == null || full.isEmpty()) {
                return;
            }
            if ("*".equals(full.getLast()) && full.size() <= 1) {
                return;
            }
            if (full.getWithoutLast().equals(InitVectorAVM2Item.VECTOR_PACKAGE)) {
                return;
            }
            Dependency dep = new Dependency(full, DependencyType.EXPRESSION);
            if ((ignorePackage == null || !full.getWithoutLast().equals(ignorePackage)) && !dependencies.contains(dep)) {
                dependencies.add(dep);
            }
        }
    }
}
