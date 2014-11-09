/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.AsTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceIns;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
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
        abc.class_info.get(class_info).deleted = d;
        abc.instance_info.get(class_info).deleted = d;

        abc.class_info.get(class_info).static_traits.delete(abc, d);
        abc.method_info.get(abc.class_info.get(class_info).cinit_index).delete(abc, d);

        abc.instance_info.get(class_info).instance_traits.delete(abc, d);
        abc.method_info.get(abc.instance_info.get(class_info).iinit_index).delete(abc, d);

        int protectedNS = abc.instance_info.get(class_info).protectedNS;
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
    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        return "Class " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " class_info=" + class_info + " metadata=" + Helper.intArrToString(metadata);
    }

    private boolean parseUsagesFromNS(List<ABCContainerTag> abcTags, ABC abc, List<String> imports, List<String> uses, int namespace_index, String ignorePackage, String name) {
        Namespace ns = abc.constants.getNamespace(namespace_index);
        if (name.isEmpty()) {
            name = "*";
        }
        String newimport = ns.getName(abc.constants, false);
        /*if ((ns.kind != Namespace.KIND_PACKAGE)
         && (ns.kind != Namespace.KIND_NAMESPACE)
         && (ns.kind != Namespace.KIND_STATIC_PROTECTED)) {
         return false;
         }*/
        /*if (ns.kind == Namespace.KIND_NAMESPACE)*/ {
            String oldimport = newimport;
            newimport = null;
            for (ABCContainerTag abcTag : abcTags) {
                String newname = abcTag.getABC().nsValueToName(oldimport);
                if (newname.equals("-")) {
                    return true;
                }
                if (!newname.isEmpty()) {
                    newimport = newname;
                    break;
                }
            }
            if (newimport == null) {
                newimport = oldimport;
                newimport += "." + name;
            }
            if (newimport != null && newimport.isEmpty()) {
                newimport = null;
            }
            if (newimport != null) {
                /*                if(ns.kind==Namespace.KIND_PACKAGE){
                 newimport+=".*";
                 }*/

                if (!imports.contains(newimport)) {
                    if (newimport.contains(":")) {
                        return true;
                    }
                    String pkg = "";
                    if (newimport.contains(".")) {
                        pkg = newimport.substring(0, newimport.lastIndexOf('.'));
                    }
                    String usname = newimport;
                    if (usname.contains(".")) {
                        usname = usname.substring(usname.lastIndexOf('.') + 1);
                    }
                    if (ns.kind == Namespace.KIND_PACKAGE) {
                        if (!pkg.equals(ignorePackage)) {
                            if (!pkg.equals("__AS3__.vec")) { //Automatic import
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

    private void parseImportsUsagesFromNS(List<ABCContainerTag> abcTags, ABC abc, List<String> imports, List<String> uses, int namespace_index, String ignorePackage, String name) {
        Namespace ns = abc.constants.getNamespace(namespace_index);
        if (name.isEmpty()) {
            name = "*";
        }
        String newimport = ns.getName(abc.constants, false);
        if (parseUsagesFromNS(abcTags, abc, imports, uses, namespace_index, ignorePackage, name)) {
            return;
        } else if ((ns.kind != Namespace.KIND_PACKAGE) && (ns.kind != Namespace.KIND_PACKAGE_INTERNAL)) {
            return;
        }
        if (newimport == null) {
            newimport = "";
        }
        //if (!newimport.equals("")) {
        newimport += "." + name;
        if (newimport.contains(":")) {
            return;
        }
        if (!imports.contains(newimport)) {
            String pkg = newimport.substring(0, newimport.lastIndexOf('.'));
            if (pkg.equals("__AS3__.vec")) { //special case - is imported always
                return;
            }
            if (!pkg.equals(ignorePackage)) {
                imports.add(newimport);
            }
        }
        //}
    }

    private void parseUsagesFromMultiname(List<ABCContainerTag> abcTags, ABC abc, List<String> imports, List<String> uses, Multiname m, String ignorePackage, List<String> fullyQualifiedNames) {
        if (m != null) {
            if (m.kind == Multiname.TYPENAME) {
                if (m.qname_index != 0) {
                    parseUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(m.qname_index), ignorePackage, fullyQualifiedNames);
                }
                for (Integer i : m.params) {
                    if (i != 0) {
                        parseUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(i), ignorePackage, fullyQualifiedNames);
                    }
                }
                return;
            }
            Namespace ns = m.getNamespace(abc.constants);
            String name = m.getName(abc.constants, fullyQualifiedNames, false);
            NamespaceSet nss = m.getNamespaceSet(abc.constants);
            if (ns != null) {
                parseUsagesFromNS(abcTags, abc, imports, uses, m.namespace_index, ignorePackage, name);
            }
            if (nss != null) {
                if (nss.namespaces.length == 1) {
                    parseUsagesFromNS(abcTags, abc, imports, uses, nss.namespaces[0], ignorePackage, name);
                } else {
                    for (int n : nss.namespaces) {
                        parseUsagesFromNS(abcTags, abc, imports, uses, n, ignorePackage, "");
                    }
                }
            }
        }
    }

    private void parseImportsUsagesFromMultiname(List<ABCContainerTag> abcTags, ABC abc, List<String> imports, List<String> uses, Multiname m, String ignorePackage, List<String> fullyQualifiedNames) {
        if (m != null) {
            if (m.kind == Multiname.TYPENAME) {
                if (m.qname_index != 0) {
                    parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(m.qname_index), ignorePackage, fullyQualifiedNames);
                }
                for (Integer i : m.params) {
                    if (i != 0) {
                        parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(i), ignorePackage, fullyQualifiedNames);
                    }
                }
                return;
            }
            Namespace ns = m.getNamespace(abc.constants);
            String name = m.getName(abc.constants, fullyQualifiedNames, false);
            NamespaceSet nss = m.getNamespaceSet(abc.constants);
            if (ns != null) {
                parseImportsUsagesFromNS(abcTags, abc, imports, uses, m.namespace_index, ignorePackage, name);
            }
            if (nss != null) {
                for (int n : nss.namespaces) {
                    parseImportsUsagesFromNS(abcTags, abc, imports, uses, n, ignorePackage, nss.namespaces.length > 1 ? "" : name);
                }
            }
        }
    }

    private void parseImportsUsagesFromMethodInfo(List<ABCContainerTag> abcTags, ABC abc, int method_index, List<String> imports, List<String> uses, String ignorePackage, List<String> fullyQualifiedNames, List<Integer> visitedMethods) {
        if ((method_index < 0) || (method_index >= abc.method_info.size())) {
            return;
        }
        visitedMethods.add(method_index);
        if (abc.method_info.get(method_index).ret_type != 0) {
            parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(abc.method_info.get(method_index).ret_type), ignorePackage, fullyQualifiedNames);
        }
        for (int t : abc.method_info.get(method_index).param_types) {
            if (t != 0) {
                parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(t), ignorePackage, fullyQualifiedNames);
            }
        }
        MethodBody body = abc.findBody(method_index);
        if (body != null) {
            parseImportsUsagesFromTraits(abcTags, abc, body.traits, imports, uses, ignorePackage, fullyQualifiedNames);
            for (ABCException ex : body.exceptions) {
                parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(ex.type_index), ignorePackage, fullyQualifiedNames);
            }
            for (AVM2Instruction ins : body.getCode().code) {
                if (ins.definition instanceof NewFunctionIns) {
                    if (ins.operands[0] != method_index) {
                        if (!visitedMethods.contains(ins.operands[0])) {
                            parseImportsUsagesFromMethodInfo(abcTags, abc, ins.operands[0], imports, uses, ignorePackage, fullyQualifiedNames, visitedMethods);
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
                        parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(m), ignorePackage, fullyQualifiedNames);
                    }
                } else {
                    for (int k = 0; k < ins.definition.operands.length; k++) {

                        if (ins.definition.operands[k] == AVM2Code.DAT_MULTINAME_INDEX) {
                            int multinameIndex = ins.operands[k];
                            parseUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(multinameIndex), ignorePackage, fullyQualifiedNames);
                        }
                    }
                }
            }
        }
    }

    private void parseImportsUsagesFromTraits(List<ABCContainerTag> abcTags, ABC abc, Traits ts, List<String> imports, List<String> uses, String ignorePackage, List<String> fullyQualifiedNames) {
        for (Trait t : ts.traits) {
            parseImportsUsagesFromTrait(abcTags, abc, t, imports, uses, ignorePackage, fullyQualifiedNames);
        }
    }

    private void parseImportsUsagesFromTrait(List<ABCContainerTag> abcTags, ABC abc, Trait t, List<String> imports, List<String> uses, String ignorePackage, List<String> fullyQualifiedNames) {
        if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
            parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(tm.name_index), ignorePackage, fullyQualifiedNames);
            if (tm.method_info != 0) {
                parseImportsUsagesFromMethodInfo(abcTags, abc, tm.method_info, imports, uses, ignorePackage, fullyQualifiedNames, new ArrayList<Integer>());
            }
        }
        parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, t.getName(abc), ignorePackage, fullyQualifiedNames);
        if (t instanceof TraitSlotConst) {
            TraitSlotConst ts = (TraitSlotConst) t;
            parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(ts.name_index), ignorePackage, fullyQualifiedNames);
            parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(ts.type_index), ignorePackage, fullyQualifiedNames);
        }
    }

    private List<String> getImportsUsages(List<ABCContainerTag> abcTags, ABC abc, List<String> imports, List<String> uses, List<String> fullyQualifiedNames) {
        //constructor

        String packageName = abc.instance_info.get(class_info).getName(abc.constants).getNamespace(abc.constants).getName(abc.constants, false); //assume not null name

        parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(abc.instance_info.get(class_info).name_index), packageName, fullyQualifiedNames);

        if (abc.instance_info.get(class_info).super_index > 0) {
            parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(abc.instance_info.get(class_info).super_index), packageName, fullyQualifiedNames);
        }
        for (int i : abc.instance_info.get(class_info).interfaces) {
            parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.getMultiname(i), packageName, fullyQualifiedNames);
        }

        //static
        parseImportsUsagesFromTraits(abcTags, abc, abc.class_info.get(class_info).static_traits, imports, uses, packageName, fullyQualifiedNames);

        //static initializer
        parseImportsUsagesFromMethodInfo(abcTags, abc, abc.class_info.get(class_info).cinit_index, imports, uses, packageName, fullyQualifiedNames, new ArrayList<Integer>());

        //instance
        parseImportsUsagesFromTraits(abcTags, abc, abc.instance_info.get(class_info).instance_traits, imports, uses, packageName, fullyQualifiedNames);

        //instance initializer
        parseImportsUsagesFromMethodInfo(abcTags, abc, abc.instance_info.get(class_info).iinit_index, imports, uses, packageName, fullyQualifiedNames, new ArrayList<Integer>());
        return imports;
    }

    @Override
    public GraphTextWriter toStringHeader(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
        abc.instance_info.get(class_info).getClassHeaderStr(writer,abc, fullyQualifiedNames,false);
        return writer;
    }

    @Override
    public void convertHeader(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
    }

    @Override
    public GraphTextWriter toString(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<String> fullyQualifiedNames, boolean parallel) throws InterruptedException {

        writer.startClass(class_info);
        String packageName = abc.instance_info.get(class_info).getName(abc.constants).getNamespace(abc.constants).getName(abc.constants, false); //assume not null name
        List<String> namesInThisPackage = new ArrayList<>();
        for (ABCContainerTag tag : abcTags) {
            for (ScriptInfo si : tag.getABC().script_info) {
                for (Trait t : si.traits.traits) {
                    String spath = t.getPath(tag.getABC());
                    String pkg = "";
                    String name = spath;
                    if (spath.contains(".")) {
                        pkg = spath.substring(0, spath.lastIndexOf('.'));
                        name = spath.substring(spath.lastIndexOf('.') + 1);
                    }
                    if (pkg.equals(packageName)) {
                        namesInThisPackage.add(name);
                    }
                }

            }
        }
        //imports
        List<String> imports = new ArrayList<>();
        List<String> uses = new ArrayList<>();
        getImportsUsages(abcTags, abc, imports, uses, new ArrayList<String>());

        fullyQualifiedNames = new ArrayList<>();

        List<String> importnames = new ArrayList<>();
        importnames.addAll(namesInThisPackage);
        for (String ipath : imports) {
            String name = ipath;
            String pkg = "";
            if (name.contains(".")) {
                pkg = name.substring(0, name.lastIndexOf('.'));
                name = name.substring(name.lastIndexOf('.') + 1);
            }
            if (importnames.contains(name) || ((!pkg.isEmpty()) && isBuiltInClass(name))) {
                fullyQualifiedNames.add(name);
            } else {
                importnames.add(name);
            }
        }
        /*List<String> imports2 = new ArrayList<String>();
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
            String imp = imports.get(i);
            String pkg = imp.substring(0, imp.lastIndexOf('.'));
            String name = imp.substring(imp.lastIndexOf('.') + 1);
            if (name.equals("*")) {
                continue;
            }
            if (imports.contains(pkg + ".*")) {
                imports.remove(i);
                i--;
            }
        }

        boolean hasImport = false;
        for (String imp : imports) {
            if (!imp.startsWith(".")) {
                writer.appendNoHilight("import " + imp + ";").newLine();
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

        //class header     
        abc.instance_info.get(class_info).getClassHeaderStr(writer,abc, fullyQualifiedNames,false);
        writer.startBlock();

        int bodyIndex = abc.findBodyIndex(abc.class_info.get(class_info).cinit_index);
        if (bodyIndex != -1) {
            if (!classInitializerIsEmpty) {
                writer.newLine();
                writer.startTrait(abc.class_info.get(class_info).static_traits.traits.size() + abc.instance_info.get(class_info).instance_traits.traits.size() + 1);
                writer.startMethod(abc.class_info.get(class_info).cinit_index);
                writer.appendNoHilight("{").newLine();
                abc.bodies.get(bodyIndex).toString(path +/*packageName +*/ "/" + abc.instance_info.get(class_info).getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false) + ".staticinitializer", exportMode, abc, this, abc.constants, abc.method_info, writer, fullyQualifiedNames);
                writer.appendNoHilight("}").newLine();
                writer.endMethod();
                writer.endTrait();
            }
        } else {
            //"/*classInitializer*/";
        }

        //constructor
        if (!abc.instance_info.get(class_info).isInterface()) {
            String modifier = "";
            Multiname m = abc.constants.getMultiname(abc.instance_info.get(class_info).name_index);
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
            writer.startTrait(abc.class_info.get(class_info).static_traits.traits.size() + abc.instance_info.get(class_info).instance_traits.traits.size());
            writer.startMethod(abc.instance_info.get(class_info).iinit_index);
            writer.appendNoHilight(modifier);
            writer.appendNoHilight("function ");
            writer.appendNoHilight(abc.constants.getMultiname(abc.instance_info.get(class_info).name_index).getName(abc.constants, new ArrayList<String>()/*do not want full names here*/, false));
            writer.appendNoHilight("(");
            bodyIndex = abc.findBodyIndex(abc.instance_info.get(class_info).iinit_index);
            if (bodyIndex != -1) {
                abc.method_info.get(abc.instance_info.get(class_info).iinit_index).getParamStr(writer, abc.constants, abc.bodies.get(bodyIndex), abc, fullyQualifiedNames);
            } else {
                abc.method_info.get(abc.instance_info.get(class_info).iinit_index).getParamStr(writer, abc.constants, null, abc, fullyQualifiedNames);
            }
            writer.appendNoHilight(")").startBlock();
            if (bodyIndex != -1) {
                abc.bodies.get(bodyIndex).toString(path +/*packageName +*/ "/" + abc.instance_info.get(class_info).getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false) + ".initializer", exportMode, abc, this, abc.constants, abc.method_info, writer, fullyQualifiedNames);
            }
            writer.endBlock().newLine();
            writer.endMethod();
            writer.endTrait();
        }

        //static variables,constants & methods
        abc.class_info.get(class_info).static_traits.toString(this, path +/*packageName +*/ "/" + abc.instance_info.get(class_info).getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false), abcTags, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        abc.instance_info.get(class_info).instance_traits.toString(this, path +/*packageName +*/ "/" + abc.instance_info.get(class_info).getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false), abcTags, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        writer.endBlock(); // class
        writer.endClass();
        writer.newLine();
        return writer;
    }

    @Override
    public void convert(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<String> fullyQualifiedNames, boolean parallel) throws InterruptedException {

        fullyQualifiedNames = new ArrayList<>();

        int bodyIndex = abc.findBodyIndex(abc.class_info.get(class_info).cinit_index);
        if (bodyIndex != -1) {
            writer.mark();
            abc.bodies.get(bodyIndex).convert(path +/*packageName +*/ "/" + abc.instance_info.get(class_info).getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false) + ".staticinitializer", exportMode, true, scriptIndex, class_info, abc, this, abc.constants, abc.method_info, new ScopeStack(), true, writer, fullyQualifiedNames, abc.class_info.get(class_info).static_traits, true);
            classInitializerIsEmpty = !writer.getMark();
        }

        //constructor
        if (!abc.instance_info.get(class_info).isInterface()) {
            bodyIndex = abc.findBodyIndex(abc.instance_info.get(class_info).iinit_index);
            if (bodyIndex != -1) {
                abc.bodies.get(bodyIndex).convert(path +/*packageName +*/ "/" + abc.instance_info.get(class_info).getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false) + ".initializer", exportMode, false, scriptIndex, class_info, abc, this, abc.constants, abc.method_info, new ScopeStack(), false, writer, fullyQualifiedNames, abc.instance_info.get(class_info).instance_traits, true);
            }
        }

        //static variables,constants & methods
        abc.class_info.get(class_info).static_traits.convert(this, path +/*packageName +*/ "/" + abc.instance_info.get(class_info).getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false), abcTags, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        abc.instance_info.get(class_info).instance_traits.convert(this, path +/*packageName +*/ "/" + abc.instance_info.get(class_info).getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false), abcTags, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);
    }

    @Override
    public Multiname getName(ABC abc) {
        return abc.constants.getMultiname(abc.instance_info.get(class_info).name_index);
    }

    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException {
        int iInitializer = abc.findBodyIndex(abc.instance_info.get(class_info).iinit_index);
        int ret = 0;
        if (iInitializer != -1) {
            ret += abc.bodies.get(iInitializer).removeTraps(abc.constants, abc, this, scriptIndex, class_info, false, path);
        }
        int sInitializer = abc.findBodyIndex(abc.class_info.get(class_info).cinit_index);
        if (sInitializer != -1) {
            ret += abc.bodies.get(sInitializer).removeTraps(abc.constants, abc, this, scriptIndex, class_info, true, path);
        }
        ret += abc.instance_info.get(class_info).instance_traits.removeTraps(scriptIndex, class_info, false, abc, path);
        ret += abc.class_info.get(class_info).static_traits.removeTraps(scriptIndex, class_info, true, abc, path);
        return ret;
    }
}
