/*
 *  Copyright (C) 2010-2016 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.AsTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceIns;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.List;

public class ImportsUsagesParser {

    public static void parseImportsUsagesFromNS(String ignoredCustom, ABC abc, List<DottedChain> imports, List<String> uses, int namespace_index, DottedChain ignorePackage, String name) {
        Namespace ns = abc.constants.getNamespace(namespace_index);
        if (name.isEmpty()) {
            name = "*";
        }
        DottedChain newimport = ns.getName(abc.constants);

        if (parseUsagesFromNS(ignoredCustom, abc, imports, uses, namespace_index, ignorePackage, name)) {
            return;
        } else if ((ns.kind != Namespace.KIND_PACKAGE) && (ns.kind != Namespace.KIND_PACKAGE_INTERNAL)) {
            return;
        }
        newimport = newimport.add(name);
        if (!imports.contains(newimport)) {
            DottedChain pkg = newimport.getWithoutLast(); //.substring(0, newimport.lastIndexOf('.'));
            if (pkg.equals(InitVectorAVM2Item.VECTOR_PACKAGE)) { //special case - is imported always
                return;
            }
            if (!pkg.equals(ignorePackage)) {
                imports.add(newimport);
            }
        }
        //}
    }

    public static void parseImportsUsagesFromMultiname(String ignoredCustom, ABC abc, List<DottedChain> imports, List<String> uses, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
        if (m != null) {
            if (m.kind == Multiname.TYPENAME) {
                if (m.qname_index != 0) {
                    parseImportsUsagesFromMultiname(ignoredCustom, abc, imports, uses, abc.constants.getMultiname(m.qname_index), ignorePackage, fullyQualifiedNames);
                }
                for (Integer i : m.params) {
                    if (i != 0) {
                        parseImportsUsagesFromMultiname(ignoredCustom, abc, imports, uses, abc.constants.getMultiname(i), ignorePackage, fullyQualifiedNames);
                    }
                }
                return;
            }
            Namespace ns = m.getNamespace(abc.constants);
            String name = m.getName(abc.constants, fullyQualifiedNames, true);
            NamespaceSet nss = m.getNamespaceSet(abc.constants);
            if (ns != null) {
                parseImportsUsagesFromNS(ignoredCustom, abc, imports, uses, m.namespace_index, ignorePackage, name);
            }
            if (nss != null) {
                for (int n : nss.namespaces) {
                    parseImportsUsagesFromNS(ignoredCustom, abc, imports, uses, n, ignorePackage, nss.namespaces.length > 1 ? "" : name);
                }
            }
        }
    }

    public static void parseImportsUsagesFromMethodInfo(String ignoredCustom, ABC abc, int method_index, List<DottedChain> imports, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<Integer> visitedMethods) {
        if ((method_index < 0) || (method_index >= abc.method_info.size())) {
            return;
        }
        visitedMethods.add(method_index);
        if (abc.method_info.get(method_index).ret_type != 0) {
            parseImportsUsagesFromMultiname(ignoredCustom, abc, imports, uses, abc.constants.getMultiname(abc.method_info.get(method_index).ret_type), ignorePackage, fullyQualifiedNames);
        }
        for (int t : abc.method_info.get(method_index).param_types) {
            if (t != 0) {
                parseImportsUsagesFromMultiname(ignoredCustom, abc, imports, uses, abc.constants.getMultiname(t), ignorePackage, fullyQualifiedNames);
            }
        }
        MethodBody body = abc.findBody(method_index);
        if (body != null) {
            body.traits.getImportsUsages(ignoredCustom, abc, imports, uses, ignorePackage, fullyQualifiedNames);
            for (ABCException ex : body.exceptions) {
                parseImportsUsagesFromMultiname(ignoredCustom, abc, imports, uses, abc.constants.getMultiname(ex.type_index), ignorePackage, fullyQualifiedNames);
            }
            for (AVM2Instruction ins : body.getCode().code) {
                if (ins.definition instanceof AlchemyTypeIns) {
                    DottedChain nimport = AlchemyTypeIns.ALCHEMY_PACKAGE.add(ins.definition.instructionName);
                    if (!imports.contains(nimport)) {
                        imports.add(nimport);
                    }
                }
                if (ins.definition instanceof NewFunctionIns) {
                    if (ins.operands[0] != method_index) {
                        if (!visitedMethods.contains(ins.operands[0])) {
                            parseImportsUsagesFromMethodInfo(ignoredCustom, abc, ins.operands[0], imports, uses, ignorePackage, fullyQualifiedNames, visitedMethods);
                        }
                    }
                }
                if ((ins.definition instanceof FindPropertyStrictIns)
                        || (ins.definition instanceof FindPropertyIns)
                        || (ins.definition instanceof GetLexIns)
                        || (ins.definition instanceof CoerceIns)
                        || (ins.definition instanceof AsTypeIns)) {
                    int m = ins.operands[0];
                    if (m != 0) {
                        if (m < abc.constants.getMultinameCount()) {
                            parseImportsUsagesFromMultiname(ignoredCustom, abc, imports, uses, abc.constants.getMultiname(m), ignorePackage, fullyQualifiedNames);
                        }
                    }
                } else {
                    for (int k = 0; k < ins.definition.operands.length; k++) {
                        if (ins.definition.operands[k] == AVM2Code.DAT_MULTINAME_INDEX) {
                            int multinameIndex = ins.operands[k];
                            if (multinameIndex < abc.constants.getMultinameCount()) {
                                parseUsagesFromMultiname(ignoredCustom, abc, imports, uses, abc.constants.getMultiname(multinameIndex), ignorePackage, fullyQualifiedNames);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void parseUsagesFromMultiname(String ignoredCustom, ABC abc, List<DottedChain> imports, List<String> uses, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
        if (m != null) {
            if (m.kind == Multiname.TYPENAME) {
                if (m.qname_index != 0) {
                    parseUsagesFromMultiname(ignoredCustom, abc, imports, uses, abc.constants.getMultiname(m.qname_index), ignorePackage, fullyQualifiedNames);
                }
                for (Integer i : m.params) {
                    if (i != 0) {
                        parseUsagesFromMultiname(ignoredCustom, abc, imports, uses, abc.constants.getMultiname(i), ignorePackage, fullyQualifiedNames);
                    }
                }
                return;
            }
            Namespace ns = m.getNamespace(abc.constants);
            String name = m.getName(abc.constants, fullyQualifiedNames, false);
            NamespaceSet nss = m.getNamespaceSet(abc.constants);
            if (ns != null) {
                parseUsagesFromNS(ignoredCustom, abc, imports, uses, m.namespace_index, ignorePackage, name);
            }
            if (nss != null) {
                if (nss.namespaces.length == 1) {
                    parseUsagesFromNS(ignoredCustom, abc, imports, uses, nss.namespaces[0], ignorePackage, name);
                } else {
                    for (int n : nss.namespaces) {
                        parseUsagesFromNS(ignoredCustom, abc, imports, uses, n, ignorePackage, "");
                    }
                }
            }
        }
    }

    private static boolean parseUsagesFromNS(String ignoredCustom, ABC abc, List<DottedChain> imports, List<String> uses, int namespace_index, DottedChain ignorePackage, String name) {
        Namespace ns = abc.constants.getNamespace(namespace_index);

        if (ns.kind == Namespace.KIND_NAMESPACE) {
            String nsVal = ns.getName(abc.constants).toRawString();
            for (ABCContainerTag abcTag : abc.getAbcTags()) {
                DottedChain nsimport = abcTag.getABC().nsValueToName(nsVal);
                if (nsimport.equals(AVM2Deobfuscation.BUILTIN)) {
                    return true; //handled, but import/use not added
                }
                if (!nsimport.isEmpty()) {

                    if (!nsimport.getWithoutLast().equals(ignorePackage) && !imports.contains(nsimport)) {
                        imports.add(nsimport);
                    }
                    if (ignoredCustom != null && nsVal.equals(ignoredCustom)) {
                        return true;
                    }
                    if (!uses.contains(nsimport.getLast())) {
                        uses.add(nsimport.getLast());
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
