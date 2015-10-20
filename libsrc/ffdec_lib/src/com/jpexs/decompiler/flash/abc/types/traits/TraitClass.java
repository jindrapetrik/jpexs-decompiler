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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
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
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;

public class TraitClass extends Trait implements TraitWithSlot {

    public int slot_id;

    public int class_info;

    private static final String[] builtInClasses = {"ArgumentError", "arguments", "Array", "Boolean", "Class", "Date", "DefinitionError", "Error", "EvalError", "Function", "int", "JSON", "Math", "Namespace", "Number", "Object", "QName", "RangeError", "ReferenceError", "RegExp", "SecurityError", "String", "SyntaxError", "TypeError", "uint", "URIError", "VerifyError", "XML", "XMLList"};

    private boolean classInitializerIsEmpty;

    @Override
    public void delete(ABC abc, boolean d) {
        ClassInfo classInfo = abc.class_info.get(class_info);
        classInfo.deleted = d;
        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        instanceInfo.deleted = d;

        classInfo.static_traits.delete(abc, d);
        abc.method_info.get(classInfo.cinit_index).delete(abc, d);

        instanceInfo.instance_traits.delete(abc, d);
        abc.method_info.get(instanceInfo.iinit_index).delete(abc, d);

        int protectedNS = instanceInfo.protectedNS;
        if (protectedNS != 0) {
            abc.constants.constant_namespace.get(protectedNS).deleted = d;
        }

        abc.constants.constant_multiname.get(name_index).deleted = d;
    }

    @Override
    public int getSlotIndex() {
        return slot_id;
    }

    private static boolean isBuiltInClass(String name) {
        for (String g : builtInClasses) {
            if (g.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return "Class " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " class_info=" + class_info + " metadata=" + Helper.intArrToString(metadata);
    }

    private boolean parseUsagesFromNS(ABC abc, List<DottedChain> imports, List<String> uses, int namespace_index, DottedChain ignorePackage, String name) {
        Namespace ns = abc.constants.getNamespace(namespace_index);
        if (name.isEmpty()) {
            name = "*";
        }
        boolean raw = ns.kind == Namespace.KIND_NAMESPACE;
        DottedChain newimport = ns.getName(abc.constants);
        /*if ((ns.kind != Namespace.KIND_PACKAGE)
         && (ns.kind != Namespace.KIND_NAMESPACE)
         && (ns.kind != Namespace.KIND_STATIC_PROTECTED)) {
         return false;
         }*/
        /*if (ns.kind == Namespace.KIND_NAMESPACE)*/ {
            DottedChain oldimport = newimport;
            newimport = new DottedChain();
            for (ABCContainerTag abcTag : abc.getAbcTags()) {
                DottedChain newname = abcTag.getABC().nsValueToName(oldimport);
                if (newname.size() == 1 && newname.get(0).equals("-")) {
                    return true;
                }

                if (!newname.isEmpty()) {
                    newimport = newname;
                    break;
                }
            }
            if (newimport.isEmpty()) {
                newimport = oldimport.add(name);
            }

            if (newimport.isEmpty()) {
                /*                if(ns.kind==Namespace.KIND_PACKAGE){
                 newimport+=".*";
                 }*/

                if (!imports.contains(newimport)) {
                    //??
                    /*if (newimport.contains(":")) {
                     return true;
                     }*/
                    DottedChain pkg = newimport.getWithoutLast();
                    String usname = newimport.getLast();
                    if (ns.kind == Namespace.KIND_PACKAGE) {
                        if (!pkg.equals(ignorePackage)) {
                            if (!pkg.equals(InitVectorAVM2Item.VECTOR_PACKAGE)) { //Automatic import
                                imports.add(newimport);
                            }
                        }
                    }
                    if (ns.kind == Namespace.KIND_NAMESPACE) {
                        if (!usname.equals("*")) {
                            /*if (!uses.contains(usname)) {
                             uses.add(usname);
                             }*/
                            if (!pkg.equals(ignorePackage)) {
                                imports.add(newimport);
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void parseImportsUsagesFromNS(ABC abc, List<DottedChain> imports, List<String> uses, int namespace_index, DottedChain ignorePackage, String name) {
        Namespace ns = abc.constants.getNamespace(namespace_index);
        if (name.isEmpty()) {
            name = "*";
        }
        DottedChain newimport = ns.getName(abc.constants);
        if (parseUsagesFromNS(abc, imports, uses, namespace_index, ignorePackage, name)) {
            return;
        } else if ((ns.kind != Namespace.KIND_PACKAGE) && (ns.kind != Namespace.KIND_PACKAGE_INTERNAL)) {
            return;
        }
        newimport = newimport.add(name);
        //WUT?
        /*if (newimport.contains(":")) {
         return;
         }*/
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

    private void parseUsagesFromMultiname(ABC abc, List<DottedChain> imports, List<String> uses, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
        if (m != null) {
            if (m.kind == Multiname.TYPENAME) {
                if (m.qname_index != 0) {
                    parseUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(m.qname_index), ignorePackage, fullyQualifiedNames);
                }
                for (Integer i : m.params) {
                    if (i != 0) {
                        parseUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(i), ignorePackage, fullyQualifiedNames);
                    }
                }
                return;
            }
            Namespace ns = m.getNamespace(abc.constants);
            String name = m.getName(abc.constants, fullyQualifiedNames, false);
            NamespaceSet nss = m.getNamespaceSet(abc.constants);
            if (ns != null) {
                parseUsagesFromNS(abc, imports, uses, m.namespace_index, ignorePackage, name);
            }
            if (nss != null) {
                if (nss.namespaces.length == 1) {
                    parseUsagesFromNS(abc, imports, uses, nss.namespaces[0], ignorePackage, name);
                } else {
                    for (int n : nss.namespaces) {
                        parseUsagesFromNS(abc, imports, uses, n, ignorePackage, "");
                    }
                }
            }
        }
    }

    private void parseImportsUsagesFromMultiname(ABC abc, List<DottedChain> imports, List<String> uses, Multiname m, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
        if (m != null) {
            if (m.kind == Multiname.TYPENAME) {
                if (m.qname_index != 0) {
                    parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(m.qname_index), ignorePackage, fullyQualifiedNames);
                }
                for (Integer i : m.params) {
                    if (i != 0) {
                        parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(i), ignorePackage, fullyQualifiedNames);
                    }
                }
                return;
            }
            Namespace ns = m.getNamespace(abc.constants);
            String name = m.getName(abc.constants, fullyQualifiedNames, true);
            NamespaceSet nss = m.getNamespaceSet(abc.constants);
            if (ns != null) {
                parseImportsUsagesFromNS(abc, imports, uses, m.namespace_index, ignorePackage, name);
            }
            if (nss != null) {
                for (int n : nss.namespaces) {
                    parseImportsUsagesFromNS(abc, imports, uses, n, ignorePackage, nss.namespaces.length > 1 ? "" : name);
                }
            }
        }
    }

    private void parseImportsUsagesFromMethodInfo(ABC abc, int method_index, List<DottedChain> imports, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<Integer> visitedMethods) {
        if ((method_index < 0) || (method_index >= abc.method_info.size())) {
            return;
        }
        visitedMethods.add(method_index);
        if (abc.method_info.get(method_index).ret_type != 0) {
            parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(abc.method_info.get(method_index).ret_type), ignorePackage, fullyQualifiedNames);
        }
        for (int t : abc.method_info.get(method_index).param_types) {
            if (t != 0) {
                parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(t), ignorePackage, fullyQualifiedNames);
            }
        }
        MethodBody body = abc.findBody(method_index);
        if (body != null) {
            parseImportsUsagesFromTraits(abc, body.traits, imports, uses, ignorePackage, fullyQualifiedNames);
            for (ABCException ex : body.exceptions) {
                parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(ex.type_index), ignorePackage, fullyQualifiedNames);
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
                            parseImportsUsagesFromMethodInfo(abc, ins.operands[0], imports, uses, ignorePackage, fullyQualifiedNames, visitedMethods);
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
                        if (m < abc.constants.constant_multiname.size()) {
                            parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(m), ignorePackage, fullyQualifiedNames);
                        }
                    }
                } else {
                    for (int k = 0; k < ins.definition.operands.length; k++) {

                        if (ins.definition.operands[k] == AVM2Code.DAT_MULTINAME_INDEX) {
                            int multinameIndex = ins.operands[k];
                            if (multinameIndex < abc.constants.constant_multiname.size()) {
                                parseUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(multinameIndex), ignorePackage, fullyQualifiedNames);
                            }
                        }
                    }
                }
            }
        }
    }

    private void parseImportsUsagesFromTraits(ABC abc, Traits ts, List<DottedChain> imports, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
        for (Trait t : ts.traits) {
            parseImportsUsagesFromTrait(abc, t, imports, uses, ignorePackage, fullyQualifiedNames);
        }
    }

    private void parseImportsUsagesFromTrait(ABC abc, Trait t, List<DottedChain> imports, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
        if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
            parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(tm.name_index), ignorePackage, fullyQualifiedNames);
            if (tm.method_info != 0) {
                parseImportsUsagesFromMethodInfo(abc, tm.method_info, imports, uses, ignorePackage, fullyQualifiedNames, new ArrayList<>());
            }
        }
        parseImportsUsagesFromMultiname(abc, imports, uses, t.getName(abc), ignorePackage, fullyQualifiedNames);
        if (t instanceof TraitSlotConst) {
            TraitSlotConst ts = (TraitSlotConst) t;
            parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(ts.name_index), ignorePackage, fullyQualifiedNames);
            parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(ts.type_index), ignorePackage, fullyQualifiedNames);
        }
    }

    private List<DottedChain> getImportsUsages(ABC abc, List<DottedChain> imports, List<String> uses, List<DottedChain> fullyQualifiedNames) {
        //constructor

        ClassInfo classInfo = abc.class_info.get(class_info);
        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        DottedChain packageName = instanceInfo.getName(abc.constants).getNamespace(abc.constants).getName(abc.constants); //assume not null name

        parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(instanceInfo.name_index), packageName, fullyQualifiedNames);

        if (instanceInfo.super_index > 0) {
            parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(instanceInfo.super_index), packageName, fullyQualifiedNames);
        }
        for (int i : instanceInfo.interfaces) {
            parseImportsUsagesFromMultiname(abc, imports, uses, abc.constants.getMultiname(i), packageName, fullyQualifiedNames);
        }

        //static
        parseImportsUsagesFromTraits(abc, classInfo.static_traits, imports, uses, packageName, fullyQualifiedNames);

        //static initializer
        parseImportsUsagesFromMethodInfo(abc, classInfo.cinit_index, imports, uses, packageName, fullyQualifiedNames, new ArrayList<>());

        //instance
        parseImportsUsagesFromTraits(abc, instanceInfo.instance_traits, imports, uses, packageName, fullyQualifiedNames);

        //instance initializer
        parseImportsUsagesFromMethodInfo(abc, instanceInfo.iinit_index, imports, uses, packageName, fullyQualifiedNames, new ArrayList<>());
        return imports;
    }

    @Override
    public GraphTextWriter toStringHeader(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) {
        abc.instance_info.get(class_info).getClassHeaderStr(writer, abc, fullyQualifiedNames, false);
        return writer;
    }

    @Override
    public void convertHeader(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) {
    }

    @Override
    public GraphTextWriter toString(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {

        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        Multiname instanceInfoMultiname = instanceInfo.getName(abc.constants);
        String instanceInfoName = instanceInfoMultiname.getName(abc.constants, fullyQualifiedNames, false);
        DottedChain packageName = instanceInfoMultiname.getNamespace(abc.constants).getName(abc.constants); //assume not null name
        List<String> namesInThisPackage = new ArrayList<>();
        for (ABCContainerTag tag : abc.getAbcTags()) {
            for (ScriptInfo si : tag.getABC().script_info) {
                for (Trait t : si.traits.traits) {
                    ClassPath classPath = t.getPath(tag.getABC());
                    if (classPath.packageStr.equals(packageName)) {
                        namesInThisPackage.add(classPath.className);
                    }
                }
            }
        }

        //imports
        List<DottedChain> imports = new ArrayList<>();
        List<String> uses = new ArrayList<>();
        getImportsUsages(abc, imports, uses, new ArrayList<>());

        fullyQualifiedNames = new ArrayList<>();

        List<String> importnames = new ArrayList<>();
        importnames.addAll(namesInThisPackage);
        for (DottedChain ipath : imports) {
            String name = ipath.getLast();
            DottedChain pkg = ipath.getWithoutLast();
            if (importnames.contains(name) || isBuiltInClass(name)) {
                fullyQualifiedNames.add(new DottedChain(name));
            } else {
                importnames.add(name);
            }
        }
        /*List<DottedChain> imports2 = new ArrayList<String>();
         for (String path : imports) {
         String name = path;
         String pkg = "";
         if (name.contains(".")) {
         pkg = name.substring(0, name.lastIndexOf("."));
         name = name.substring(name.lastIndexOf(".") + 1);
         }

         if ((!packageName.equals(pkg)) && (!fullyQualifiedNames.contains(name))) {
         imports2.add(path);
         }
         }
         imports = imports2;*/

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

        writer.startClass(class_info);

        getMetaData(abc, writer);
        //class header
        instanceInfo.getClassHeaderStr(writer, abc, fullyQualifiedNames, false);
        writer.startBlock();

        //static variables & constants
        ClassInfo classInfo = abc.class_info.get(class_info);
        classInfo.static_traits.toString(new Class[]{TraitSlotConst.class}, this, path +/*packageName +*/ "/" + instanceInfoName, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        //static initializer
        int bodyIndex = abc.findBodyIndex(classInfo.cinit_index);
        if (bodyIndex != -1) {
            //Note: There must be trait/method highlight even if the initializer is empty to TraitList in GUI to work correctly
            //TODO: handle this better in GUI(?)
            writer.startTrait(GraphTextWriter.TRAIT_CLASS_INITIALIZER);
            writer.startMethod(classInfo.cinit_index);
            if (!classInitializerIsEmpty) {
                writer.startBlock();
                abc.bodies.get(bodyIndex).toString(path +/*packageName +*/ "/" + instanceInfoName + ".staticinitializer", exportMode, abc, this, abc.constants, abc.method_info, writer, fullyQualifiedNames);
                writer.endBlock();
            } else {
                writer.append(" ");
            }
            writer.endMethod();
            writer.endTrait();
            if (!classInitializerIsEmpty) {
                writer.newLine();
            }
        } else {
            //"/*classInitializer*/";
        }

        //instance variables
        instanceInfo.instance_traits.toString(new Class[]{TraitSlotConst.class}, this, path +/*packageName +*/ "/" + instanceInfoName, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        //instance initializer - constructor
        if (!instanceInfo.isInterface()) {
            String modifier = "";
            Multiname m = abc.constants.getMultiname(instanceInfo.name_index);
            if (m != null) {
                Namespace ns = m.getNamespace(abc.constants);
                if (ns != null) {
                    modifier = ns.getPrefix(abc) + " ";
                    if (modifier.equals(" ")) {
                        modifier = "";
                    }
                    if (modifier.startsWith("private")) { //cannot have private constuctor
                        modifier = "";
                    }
                }
            }

            writer.newLine();
            writer.startTrait(GraphTextWriter.TRAIT_INSTANCE_INITIALIZER);
            writer.startMethod(instanceInfo.iinit_index);
            writer.appendNoHilight(modifier);
            writer.appendNoHilight("function ");
            writer.appendNoHilight(m.getName(abc.constants, null/*do not want full names here*/, false));
            writer.appendNoHilight("(");
            bodyIndex = abc.findBodyIndex(instanceInfo.iinit_index);
            MethodBody body = bodyIndex == -1 ? null : abc.bodies.get(bodyIndex);
            abc.method_info.get(instanceInfo.iinit_index).getParamStr(writer, abc.constants, body, abc, fullyQualifiedNames);
            writer.appendNoHilight(")").startBlock();
            if (body != null) {
                body.toString(path +/*packageName +*/ "/" + instanceInfoName + ".initializer", exportMode, abc, this, abc.constants, abc.method_info, writer, fullyQualifiedNames);
            }

            writer.endBlock().newLine();
            writer.endMethod();
            writer.endTrait();
        }

        //static methods
        classInfo.static_traits.toString(new Class[]{TraitClass.class, TraitFunction.class, TraitMethodGetterSetter.class}, this, path +/*packageName +*/ "/" + instanceInfoName, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        //instance methods
        instanceInfo.instance_traits.toString(new Class[]{TraitClass.class, TraitFunction.class, TraitMethodGetterSetter.class}, this, path +/*packageName +*/ "/" + instanceInfoName, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        writer.endBlock(); // class
        writer.endClass();
        writer.newLine();
        return writer;
    }

    @Override
    public void convert(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {

        fullyQualifiedNames = new ArrayList<>();

        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        String instanceInfoName = instanceInfo.getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false);
        ClassInfo classInfo = abc.class_info.get(class_info);

        //class initializer
        int bodyIndex = abc.findBodyIndex(classInfo.cinit_index);
        if (bodyIndex != -1) {
            writer.mark();
            List<Traits> ts = new ArrayList<>();
            ts.add(classInfo.static_traits);
            abc.bodies.get(bodyIndex).convert(path +/*packageName +*/ "/" + instanceInfoName + ".staticinitializer", exportMode, true, classInfo.cinit_index, scriptIndex, class_info, abc, this, abc.constants, abc.method_info, new ScopeStack(), GraphTextWriter.TRAIT_CLASS_INITIALIZER, writer, fullyQualifiedNames, ts, true);
            classInitializerIsEmpty = !writer.getMark();
        }

        //constructor - instance initializer
        if (!instanceInfo.isInterface()) {
            bodyIndex = abc.findBodyIndex(instanceInfo.iinit_index);
            if (bodyIndex != -1) {
                List<Traits> ts = new ArrayList<>();
                ts.add(instanceInfo.instance_traits);
                abc.bodies.get(bodyIndex).convert(path +/*packageName +*/ "/" + instanceInfoName + ".initializer", exportMode, false, instanceInfo.iinit_index, scriptIndex, class_info, abc, this, abc.constants, abc.method_info, new ScopeStack(), GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, writer, fullyQualifiedNames, ts, true);
            }
        }

        //static variables,constants & methods
        classInfo.static_traits.convert(this, path +/*packageName +*/ "/" + instanceInfoName, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        instanceInfo.instance_traits.convert(this, path +/*packageName +*/ "/" + instanceInfoName, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);
    }

    @Override
    public Multiname getName(ABC abc) {
        return abc.constants.getMultiname(abc.instance_info.get(class_info).name_index);
    }

    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException {
        ClassInfo classInfo = abc.class_info.get(class_info);
        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        int iInitializer = abc.findBodyIndex(instanceInfo.iinit_index);
        int ret = 0;
        if (iInitializer != -1) {
            ret += abc.bodies.get(iInitializer).removeTraps(abc.constants, abc, this, scriptIndex, class_info, false, path);
        }
        int sInitializer = abc.findBodyIndex(classInfo.cinit_index);
        if (sInitializer != -1) {
            ret += abc.bodies.get(sInitializer).removeTraps(abc.constants, abc, this, scriptIndex, class_info, true, path);
        }
        ret += instanceInfo.instance_traits.removeTraps(scriptIndex, class_info, false, abc, path);
        ret += classInfo.static_traits.removeTraps(scriptIndex, class_info, true, abc, path);
        return ret;
    }

    @Override
    public TraitClass clone() {
        TraitClass ret = (TraitClass) super.clone();
        return ret;
    }
}
