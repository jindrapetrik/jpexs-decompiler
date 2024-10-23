/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.NumberContext;
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
import com.jpexs.helpers.Reference;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Trait in ABC file.
 *
 * @author JPEXS
 */
public abstract class Trait implements Cloneable, Serializable {

    /**
     * Goto constructor definition metadata identifier
     */
    public static final String METADATA_CTOR_DEFINITION = "__go_to_ctor_definition_help";

    /**
     * Goto definition metadata identifier
     */
    public static final String METADATA_DEFINITION = "__go_to_definition_help";

    /**
     * Empty metadata array
     */
    private static final int[] EMPTY_METADATA_ARRAY = new int[0];

    /**
     * Name index - index to multiname constant pool
     */
    public int name_index;

    /**
     * Kind type
     */
    public int kindType;

    /**
     * Kind flags
     */
    public int kindFlags;

    /**
     * Metadata
     */
    public int[] metadata = EMPTY_METADATA_ARRAY;

    /**
     * File offset
     */
    public long fileOffset;

    /**
     * Bytes
     */
    public byte[] bytes;

    /**
     * Attribute: Trait is final
     */
    public static final int ATTR_Final = 0x1;

    /**
     * Attribute: Trait has override flag
     */
    public static final int ATTR_Override = 0x2;

    /**
     * Attribute: Trait has metadata
     */
    public static final int ATTR_Metadata = 0x4;

    /**
     * Attribute: Unknown
     */
    public static final int ATTR_0x8 = 0x8; //unknown

    /**
     * Trait kind: Slot
     */
    public static final int TRAIT_SLOT = 0;

    /**
     * Trait kind: Method
     */
    public static final int TRAIT_METHOD = 1;

    /**
     * Trait kind: Getter
     */
    public static final int TRAIT_GETTER = 2;

    /**
     * Trait kind: Setter
     */
    public static final int TRAIT_SETTER = 3;

    /**
     * Trait kind: Class
     */
    public static final int TRAIT_CLASS = 4;

    /**
     * Trait kind: Function
     */
    public static final int TRAIT_FUNCTION = 5;

    /**
     * Trait kind: Const
     */
    public static final int TRAIT_CONST = 6;

    /**
     * Deleted flag
     */
    public boolean deleted = false;

    /**
     * Deletes trait.
     *
     * @param abc ABC
     * @param d Deleted flag
     */
    public void delete(ABC abc, boolean d) {
        deleted = d;
    }

    /**
     * Gets metadata table.
     *
     * @param parent Parent trait
     * @param convertData Convert data
     * @param abc ABC
     * @return Metadata table
     */
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

    /**
     * Gets package.
     *
     * @param abc ABC
     * @return Package
     */
    protected DottedChain getPackage(ABC abc) {
        return getName(abc).getSimpleNamespaceName(abc.constants);
    }

    /**
     * Gets dependencies.
     *
     * @param abcIndex ABC indexing
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static
     * @param customNamespace Custom namespace
     * @param abc ABC
     * @param dependencies Dependencies
     * @param ignorePackage Ignore package
     * @param fullyQualifiedNames Fully qualified names
     * @param uses Uses
     * @throws InterruptedException On interrupt
     */
    public void getDependencies(AbcIndexing abcIndex, int scriptIndex, int classIndex, boolean isStatic, String customNamespace, ABC abc, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<String> uses, Reference<Integer> numberContextRef) throws InterruptedException {
        if (customNamespace == null) {
            Multiname m = getName(abc);
            int nskind = m.getSimpleNamespaceKind(abc.constants);
            if (nskind == Namespace.KIND_NAMESPACE) {
                customNamespace = m.getSimpleNamespaceName(abc.constants).toRawString();
            }
        }
        DependencyParser.parseDependenciesFromMultiname(abcIndex, customNamespace, abc, dependencies, getName(abc), ignorePackage, fullyQualifiedNames, DependencyType.NAMESPACE, uses);
        //DependencyParser.parseUsagesFromMultiname(ignoredCustom, abc, dependencies, getName(abc), ignorePackage, fullyQualifiedNames, DependencyType.NAMESPACE);
    }

    /*
     * List of built-in classes that are not imported by default
     */
    private static final String[] builtInClasses = {"ArgumentError", "arguments", "Array", "Boolean", "Class", "Date", "DefinitionError", "Error", "EvalError", "Function", "int", "JSON", "Math", "Namespace", "Number", "Object", "QName", "RangeError", "ReferenceError", "RegExp", "SecurityError", "String", "SyntaxError", "TypeError", "uint", "URIError", "VerifyError", "XML", "XMLList"};

    /**
     * Checks if class is built-in.
     *
     * @param name Name
     * @return True if class is built-in
     */
    private static boolean isBuiltInClass(String name) {
        for (String g : builtInClasses) {
            if (g.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all class trait names.
     *
     * @param traitNamesInThisScript Trait names in this script
     * @param abcIndex ABC indexing
     * @param abc ABC
     * @param classIndex Class index
     * @param scriptIndex Script index
     * @param isParent Is parent
     */
    private static void getAllClassTraitNames(List<String> traitNamesInThisScript, AbcIndexing abcIndex, ABC abc, int classIndex, Integer scriptIndex, boolean isParent) {
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

    /**
     * Writes imports.
     *
     * @param trait Trait
     * @param methodIndex Method index
     * @param abcIndex ABC indexing
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static
     * @param abc ABC
     * @param writer Writer
     * @param ignorePackage Ignore package
     * @param fullyQualifiedNames Fully qualified names
     * @return True if its not empty
     * @throws InterruptedException On interrupt
     */
    public static boolean writeImports(List<Trait> traits, int methodIndex, AbcIndexing abcIndex, int scriptIndex, int classIndex, boolean isStatic, ABC abc, GraphTextWriter writer, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) throws InterruptedException {

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

        Set<String> traitNamesInThisScriptSet = new LinkedHashSet<>(traitNamesInThisScript);
        traitNamesInThisScript = new ArrayList<>(traitNamesInThisScriptSet);

        //imports
        List<Dependency> dependencies = new ArrayList<>();
        String customNs = null;
        List<String> uses = new ArrayList<>();
        Reference<Integer> numberContextRef = new Reference<>(null);

        for (Trait trait : traits) {
            Multiname multiname = trait.getName(abc);
            int nskind = multiname.getSimpleNamespaceKind(abc.constants);
            if (nskind == Namespace.KIND_NAMESPACE) {
                customNs = multiname.getSimpleNamespaceName(abc.constants).toRawString();
            }
            trait.getDependencies(abcIndex, scriptIndex, classIndex, isStatic, customNs, abc, dependencies, ignorePackage, new ArrayList<>(), uses, numberContextRef);
        }
        if (methodIndex != -1) {
            DependencyParser.parseDependenciesFromMethodInfo(abcIndex, null, scriptIndex, classIndex, isStatic, customNs, abc, methodIndex, dependencies, ignorePackage, fullyQualifiedNames, new ArrayList<>(), uses, numberContextRef);
        }
        List<DottedChain> imports = new ArrayList<>();
        for (Dependency d : dependencies) {
            if (!imports.contains(d.getId())) {
                imports.add(d.getId());
            }
        }
        
        List<String> importedNames = new ArrayList<>();

        importedNames.addAll(Arrays.asList(builtInClasses));
        importedNames.addAll(namesInThisPackage);
        importedNames.addAll(traitNamesInThisScript);

        for (DottedChain imp : imports) {
            if (imp.getLast().equals("*")) {
                if (imp.getWithoutLast().equals(ignorePackage)) {
                    continue;
                }
                importedNames.addAll(abcIndex.getPackageObjects(imp.getWithoutLast()));
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
            DottedChain pkg = ipath.getWithoutLast();

            if (pkg.equals(ignorePackage)) {
                imports.remove(i);
                i--;
            }
        }

        for (int i = 0; i < imports.size(); i++) {
            DottedChain ipath = imports.get(i);
            String name = ipath.getLast();
            importedNames.add(name);
        }

        List<String> uniqueImportedNames = new ArrayList<>();
        for (int i = 0; i < importedNames.size(); i++) {
            String name = importedNames.get(i);
            if (uniqueImportedNames.contains(name)) {
                fullyQualifiedNames.add(DottedChain.parseWithSuffix(name));
            } else {
                uniqueImportedNames.add(name);
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

        boolean hasUse = false;
        if (!uses.isEmpty()) {
            hasUse = true;
            if (hasImport) {
                writer.newLine();
            }
            for (String u : uses) {
                writer.appendNoHilight("use namespace " + u + ";").newLine();
            }
        }
        if (numberContextRef.getVal() != null) {
            if (uses.isEmpty() && hasImport) {
                writer.newLine();
            }
            writer.appendNoHilight("use ");
            NumberContext nc = new NumberContext(numberContextRef.getVal());
            writer.appendNoHilight(NumberContext.usageToName(nc.getUsage()));
            if (nc.getUsage() == NumberContext.USE_NUMBER || nc.getUsage() == NumberContext.USE_DECIMAL) {
                writer.appendNoHilight(", rounding ");
                writer.appendNoHilight(NumberContext.roundingToName(nc.getRounding()));
                if (nc.getPrecision() != 34) {
                    writer.appendNoHilight(", precision ");
                    writer.appendNoHilight(nc.getPrecision());
                }
            }
            writer.appendNoHilight(";");
            writer.newLine();
            hasUse = true;
        }
        if (hasImport || hasUse) {
            writer.newLine();
        }
        return hasImport || hasUse;
    }

    /**
     * Gets metadata.
     *
     * @param parent Parent trait
     * @param convertData Convert data
     * @param abc ABC
     * @param writer Writer
     * @return Writer
     */
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

    /**
     * Checks if trait is API versioned.
     *
     * @param abc ABC
     * @return True if trait is API versioned
     */
    public boolean isApiVersioned(ABC abc) {
        return abc.constants.getMultiname(name_index).isApiVersioned(abc.constants);
    }

    /**
     * Gets API versions.
     *
     * @param abc ABC
     * @param writer Writer
     * @return Writer
     */
    public final GraphTextWriter getApiVersions(ABC abc, GraphTextWriter writer) {
        List<Integer> apiVersions = abc.constants.getMultiname(name_index).getApiVersions(abc.constants);
        for (int version : apiVersions) {
            writer.appendNoHilight("[API(\"" + version + "\")]").newLine();
        }
        return writer;
    }

    /**
     * Gets modifiers.
     *
     * @param abc ABC
     * @param isStatic Is static
     * @param insideInterface Inside interface
     * @param writer Writer
     * @param classIndex Class index
     * @return Writer
     */
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

                    if (classIndex == -1) {
                        nskind = Namespace.KIND_PACKAGE_INTERNAL;
                    } else {
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
                }

                if (!(classIndex == -1 && nskind == Namespace.KIND_PACKAGE_INTERNAL)) {                                    
                    String nsPrefix = Namespace.getPrefix(nskind);
                    if (nsPrefix != null && !nsPrefix.isEmpty()) {
                        writer.appendNoHilight(nsPrefix).appendNoHilight(" ");
                    }
                }
            }
        }
        if (isStatic && classIndex > -1) {
            if ((this instanceof TraitSlotConst) && ((TraitSlotConst) this).isNamespace()) {
                //static is automatic
            } else {
                writer.appendNoHilight("static ");
            }
        }
        return writer;
    }

    /**
     * To string.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "name_index=" + name_index + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    /**
     * To string.
     *
     * @param abc ABC
     * @param fullyQualifiedNames Fully qualified names
     * @return String
     */
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    /**
     * To string.
     *
     * @param abcIndex ABC indexing
     * @param packageName Package name
     * @param parent Parent trait
     * @param convertData Convert data
     * @param path Path
     * @param abc ABC
     * @param isStatic Is static
     * @param exportMode Export mode
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param parallel Parallel
     * @param insideInterface Inside interface
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toString(AbcIndexing abcIndex, DottedChain packageName, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {
        writer.appendNoHilight(abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata));
        return writer;
    }

    /**
     * Converts trait.
     *
     * @param abcIndex ABC indexing
     * @param parent Parent trait
     * @param convertData Convert data
     * @param path Path
     * @param abc ABC
     * @param isStatic Is static
     * @param exportMode Export mode
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param parallel Parallel
     * @param scopeStack Scope stack
     * @throws InterruptedException On interrupt
     */
    public void convert(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {
    }

    /**
     * Converts trait header.
     *
     * @param abc ABC
     * @param writer Writer
     * @return Writer
     */
    public abstract GraphTextWriter convertTraitHeader(ABC abc, GraphTextWriter writer);

    /**
     * Converts common header flags.
     *
     * @param traitType Trait type
     * @param abc ABC
     * @param writer Writer
     * @return Writer
     */
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

    /**
     * ToString conversion including package.
     *
     * @param abcIndex ABC indexing
     * @param parent Parent trait
     * @param convertData Convert data
     * @param path Path
     * @param abc ABC
     * @param isStatic Is static
     * @param exportMode Export mode
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param parallel Parallel
     * @param insideInterface Inside interface
     * @return Writer
     * @throws InterruptedException On interrupt
     */
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
            List<Trait> traits = new ArrayList<>();
            traits.add(this);           
            writeImports(traits, -1, abcIndex, scriptIndex, classIndex, isStatic, abc, writer, getPackage(abc), fullyQualifiedNames);        
            toString(abcIndex, name.getNameWithNamespace(abc.constants, true).getWithoutLast(), parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, insideInterface);
            writer.endBlock();
            writer.newLine();
        }
        return writer;
    }

    /**
     * Converts trait including package.
     *
     * @param abcIndex ABC indexing
     * @param parent Parent trait
     * @param convertData Convert data
     * @param path Path
     * @param abc ABC
     * @param isStatic Is static
     * @param exportMode Export mode
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param parallel Parallel
     * @param scopeStack Scope stack
     * @throws InterruptedException On interrupt
     */
    public void convertPackaged(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {
        Multiname name = abc.constants.getMultiname(name_index);
        int nskind = name.getSimpleNamespaceKind(abc.constants);
        if ((nskind == Namespace.KIND_PACKAGE) || (nskind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = name.getSimpleNamespaceName(abc.constants).toPrintableString(true);
            convert(abcIndex, parent, convertData, path + nsname, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, scopeStack);
        }
    }

    /**
     * ToString of header.
     *
     * @param parent Parent trait
     * @param packageName Package name
     * @param convertData Convert data
     * @param path Path
     * @param abc ABC
     * @param isStatic Is static
     * @param exportMode Export mode
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param parallel Parallel
     * @param insideInterface Inside interface
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toStringHeader(Trait parent, DottedChain packageName, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {
        toString(null, packageName, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, insideInterface);
        return writer;
    }

    /**
     * Converts header.
     *
     * @param parent Parent trait
     * @param convertData Convert data
     * @param path Path
     * @param abc ABC
     * @param isStatic Is static
     * @param exportMode Export mode
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param parallel Parallel
     * @throws InterruptedException On interrupt
     */
    public void convertHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        convert(null, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, new ScopeStack());
    }

    /**
     * Gets name.
     *
     * @param abc ABC
     * @return Name
     */
    public final Multiname getName(ABC abc) {
        if (name_index == 0) {
            return null;
        } else {
            return abc.constants.getMultiname(name_index);
        }
    }

    /**
     * Removes traps - deobfuscation.
     *
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static
     * @param abc ABC
     * @param path Path
     * @return Number of removed traps
     * @throws InterruptedException On interrupt
     */
    public abstract int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException;

    /**
     * Gets class path.
     *
     * @param abc ABC
     * @return Class path
     */
    public final ClassPath getPath(ABC abc) {
        Multiname name = getName(abc);
        Namespace ns = name.getNamespace(abc.constants);
        DottedChain packageName = ns == null ? DottedChain.EMPTY : ns.getName(abc.constants);
        String objectName = name.getName(abc.constants, null, true, false);
        String namespaceSuffix = name.getNamespaceSuffix();
        return new ClassPath(packageName, objectName, namespaceSuffix); //assume not null name
    }

    /**
     * Clones trait.
     *
     * @return Cloned trait
     */
    @Override
    public Trait clone() {
        try {
            Trait ret = (Trait) super.clone();
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }

    /**
     * Checks if trait is visible.
     *
     * @param isStatic Is static
     * @param abc ABC
     * @return True if trait is visible
     */
    public boolean isVisible(boolean isStatic, ABC abc) {
        return true;
    }

    /**
     * Gets method infos.
     *
     * @param abc ABC
     * @param traitId Trait ID
     * @param classIndex Class index
     * @param methodInfos Method infos
     */
    public abstract void getMethodInfos(ABC abc, int traitId, int classIndex, List<MethodId> methodInfos);

    /**
     * Converts kind to string.
     *
     * @return String
     */
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
