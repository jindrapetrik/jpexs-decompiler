/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
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
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

    public abstract void delete(ABC abc, boolean d);

    public final List<Entry<String, Map<String, String>>> getMetaDataTable(Trait parent, ConvertData convertData, ABC abc) {
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
        return getName(abc).getNamespace(abc.constants).getName(abc.constants);
    }

    public void getDependencies(int scriptIndex, int classIndex, boolean isStatic, String ignoredCustom, ABC abc, List<Dependency> dependencies, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) throws InterruptedException {
        if (ignoredCustom == null) {
            Namespace n = getName(abc).getNamespace(abc.constants);
            if (n.kind == Namespace.KIND_NAMESPACE) {
                ignoredCustom = n.getName(abc.constants).toRawString();
            }
        }
        DependencyParser.parseUsagesFromMultiname(ignoredCustom, abc, dependencies, uses, getName(abc), ignorePackage, fullyQualifiedNames, DependencyType.NAMESPACE);
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

    public void writeImportsUsages(int scriptIndex, int classIndex, boolean isStatic, ABC abc, GraphTextWriter writer, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) throws InterruptedException {

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
        List<Dependency> dependencies = new ArrayList<>();
        List<String> uses = new ArrayList<>();
        String customNs = null;
        Namespace ns = getName(abc).getNamespace(abc.constants);
        if (ns.kind == Namespace.KIND_NAMESPACE) {
            customNs = ns.getName(abc.constants).toRawString();
        }
        getDependencies(scriptIndex, classIndex, isStatic, customNs, abc, dependencies, uses, ignorePackage, new ArrayList<>());

        List<DottedChain> imports = new ArrayList<>();
        for (Dependency d : dependencies) {
            if (!imports.contains(d.getId())) {
                imports.add(d.getId());
            }
        }

        List<String> importnames = new ArrayList<>();
        importnames.addAll(namesInThisPackage);
        importnames.addAll(Arrays.asList(builtInClasses));
        for (int i = 0; i < imports.size(); i++) {
            DottedChain ipath = imports.get(i);
            if (ipath.getWithoutLast().equals(ignorePackage)) { //do not check classes from same package, they are imported automatically
                imports.remove(i);
                i--;
                continue;
            }
            String name = ipath.getLast();
            if (importnames.contains(name)) {
                fullyQualifiedNames.add(DottedChain.parseWithSuffix(name));
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
            DottedChain dAll = pkg.addWithSuffix("*");
            if (imports.contains(dAll)) {
                imports.remove(i);
                i--;
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
                if ("*".equals(imp.getLast())){
                    writer.appendNoHilight("*");
                }else{
                    writer.hilightSpecial(IdentifiersDeobfuscation.printIdentifier(true, imp.getLast()), HighlightSpecialType.TYPE_NAME, imp.toRawString());
                }
                writer.appendNoHilight(";").newLine();
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
        return writer;
    }

    public final GraphTextWriter getModifiers(ABC abc, boolean isStatic, GraphTextWriter writer) {
        if ((kindFlags & ATTR_Override) > 0) {
            writer.appendNoHilight("override ");
        }
        Multiname m = getName(abc);
        if (m != null) {
            DottedChain dc = abc.findCustomNs(m.namespace_index);
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
            writer.newLine();
            for (int m : metadata) {
                writer.append("metadata");
                writer.append("\"");
                writer.append(Helper.escapeActionScriptString(abc.constants.getString(abc.metadata_info.get(m).name_index)));
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

                        writer.append("\"");
                        writer.append(Helper.escapeActionScriptString(abc.constants.getString(key)));
                        writer.append("\"");

                        writer.append(" ");

                        writer.append("\"");
                        writer.append(Helper.escapeActionScriptString(abc.constants.getString(val)));
                        writer.append("\"");
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
}
