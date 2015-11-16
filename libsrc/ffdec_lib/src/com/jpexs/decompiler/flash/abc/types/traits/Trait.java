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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
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
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Trait implements Cloneable, Serializable {

    public static final String METADATA_CTOR_DEFINITION = "__go_to_ctor_definition_help";

    public static final String METADATA_DEFINITION = "__go_to_definition_help";

    private static final int[] EMPTY_METADATA_ARRAY = new int[0];

    public int name_index;

    public int kindType;

    public int kindFlags;

    public int[] metadata = EMPTY_METADATA_ARRAY;

    public long fileOffset;

    public byte[] bytes;

    public static final int ATTR_Final = 0x1;

    public static final int ATTR_Override = 0x2;

    public static final int ATTR_Metadata = 0x4;

    public static final int TRAIT_SLOT = 0;

    public static final int TRAIT_METHOD = 1;

    public static final int TRAIT_GETTER = 2;

    public static final int TRAIT_SETTER = 3;

    public static final int TRAIT_CLASS = 4;

    public static final int TRAIT_FUNCTION = 5;

    public static final int TRAIT_CONST = 6;

    public abstract void delete(ABC abc, boolean d);

    public final List<Entry<String, Map<String, String>>> getMetaDataTable(ABC abc) {
        List<Entry<String, Map<String, String>>> ret = new ArrayList<>();
        for (int m : metadata) {
            if (m >= 0 && m < abc.metadata_info.size()) {
                String name = abc.constants.getString(abc.metadata_info.get(m).name_index);
                Map<String, String> data = new HashMap<>();
                for (int i = 0; i < abc.metadata_info.get(m).keys.length; i++) {
                    data.put(abc.constants.getString(abc.metadata_info.get(m).keys[i]),
                            abc.constants.getString(abc.metadata_info.get(m).values[i]));
                }
                ret.add(new SimpleEntry<>(name, data));
            }
        }
        return ret;
    }

    protected void parseImportsUsagesFromMultiname(String ignoredCustom, ABC abc, List<DottedChain> imports, List<String> uses, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
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

    private boolean parseUsagesFromNS(String ignoredCustom, ABC abc, List<DottedChain> imports, List<String> uses, int namespace_index, DottedChain ignorePackage, String name) {
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

    protected void parseImportsUsagesFromNS(String ignoredCustom, ABC abc, List<DottedChain> imports, List<String> uses, int namespace_index, DottedChain ignorePackage, String name) {
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

    protected void parseUsagesFromMultiname(String ignoredCustom, ABC abc, List<DottedChain> imports, List<String> uses, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
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

    protected DottedChain getPackage(ABC abc) {
        return getName(abc).getNamespace(abc.constants).getName(abc.constants);
    }

    public void getImportsUsages(String ignoredCustom, ABC abc, List<DottedChain> imports, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
        if (ignoredCustom == null) {
            Namespace n = getName(abc).getNamespace(abc.constants);
            if (n.kind == Namespace.KIND_NAMESPACE) {
                ignoredCustom = n.getName(abc.constants).toRawString();
            }
        }
        parseUsagesFromMultiname(ignoredCustom, abc, imports, uses, getName(abc), ignorePackage, fullyQualifiedNames);
    }

    private static final String[] builtInClasses = {"ArgumentError", "arguments", "Array", "Boolean", "Class", "Date", "DefinitionError", "Error", "EvalError", "Function", "int", "JSON", "Math", "Namespace", "Number", "Object", "QName", "RangeError", "ReferenceError", "RegExp", "SecurityError", "String", "SyntaxError", "TypeError", "uint", "URIError", "VerifyError", "XML", "XMLList"};

    private static boolean isBuiltInClass(String name) {
        for (String g : builtInClasses) {
            if (g.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void writeImportsUsages(ABC abc, GraphTextWriter writer, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {

        List<String> namesInThisPackage = new ArrayList<>();
        for (ABCContainerTag tag : abc.getAbcTags()) {
            for (ScriptInfo si : tag.getABC().script_info) {
                for (Trait t : si.traits.traits) {
                    ClassPath classPath = t.getPath(tag.getABC());
                    if (classPath.packageStr.equals(ignorePackage)) {
                        namesInThisPackage.add(classPath.className);
                    }
                }
            }
        }

        //imports
        List<DottedChain> imports = new ArrayList<>();
        List<String> uses = new ArrayList<>();
        String customNs = null;
        Namespace ns = getName(abc).getNamespace(abc.constants);
        if (ns.kind == Namespace.KIND_NAMESPACE) {
            customNs = ns.getName(abc.constants).toRawString();
        }
        getImportsUsages(customNs, abc, imports, uses, ignorePackage, new ArrayList<>());

        List<String> importnames = new ArrayList<>();
        importnames.addAll(namesInThisPackage);
        for (int i = 0; i < imports.size(); i++) {
            DottedChain ipath = imports.get(i);
            String name = ipath.getLast();
            if (importnames.contains(name) || isBuiltInClass(name)) {
                imports.remove(i);
                i--;
                fullyQualifiedNames.add(new DottedChain(name));
            } else {
                importnames.add(name);
            }
        }

        for (int i = 0; i < imports.size(); i++) {
            DottedChain imp = imports.get(i);
            DottedChain pkg = imp.getWithoutLast();
            String name = imp.getLast();
            if (name.equals("*")) {
                continue;
            }
            DottedChain dAll = pkg.add("*");
            if (imports.contains(dAll)) {
                imports.remove(i);
                i--;
            }
        }

        boolean hasImport = false;
        for (DottedChain imp : imports) {
            if (imp.size() > 1) {  //No imports from root package
                writer.appendNoHilight("import " + imp.toPrintableString(true) + ";").newLine();
                hasImport = true;
            }
        }
        if (hasImport) {
            writer.newLine();
        }
        for (String us : uses) {
            writer.appendNoHilight("use namespace " + us + ";").newLine();
        }
        if (uses.size() > 0) {
            writer.newLine();
        }
    }

    protected void parseImportsUsagesFromMethodInfo(String ignoredCustom, ABC abc, int method_index, List<DottedChain> imports, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<Integer> visitedMethods) {
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

    public final GraphTextWriter getMetaData(ABC abc, GraphTextWriter writer) {
        List<Entry<String, Map<String, String>>> md = getMetaDataTable(abc);
        for (Entry<String, Map<String, String>> en : md) {
            String name = en.getKey();
            if (METADATA_DEFINITION.equals(name) || METADATA_CTOR_DEFINITION.equals(name)) {
                continue;
            }
            writer.append("[").append(IdentifiersDeobfuscation.printIdentifier(true, name));
            if (!en.getValue().isEmpty()) {
                writer.append("(");
                boolean first = true;
                for (String key : en.getValue().keySet()) {
                    if (!first) {
                        writer.append(",");
                    }
                    first = false;
                    if (key != null && !key.isEmpty()) {
                        writer.append(IdentifiersDeobfuscation.printIdentifier(true, key)).append("=");
                    }
                    writer.append("\"");
                    String val = en.getValue().get(key);
                    writer.append(Helper.escapeActionScriptString(val));
                    writer.append("\"");
                }
                writer.append(")");
            }
            writer.append("]");
            writer.newLine();
        }
        return writer;
    }

    protected final DottedChain findCustomNs(int link_ns_index, ABC abc) {
        String nsname;
        if (link_ns_index <= 0) {
            return null;
        }
        Namespace ns = abc.constants.getNamespace(link_ns_index);
        if (ns.kind != Namespace.KIND_NAMESPACE) {
            return null;
        }
        String name = abc.constants.getString(ns.name_index);
        for (ABCContainerTag abcTag : abc.getAbcTags()) {
            DottedChain dc = abcTag.getABC().nsValueToName(name);
            nsname = dc.getLast();

            if (nsname == null) {
                continue;
            }
            if (!nsname.isEmpty()) {
                return dc;
            }
        }
        return null;
    }

    public final GraphTextWriter getModifiers(ABC abc, boolean isStatic, GraphTextWriter writer) {
        if ((kindFlags & ATTR_Override) > 0) {
            writer.appendNoHilight("override ");
        }
        Multiname m = getName(abc);
        if (m != null) {
            DottedChain dc = findCustomNs(m.namespace_index, abc);
            String nsname = dc != null ? dc.getLast() : null;

            Namespace ns = m.getNamespace(abc.constants);

            if (nsname != null) {
                String identifier = IdentifiersDeobfuscation.printIdentifier(true, nsname);
                if (identifier != null && !identifier.isEmpty()) {
                    writer.appendNoHilight(identifier).appendNoHilight(" ");
                }
            }
            if (ns != null) {
                String nsPrefix = ns.getPrefix(abc);
                if (nsPrefix != null && !nsPrefix.isEmpty()) {
                    writer.appendNoHilight(nsPrefix).appendNoHilight(" ");
                }
            }
        }
        if (isStatic) {
            if ((this instanceof TraitSlotConst) && ((TraitSlotConst) this).isNamespace()) {
                //static is automatic
            } else {
                writer.appendNoHilight("static ");
            }
        }
        if ((kindFlags & ATTR_Final) > 0) {
            if (!isStatic) {
                writer.appendNoHilight("final ");
            }
        }
        return writer;
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public GraphTextWriter toString(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        writer.appendNoHilight(abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata));
        return writer;
    }

    public void convert(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
    }

    public GraphTextWriter toStringPackaged(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        Namespace ns = abc.constants.getMultiname(name_index).getNamespace(abc.constants);
        if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = ns.getName(abc.constants).toPrintableString(true);
            writer.appendNoHilight("package");
            if (!nsname.isEmpty()) {
                writer.appendNoHilight(" " + nsname); //assume not null name
            }
            writer.startBlock();
            toString(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            writer.endBlock();
            writer.newLine();
        }
        return writer;
    }

    public void convertPackaged(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        Namespace ns = abc.constants.getMultiname(name_index).getNamespace(abc.constants);
        if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = ns.getName(abc.constants).toPrintableString(true);
            convert(parent, convertData, path + nsname, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        }
    }

    public GraphTextWriter toStringHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        toString(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        return writer;
    }

    public void convertHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        convert(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
    }

    public final Multiname getName(ABC abc) {
        if (name_index == 0) {
            return null;
        } else {
            return abc.constants.getMultiname(name_index);
        }
    }

    public abstract int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException;

    public final ClassPath getPath(ABC abc) {
        Multiname name = getName(abc);
        Namespace ns = name.getNamespace(abc.constants);
        DottedChain packageName = ns == null ? DottedChain.EMPTY : ns.getName(abc.constants);
        String objectName = name.getName(abc.constants, null, true);
        return new ClassPath(packageName, objectName); //assume not null name
    }

    @Override
    public Trait clone() {
        try {
            Trait ret = (Trait) super.clone();
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }
}
