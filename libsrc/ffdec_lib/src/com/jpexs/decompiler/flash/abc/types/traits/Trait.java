/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.Dependency;
import com.jpexs.decompiler.flash.exporters.script.DependencyParser;
import com.jpexs.decompiler.flash.exporters.script.DependencyType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.search.MethodId;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
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

    public static final int ATTR_0x8 = 0x8; //unknown

    public static final int TRAIT_SLOT = 0;

    public static final int TRAIT_METHOD = 1;

    public static final int TRAIT_GETTER = 2;

    public static final int TRAIT_SETTER = 3;

    public static final int TRAIT_CLASS = 4;

    public static final int TRAIT_FUNCTION = 5;

    public static final int TRAIT_CONST = 6;

    public boolean deleted = false;

    public void delete(ABC abc, boolean d) {
        deleted = d;
    }

    public final List<Entry<String, Map<String, String>>> getMetaDataTable(Trait parent, ConvertData convertData, ABC abc) {
        List<Entry<String, Map<String, String>>> ret = new ArrayList<>();
        for (int m : metadata) {
            if (m >= 0 && m < abc.metadata_info.size()) {
                String name = abc.constants.getString(abc.metadata_info.get(m).name_index);
                Map<String, String> data = new LinkedHashMap<>();
                for (int i = 0; i < abc.metadata_info.get(m).keys.length; i++) {
                    data.put(abc.constants.getString(abc.metadata_info.get(m).keys[i]),
                            abc.constants.getString(abc.metadata_info.get(m).values[i]));
                }
                ret.add(new SimpleEntry<>(name, data));
            }
        }
        if (Configuration.handleSkinPartsAutomatically.get()) {
            /*
            private static var _skinParts:Object = {"attr":false,"attr2":true};
               =>
            [SkinPart required="false"]
            public var attr;
            [SkinPart required="true"]
            public var attr2;
             */
            if (parent instanceof TraitClass) {
                String thisName = getName(abc).getName(abc.constants, new ArrayList<>(), true, true);
                List<Trait> classTraits = abc.class_info.get(((TraitClass) parent).class_info).static_traits.traits;
                for (Trait t : classTraits) {
                    if (t.kindType == Trait.TRAIT_SLOT) {
                        if ("_skinParts".equals(t.getName(abc).getName(abc.constants, new ArrayList<>(), true, true))) {
                            if (t.getName(abc).getNamespace(abc.constants).kind == Namespace.KIND_PRIVATE) {
                                if (convertData.assignedValues.containsKey(t)) {
                                    if (convertData.assignedValues.get(t).value instanceof NewObjectAVM2Item) {
                                        NewObjectAVM2Item no = (NewObjectAVM2Item) convertData.assignedValues.get(t).value;
                                        for (NameValuePair nvp : no.pairs) {
                                            if (nvp.name instanceof StringAVM2Item) {
                                                if (thisName.equals(((StringAVM2Item) nvp.name).getValue())) {
                                                    String newReq = "" + nvp.value.getResult();
                                                    boolean found = false;
                                                    //if already has SkinPart metadata, change required value only
                                                    for (int i = 0; i < ret.size(); i++) {
                                                        Entry<String, Map<String, String>> e = ret.get(i);
                                                        if ("SkinPart".equals(e.getKey())) {
                                                            e.getValue().put("required", newReq);
                                                            found = true;
                                                            break;
                                                        }
                                                    }

                                                    //add new metadata if not found
                                                    if (!found) {
                                                        Map<String, String> data = new HashMap<>();
                                                        data.put("required", newReq);

                                                        ret.add(new SimpleEntry<>("SkinPart", data));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    protected DottedChain getPackage(ABC abc) {
        return getName(abc).getSimpleNamespaceName(abc.constants);
    }

    public void getDependencies(AbcIndexing abcIndex, int scriptIndex, int classIndex, boolean isStatic, String ignoredCustom, ABC abc, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<String> uses) throws InterruptedException {
        if (ignoredCustom == null) {
            Multiname m = getName(abc);
            int nskind = m.getSimpleNamespaceKind(abc.constants);
            if (nskind == Namespace.KIND_NAMESPACE) {
                ignoredCustom = m.getSimpleNamespaceName(abc.constants).toRawString();
            }
        }
        DependencyParser.parseDependenciesFromMultiname(abcIndex, ignoredCustom, abc, dependencies, getName(abc), ignorePackage, fullyQualifiedNames, DependencyType.NAMESPACE, uses);
        //DependencyParser.parseUsagesFromMultiname(ignoredCustom, abc, dependencies, getName(abc), ignorePackage, fullyQualifiedNames, DependencyType.NAMESPACE);
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

    private void getAllClassTraitNames(List<String> traitNamesInThisScript, AbcIndexing abcIndex, ABC abc, int classIndex, Integer scriptIndex, boolean isParent) {
        boolean publicProtectedOnly = isParent;
        for (Trait it : abc.instance_info.get(classIndex).instance_traits.traits) {
            if (publicProtectedOnly) {
                int nskind = it.getName(abc).getSimpleNamespaceKind(abc.constants);
                if (nskind != Namespace.KIND_PACKAGE && nskind != Namespace.KIND_PROTECTED) {
                    continue;
                }
            }
            traitNamesInThisScript.add(it.getName(abc).getName(abc.constants, new ArrayList<>(), true, true));
        }
        for (Trait ct : abc.class_info.get(classIndex).static_traits.traits) {
            if (publicProtectedOnly) {
                int nskind = ct.getName(abc).getSimpleNamespaceKind(abc.constants);
                if (nskind != Namespace.KIND_PACKAGE && nskind != Namespace.KIND_STATIC_PROTECTED) {
                    continue;
                }
            }
            traitNamesInThisScript.add(ct.getName(abc).getName(abc.constants, new ArrayList<>(), true, true));
        }
        if (abc.instance_info.get(classIndex).super_index == 0) {
            return;
        }
        DottedChain fullClassName = abc.constants.getMultiname(abc.instance_info.get(classIndex).super_index).getNameWithNamespace(abc.constants, true);
        AbcIndexing.ClassIndex ci = abcIndex.findClass(new TypeItem(fullClassName), abc, scriptIndex);
        if (ci != null) {
            getAllClassTraitNames(traitNamesInThisScript, abcIndex, ci.abc, ci.index, ci.scriptIndex, true);
        }
    }

    public void writeImports(AbcIndexing abcIndex, int scriptIndex, int classIndex, boolean isStatic, ABC abc, GraphTextWriter writer, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) throws InterruptedException {

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

        List<String> traitNamesInThisScript = new ArrayList<>();
        for (Trait st : abc.script_info.get(scriptIndex).traits.traits) {
            if (st instanceof TraitClass) {
                getAllClassTraitNames(traitNamesInThisScript, abcIndex, abc, ((TraitClass) st).class_info, scriptIndex, false);
            } else {
                traitNamesInThisScript.add(st.getName(abc).getName(abc.constants, new ArrayList<>(), true, true));
            }
        }

        //imports
        List<Dependency> dependencies = new ArrayList<>();
        String customNs = null;
        Multiname multiname = getName(abc);
        int nskind = multiname.getSimpleNamespaceKind(abc.constants);
        if (nskind == Namespace.KIND_NAMESPACE) {
            customNs = multiname.getSimpleNamespaceName(abc.constants).toRawString();
        }
        List<String> uses = new ArrayList<>();
        getDependencies(abcIndex, scriptIndex, classIndex, isStatic, customNs, abc, dependencies, ignorePackage, new ArrayList<>(), uses);

        List<DottedChain> imports = new ArrayList<>();
        for (Dependency d : dependencies) {
            if (!imports.contains(d.getId())) {
                imports.add(d.getId());
            }
        }

        List<String> importnames = new ArrayList<>();
        importnames.addAll(namesInThisPackage);
        importnames.addAll(traitNamesInThisScript);
        importnames.addAll(Arrays.asList(builtInClasses));

        for (DottedChain imp : imports) {
            if (imp.getLast().equals("*")) {
                if (imp.getWithoutLast().equals(ignorePackage)) {
                    continue;
                }
                Set<String> objectsInPkg = abcIndex.getPackageObjects(imp.getWithoutLast());
                for (String objectName : objectsInPkg) {
                    if (importnames.contains(objectName)) {
                        fullyQualifiedNames.add(DottedChain.parseWithSuffix(objectName));
                    } else {
                        importnames.add(objectName);
                    }
                }
            }
        }

        for (int i = 0; i < imports.size(); i++) {
            DottedChain imp = imports.get(i);
            DottedChain pkg = imp.getWithoutLast();
            String name = imp.getLast();
            if (name.equals("*")) {
                continue;
            }
            DottedChain dAll = pkg.addWithSuffix("*");
            if (imports.contains(dAll)) {
                imports.remove(i);
                i--;
            }
        }

        for (int i = 0; i < imports.size(); i++) {
            DottedChain ipath = imports.get(i);
            String name = ipath.getLast();
            if (ipath.getWithoutLast().equals(ignorePackage)) { //do not check classes from same package, they are imported automatically                
                if (traitNamesInThisScript.contains(name)) {
                    fullyQualifiedNames.add(DottedChain.parseWithSuffix(name));
                }

                imports.remove(i);
                i--;
                continue;
            }

            if (importnames.contains(name)) {
                fullyQualifiedNames.add(DottedChain.parseWithSuffix(name));
            } else {
                importnames.add(name);
            }
        }

        boolean hasImport = false;
        Collections.sort(imports);
        for (DottedChain imp : imports) {
            if (imp.size() > 1) {  //No imports from root package
                writer.appendNoHilight("import ");

                if (imp.size() > 1) {
                    writer.appendNoHilight(imp.getWithoutLast().toPrintableString(true));
                    writer.appendNoHilight(".");
                }
                if ("*".equals(imp.getLast())) {
                    writer.appendNoHilight("*");
                } else {
                    writer.hilightSpecial(IdentifiersDeobfuscation.printIdentifier(true, imp.getLast()), HighlightSpecialType.TYPE_NAME, imp.toRawString());
                }
                writer.appendNoHilight(";").newLine();
                hasImport = true;
            }
        }
        if (hasImport) {
            writer.newLine();
        }

        if (!uses.isEmpty()) {
            for (String u : uses) {
                writer.appendNoHilight("use namespace " + u + ";").newLine();
            }
            writer.newLine();
        }
    }

    public final GraphTextWriter getMetaData(Trait parent, ConvertData convertData, ABC abc, GraphTextWriter writer) {
        List<Entry<String, Map<String, String>>> md = getMetaDataTable(parent, convertData, abc);
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
        getApiVersions(abc, writer);
        return writer;
    }

    public boolean isApiVersioned(ABC abc) {
        return abc.constants.getMultiname(name_index).isApiVersioned(abc.constants);
    }

    public final GraphTextWriter getApiVersions(ABC abc, GraphTextWriter writer) {
        List<Integer> apiVersions = abc.constants.getMultiname(name_index).getApiVersions(abc.constants);
        for (int version : apiVersions) {
            writer.appendNoHilight("[API(\"" + version + "\")]").newLine();
        }
        return writer;
    }

    public final GraphTextWriter getModifiers(ABC abc, boolean isStatic, boolean insideInterface, GraphTextWriter writer, int classIndex) {
        if ((kindFlags & ATTR_Final) > 0) {
            if (!isStatic) {
                writer.appendNoHilight("final ");
            }
        }
        if ((kindFlags & ATTR_Override) > 0) {
            writer.appendNoHilight("override ");
        }
        Multiname m = getName(abc);
        if (m != null) {
            DottedChain dc = abc.findCustomNsOfMultiname(m);
            String nsname = dc != null ? dc.getLast() : null;

            int nskind = m.getSimpleNamespaceKind(abc.constants);

            if (insideInterface) {
                //no namespace identifier
            } else if (nskind == Namespace.KIND_NAMESPACE && nsname == null) {
                writer.append("§§namespace(\"");
                writer.append(Helper.escapeActionScriptString(m.getSimpleNamespaceName(abc.constants).toRawString()));
                writer.append("\") ");
            } else if (nsname != null) {
                String identifier = IdentifiersDeobfuscation.printIdentifier(true, nsname);
                if (identifier != null && !identifier.isEmpty()) {
                    writer.appendNoHilight(identifier).appendNoHilight(" ");
                }
            } else if (nskind != 0) {

                //Traits of private classes inside script have same namespace as the class
                if (nskind == Namespace.KIND_PRIVATE) {
                    Set<Integer> namespaceIdsThis = new HashSet<>();
                    if (m.isApiVersioned(abc.constants)) {
                        NamespaceSet nss = m.getNamespaceSet(abc.constants);
                        for (int n : nss.namespaces) {
                            namespaceIdsThis.add(n);
                        }
                    } else {
                        namespaceIdsThis.add(m.namespace_index);
                    }
                    Set<Integer> namespaceIdsClass = new HashSet<>();

                    Multiname mc = abc.instance_info.get(classIndex).getName(abc.constants);
                    if (mc.isApiVersioned(abc.constants)) {
                        NamespaceSet nss = mc.getNamespaceSet(abc.constants);
                        for (int n : nss.namespaces) {
                            namespaceIdsClass.add(n);
                        }
                    } else {
                        namespaceIdsClass.add(mc.namespace_index);
                    }

                    for (int ns : namespaceIdsThis) {
                        if (namespaceIdsClass.contains(ns)) {
                            nskind = Namespace.KIND_PACKAGE_INTERNAL;
                            break;
                        }
                    }
                }
                String nsPrefix = Namespace.getPrefix(nskind);
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
        return writer;
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public GraphTextWriter toString(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {
        writer.appendNoHilight(abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata));
        return writer;
    }

    public void convert(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {
    }

    public abstract GraphTextWriter convertTraitHeader(ABC abc, GraphTextWriter writer);

    public GraphTextWriter convertCommonHeaderFlags(String traitType, ABC abc, GraphTextWriter writer) {
        writer.appendNoHilight("trait ");
        writer.hilightSpecial(traitType, HighlightSpecialType.TRAIT_TYPE);
        writer.appendNoHilight(" ");
        writer.hilightSpecial(abc.constants.multinameToString(name_index), HighlightSpecialType.TRAIT_NAME);
        if (Configuration.indentAs3PCode.get()) {
            writer.indent();
        }
        if ((kindFlags & ATTR_Final) > 0) {
            writer.newLine();
            writer.append("flag ");
            writer.hilightSpecial("FINAL", HighlightSpecialType.ATTR_FINAL);
        }
        if ((kindFlags & ATTR_Override) > 0) {
            writer.newLine();
            writer.append("flag ");
            writer.hilightSpecial("OVERRIDE", HighlightSpecialType.ATTR_OVERRIDE);
        }
        if ((kindFlags & ATTR_Metadata) > 0) {
            writer.newLine();
            writer.append("flag ");
            writer.hilightSpecial("METADATA", HighlightSpecialType.ATTR_METADATA);
        }
        if ((kindFlags & ATTR_0x8) > 0) {
            writer.newLine();
            writer.append("flag ");
            writer.hilightSpecial("0x8", HighlightSpecialType.ATTR_0x8);
        }
        if ((kindFlags & ATTR_Metadata) > 0) {
            for (int m : metadata) {
                writer.newLine();
                writer.append("metadata ");
                writer.append("\"");
                writer.append(Helper.escapePCodeString(abc.constants.getString(abc.metadata_info.get(m).name_index)));
                writer.append("\"");
                if (Configuration.indentAs3PCode.get()) {
                    writer.indent();
                }
                writer.newLine();
                if (m >= 0 && m < abc.metadata_info.size()) {
                    for (int i = 0; i < abc.metadata_info.get(m).keys.length; i++) {
                        int key = abc.metadata_info.get(m).keys[i];
                        int val = abc.metadata_info.get(m).values[i];
                        writer.append("item ");

                        if (key == 0) {
                            writer.append("null");
                        } else {
                            writer.append("\"");
                            writer.append(Helper.escapePCodeString(abc.constants.getString(key)));
                            writer.append("\"");
                        }

                        writer.append(" ");

                        if (val == 0) {
                            writer.append("null");
                        } else {
                            writer.append("\"");
                            writer.append(Helper.escapePCodeString(abc.constants.getString(val)));
                            writer.append("\"");
                        }
                        writer.newLine();
                    }
                }
                if (Configuration.indentAs3PCode.get()) {
                    writer.unindent();
                }
                writer.append("end ; metadata");
            }
        }
        return writer;
    }

    public GraphTextWriter toStringPackaged(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {
        Multiname name = abc.constants.getMultiname(name_index);
        int nskind = name.getSimpleNamespaceKind(abc.constants);
        if ((nskind == Namespace.KIND_PACKAGE) || (nskind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = name.getSimpleNamespaceName(abc.constants).toPrintableString(true);
            writer.appendNoHilight("package");
            if (!nsname.isEmpty()) {
                writer.appendNoHilight(" " + nsname); //assume not null name
            }
            writer.startBlock();
            toString(abcIndex, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, insideInterface);
            writer.endBlock();
            writer.newLine();
        }
        return writer;
    }

    public void convertPackaged(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {
        Multiname name = abc.constants.getMultiname(name_index);
        int nskind = name.getSimpleNamespaceKind(abc.constants);
        if ((nskind == Namespace.KIND_PACKAGE) || (nskind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = name.getSimpleNamespaceName(abc.constants).toPrintableString(true);
            convert(abcIndex, parent, convertData, path + nsname, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, scopeStack);
        }
    }

    public GraphTextWriter toStringHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {
        toString(null, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, insideInterface);
        return writer;
    }

    public void convertHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        convert(null, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, new ScopeStack());
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
        String objectName = name.getName(abc.constants, null, true, false);
        String namespaceSuffix = name.getNamespaceSuffix();
        return new ClassPath(packageName, objectName, namespaceSuffix); //assume not null name
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

    public boolean isVisible(boolean isStatic, ABC abc) {
        return true;
    }

    public abstract void getMethodInfos(ABC abc, int traitId, int classIndex, List<MethodId> methodInfos);

    public String getKindToStr() {
        String traitKindStr = "";
        switch (kindType) {
            case Trait.TRAIT_CLASS:
                traitKindStr = "class";
                break;
            case Trait.TRAIT_CONST:
                traitKindStr = "const";
                break;
            case Trait.TRAIT_FUNCTION:
                traitKindStr = "function";
                break;
            case Trait.TRAIT_GETTER:
                traitKindStr = "getter";
                break;
            case Trait.TRAIT_METHOD:
                traitKindStr = "method";
                break;
            case Trait.TRAIT_SETTER:
                traitKindStr = "setter";
                break;
            case Trait.TRAIT_SLOT:
                traitKindStr = "slot";
                break;
        }
        return traitKindStr;
    }
}
