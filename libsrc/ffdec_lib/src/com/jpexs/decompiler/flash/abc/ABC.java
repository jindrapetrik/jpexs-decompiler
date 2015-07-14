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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Deobfuscation;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushStringIns;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MetadataInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.abc.usages.ClassNameMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.ConstVarNameMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.ConstVarTypeMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.DefinitionUsage;
import com.jpexs.decompiler.flash.abc.usages.ExtendsMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.ImplementsMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MethodBodyMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MethodNameMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MethodParamsMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MethodReturnTypeMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.TypeNameMultinameUsage;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.utf8.Utf8PrintWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ABC {

    public int major_version = 46;

    public int minor_version = 16;

    public AVM2ConstantPool constants = new AVM2ConstantPool();

    public List<MethodInfo> method_info = new ArrayList<>();

    public List<MetadataInfo> metadata_info = new ArrayList<>();

    public List<InstanceInfo> instance_info = new ArrayList<>();

    public List<ClassInfo> class_info = new ArrayList<>();

    public List<ScriptInfo> script_info = new ArrayList<>();

    public List<MethodBody> bodies = new ArrayList<>();

    private Map<Integer, Integer> bodyIdxFromMethodIdx;

    private long[] stringOffsets;

    public static final int MINORwithDECIMAL = 17;

    protected Set<EventListener> listeners = new HashSet<>();

    private static final Logger logger = Logger.getLogger(ABC.class.getName());

    private AVM2Deobfuscation deobfuscation;

    @Internal
    public ABCContainerTag parentTag;

    /* Map from multiname index of namespace value to namespace name**/
    private Map<String, DottedChain> namespaceMap;

    public ABC(ABCContainerTag tag) {
        this.parentTag = tag;
        this.deobfuscation = null;
        constants.constant_double.add(null);
        constants.constant_int.add(null);
        constants.constant_uint.add(null);
        constants.constant_string.add(null);
        constants.constant_multiname.add(null);
        constants.constant_namespace.add(null);
        constants.constant_namespace_set.add(null);
    }

    public SWF getSwf() {
        return parentTag.getSwf();
    }

    public List<ABCContainerTag> getAbcTags() {
        return getSwf().getAbcList();
    }

    public int addMethodBody(MethodBody body) {
        bodies.add(body);
        bodyIdxFromMethodIdx = null;
        return bodies.size() - 1;
    }

    public int addMethodInfo(MethodInfo mi) {
        method_info.add(mi);
        return method_info.size() - 1;
    }

    public void addEventListener(EventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(EventListener listener) {
        listeners.remove(listener);
    }

    protected void informListeners(String event, Object data) {
        for (EventListener listener : listeners) {
            listener.handleEvent(event, data);
        }
    }

    public int removeTraps() throws InterruptedException {
        int rem = 0;
        for (int s = 0; s < script_info.size(); s++) {
            rem += script_info.get(s).removeTraps(s, this, "");
        }
        return rem;
    }

    public int removeDeadCode() throws InterruptedException {
        int rem = 0;
        for (MethodBody body : bodies) {
            rem += body.removeDeadCode(constants, null/*FIXME*/, method_info.get(body.method_info));
        }
        return rem;
    }

    public void restoreControlFlow() throws InterruptedException {
        for (MethodBody body : bodies) {
            body.restoreControlFlow(constants, null/*FIXME*/, method_info.get(body.method_info));
        }
    }

    public Set<Integer> getNsStringUsages() {
        Set<Integer> ret = new HashSet<>();
        for (int n = 1; n < constants.getNamespaceCount(); n++) {
            ret.add(constants.getNamespace(n).name_index);
        }
        return ret;
    }

    public Set<Integer> getStringUsages() {
        Set<Integer> ret = new HashSet<>();
        for (MethodBody body : bodies) {
            for (AVM2Instruction ins : body.getCode().code) {
                for (int i = 0; i < ins.definition.operands.length; i++) {
                    if (ins.definition.operands[i] == AVM2Code.DAT_STRING_INDEX) {
                        ret.add(ins.operands[i]);
                    }
                }
            }
        }
        return ret;
    }

    private void setStringUsageType(Map<Integer, String> ret, int strIndex, String usageType) {
        if (ret.containsKey(strIndex)) {
            if (!"name".equals(usageType)) {
                if (!ret.get(strIndex).equals(usageType)) {
                    ret.put(strIndex, "name");
                }
            }
        } else {
            ret.put(strIndex, usageType);
        }
    }

    private void getStringUsageTypes(Map<Integer, String> ret, Traits traits, boolean classesOnly) {
        for (Trait t : traits.traits) {
            int strIndex = constants.getMultiname(t.name_index).name_index;
            String usageType = "";
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                getStringUsageTypes(ret, class_info.get(tc.class_info).static_traits, classesOnly);
                getStringUsageTypes(ret, instance_info.get(tc.class_info).instance_traits, classesOnly);

                if (instance_info.get(tc.class_info).name_index != 0) {
                    setStringUsageType(ret, constants.getMultiname(instance_info.get(tc.class_info).name_index).name_index, "class");
                }
                if (instance_info.get(tc.class_info).super_index != 0) {
                    setStringUsageType(ret, constants.getMultiname(instance_info.get(tc.class_info).super_index).name_index, "class");
                }

                usageType = "class";
            }
            if (t instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
                usageType = "method";
                MethodBody body = findBody(tm.method_info);
                if (body != null) {
                    getStringUsageTypes(ret, body.traits, classesOnly);
                }
            }
            if (t instanceof TraitFunction) {
                TraitFunction tf = (TraitFunction) t;
                MethodBody body = findBody(tf.method_info);
                if (body != null) {
                    getStringUsageTypes(ret, body.traits, classesOnly);
                }
                usageType = "function";
            }
            if (t instanceof TraitSlotConst) {
                TraitSlotConst ts = (TraitSlotConst) t;
                if (ts.isVar()) {
                    usageType = "var";
                }
                if (ts.isConst()) {
                    usageType = "const";
                }
            }
            if (usageType.equals("class") || (!classesOnly)) {
                setStringUsageType(ret, strIndex, usageType);
            }
        }
    }

    public void getStringUsageTypes(Map<Integer, String> ret, boolean classesOnly) {
        for (ScriptInfo script : script_info) {
            getStringUsageTypes(ret, script.traits, classesOnly);
        }
    }

    public void renameMultiname(int multinameIndex, String newname) {
        if (multinameIndex <= 0 || multinameIndex >= constants.getMultinameCount()) {
            throw new IllegalArgumentException("Multiname with index " + multinameIndex + " does not exist");
        }
        Set<Integer> stringUsages = getStringUsages();
        Set<Integer> namespaceUsages = getNsStringUsages();
        int strIndex = constants.getMultiname(multinameIndex).name_index;
        if (stringUsages.contains(strIndex) || namespaceUsages.contains(strIndex)) { // name is used elsewhere as string literal
            strIndex = constants.getStringId(newname, true);
            constants.getMultiname(multinameIndex).name_index = strIndex;
        } else {
            constants.setString(strIndex, newname);
        }
    }

    public void deobfuscateIdentifiers(HashMap<String, String> namesMap, RenameType renameType, boolean classesOnly) {
        Set<Integer> stringUsages = getStringUsages();
        Set<Integer> namespaceUsages = getNsStringUsages();
        Map<Integer, String> stringUsageTypes = new HashMap<>();
        informListeners("deobfuscate", "Getting usage types...");
        getStringUsageTypes(stringUsageTypes, classesOnly);
        AVM2Deobfuscation deobfuscation = getDeobfuscation();
        for (int i = 0; i < instance_info.size(); i++) {
            informListeners("deobfuscate", "class " + i + "/" + instance_info.size());
            InstanceInfo insti = instance_info.get(i);
            if (insti.name_index != 0) {
                constants.getMultiname(insti.name_index).name_index = deobfuscation.deobfuscateName(stringUsageTypes, stringUsages, namespaceUsages, namesMap, constants.getMultiname(insti.name_index).name_index, true, renameType);
                if (constants.getMultiname(insti.name_index).namespace_index != 0) {
                    constants.getNamespace(constants.getMultiname(insti.name_index).namespace_index).name_index
                            = deobfuscation.deobfuscatePackageName(stringUsageTypes, stringUsages, namesMap, constants.getNamespace(constants.getMultiname(insti.name_index).namespace_index).name_index, renameType);
                }
            }
            if (insti.super_index != 0) {
                constants.getMultiname(insti.super_index).name_index = deobfuscation.deobfuscateName(stringUsageTypes, stringUsages, namespaceUsages, namesMap, constants.getMultiname(insti.super_index).name_index, true, renameType);
            }
        }
        if (classesOnly) {
            return;
        }
        for (int i = 1; i < constants.getMultinameCount(); i++) {
            informListeners("deobfuscate", "name " + i + "/" + constants.getMultinameCount());
            constants.getMultiname(i).name_index = deobfuscation.deobfuscateName(stringUsageTypes, stringUsages, namespaceUsages, namesMap, constants.getMultiname(i).name_index, false, renameType);
        }
        for (int i = 1; i < constants.getNamespaceCount(); i++) {
            informListeners("deobfuscate", "namespace " + i + "/" + constants.getNamespaceCount());
            if (constants.getNamespace(i).kind != Namespace.KIND_PACKAGE) { // only packages
                continue;
            }
            constants.getNamespace(i).name_index = deobfuscation.deobfuscatePackageName(stringUsageTypes, stringUsages, namesMap, constants.getNamespace(i).name_index, renameType);
        }

        // process reflection using getDefinitionByName too
        for (MethodBody body : bodies) {
            for (int ip = 0; ip < body.getCode().code.size(); ip++) {
                if (body.getCode().code.get(ip).definition instanceof CallPropertyIns) {
                    int mIndex = body.getCode().code.get(ip).operands[0];
                    if (mIndex > 0) {
                        Multiname m = constants.getMultiname(mIndex);
                        if (m.getNameWithNamespace(constants).equals("flash.utils.getDefinitionByName")) {
                            if (ip > 0) {
                                if (body.getCode().code.get(ip - 1).definition instanceof PushStringIns) {
                                    int strIndex = body.getCode().code.get(ip - 1).operands[0];
                                    String fullname = constants.getString(strIndex);
                                    String pkg = "";
                                    String name = fullname;
                                    if (fullname.contains(".")) {
                                        pkg = fullname.substring(0, fullname.lastIndexOf('.'));
                                        name = fullname.substring(fullname.lastIndexOf('.') + 1);
                                    }
                                    if (!pkg.isEmpty()) {
                                        int pkgStrIndex = constants.getStringId(pkg, true);
                                        pkgStrIndex = deobfuscation.deobfuscatePackageName(stringUsageTypes, stringUsages, namesMap, pkgStrIndex, renameType);
                                        pkg = constants.getString(pkgStrIndex);
                                    }
                                    int nameStrIndex = constants.getStringId(name, true);
                                    nameStrIndex = deobfuscation.deobfuscateName(stringUsageTypes, stringUsages, namespaceUsages, namesMap, nameStrIndex, true, renameType);
                                    name = constants.getString(nameStrIndex);
                                    String fullChanged = "";
                                    if (!pkg.isEmpty()) {
                                        fullChanged = pkg + ".";
                                    }
                                    fullChanged += name;
                                    strIndex = constants.getStringId(fullChanged, true);
                                    body.getCode().code.get(ip - 1).operands[0] = strIndex;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public ABC(ABCInputStream ais, SWF swf, ABCContainerTag tag) throws IOException {
        this.parentTag = tag;
        minor_version = ais.readU16("minor_version");
        major_version = ais.readU16("major_version");
        logger.log(Level.FINE, "ABC minor_version: {0}, major_version: {1}", new Object[]{minor_version, major_version});

        constants = new AVM2ConstantPool();
        ais.newDumpLevel("constant_pool", "cpool_info");

        // constant integers
        int constant_int_pool_count = ais.readU30("int_count");
        constants.constant_int = new ArrayList<>(constant_int_pool_count);
        if (constant_int_pool_count > 0) {
            constants.addInt(0);
        }
        if (constant_int_pool_count > 1) {
            ais.newDumpLevel("integers", "integer[]");
            for (int i = 1; i < constant_int_pool_count; i++) { // index 0 not used. Values 1..n-1
                constants.addInt(ais.readS32("int"));
            }
            ais.endDumpLevel();
        }

        // constant unsigned integers
        int constant_uint_pool_count = ais.readU30("uint_count");
        constants.constant_uint = new ArrayList<>(constant_uint_pool_count);
        if (constant_uint_pool_count > 0) {
            constants.addUInt(0);
        }
        if (constant_uint_pool_count > 1) {
            ais.newDumpLevel("uintegers", "uinteger[]");
            for (int i = 1; i < constant_uint_pool_count; i++) { // index 0 not used. Values 1..n-1
                constants.addUInt(ais.readU32("uint"));
            }
            ais.endDumpLevel();
        }

        // constant double
        int constant_double_pool_count = ais.readU30("double_count");
        constants.constant_double = new ArrayList<>(constant_double_pool_count);
        if (constant_double_pool_count > 0) {
            constants.addDouble(0);
        }
        if (constant_double_pool_count > 1) {
            ais.newDumpLevel("doubles", "double[]");
            for (int i = 1; i < constant_double_pool_count; i++) { // index 0 not used. Values 1..n-1
                constants.addDouble(ais.readDouble("double"));
            }
            ais.endDumpLevel();
        }

        // constant decimal
        if (minor_version >= MINORwithDECIMAL) {
            int constant_decimal_pool_count = ais.readU30("decimal_count");
            constants.constant_decimal = new ArrayList<>(constant_decimal_pool_count);
            if (constant_decimal_pool_count > 0) {
                constants.addDecimal(null);
            }
            if (constant_decimal_pool_count > 1) {
                ais.newDumpLevel("decimals", "decimal[]");
                for (int i = 1; i < constant_decimal_pool_count; i++) { // index 0 not used. Values 1..n-1
                    constants.addDecimal(ais.readDecimal("decimal"));
                }
                ais.endDumpLevel();
            }
        } else {
            constants.constant_decimal = new ArrayList<>(0);
        }

        // constant string
        int constant_string_pool_count = ais.readU30("string_count");
        constants.constant_string = new ArrayList<>(constant_string_pool_count);
        stringOffsets = new long[constant_string_pool_count];
        if (constant_string_pool_count > 0) {
            constants.addString("");
        }
        if (constant_string_pool_count > 1) {
            ais.newDumpLevel("strings", "string[]");
            for (int i = 1; i < constant_string_pool_count; i++) { // index 0 not used. Values 1..n-1
                long pos = ais.getPosition();
                constants.addString(ais.readString("string"));
                stringOffsets[i] = pos;
            }
            ais.endDumpLevel();
        }

        // constant namespace
        int constant_namespace_pool_count = ais.readU30("namespace_count");
        constants.constant_namespace = new ArrayList<>(constant_namespace_pool_count);
        if (constant_namespace_pool_count > 0) {
            constants.addNamespace(null);
        }
        if (constant_namespace_pool_count > 1) {
            ais.newDumpLevel("namespaces", "namespace[]");
            for (int i = 1; i < constant_namespace_pool_count; i++) { // index 0 not used. Values 1..n-1
                constants.addNamespace(ais.readNamespace("namespace"));
            }
            ais.endDumpLevel();
        }

        // constant namespace set
        int constant_namespace_set_pool_count = ais.readU30("ns_set_count");
        constants.constant_namespace_set = new ArrayList<>(constant_namespace_set_pool_count);
        if (constant_namespace_set_pool_count > 0) {
            constants.addNamespaceSet(null);
        }
        if (constant_namespace_set_pool_count > 1) {
            ais.newDumpLevel("ns_sets", "ns_set[]");
            for (int i = 1; i < constant_namespace_set_pool_count; i++) { // index 0 not used. Values 1..n-1
                ais.newDumpLevel("ns_set_infos", "ns_set_info[]");
                constants.addNamespaceSet(new NamespaceSet());
                int namespace_count = ais.readU30("count");
                constants.getNamespaceSet(i).namespaces = new int[namespace_count];
                for (int j = 0; j < namespace_count; j++) {
                    constants.getNamespaceSet(i).namespaces[j] = ais.readU30("ns");
                }
                ais.endDumpLevel();
            }
            ais.endDumpLevel();
        }

        // constant multiname
        int constant_multiname_pool_count = ais.readU30("multiname_count");
        constants.constant_multiname = new ArrayList<>(constant_multiname_pool_count);
        if (constant_multiname_pool_count > 0) {
            constants.addMultiname(null);
        }
        if (constant_multiname_pool_count > 1) {
            ais.newDumpLevel("multiname", "multinames[]");
            for (int i = 1; i < constant_multiname_pool_count; i++) { // index 0 not used. Values 1..n-1
                constants.addMultiname(ais.readMultiname("multiname"));
            }
            ais.endDumpLevel();
        }

        ais.endDumpLevel(); // cpool_info

        // method info
        int methods_count = ais.readU30("methods_count");
        method_info = new ArrayList<>(methods_count); // MethodInfo[methods_count];
        for (int i = 0; i < methods_count; i++) {
            method_info.add(ais.readMethodInfo("method"));
        }

        // metadata info
        int metadata_count = ais.readU30("metadata_count");
        metadata_info = new ArrayList<>(metadata_count);
        for (int i = 0; i < metadata_count; i++) {
            int name_index = ais.readU30("name_index");
            int values_count = ais.readU30("values_count");
            int[] keys = new int[values_count];
            for (int v = 0; v < values_count; v++) {
                keys[v] = ais.readU30("key");
            }
            int[] values = new int[values_count];
            for (int v = 0; v < values_count; v++) {
                values[v] = ais.readU30("value");
            }
            metadata_info.add(new MetadataInfo(name_index, keys, values));
        }

        int class_count = ais.readU30("class_count");
        instance_info = new ArrayList<>(class_count);
        for (int i = 0; i < class_count; i++) {
            instance_info.add(ais.readInstanceInfo("instance"));
        }
        class_info = new ArrayList<>(class_count);
        for (int i = 0; i < class_count; i++) {
            ais.newDumpLevel("class", "class_info");
            ClassInfo ci = new ClassInfo(null); // do not create Traits in constructor
            ci.cinit_index = ais.readU30("cinit_index");
            ci.static_traits = ais.readTraits("static_traits");
            class_info.add(ci);
            ais.endDumpLevel();
        }
        int script_count = ais.readU30("script_count");
        script_info = new ArrayList<>(script_count);
        for (int i = 0; i < script_count; i++) {
            ais.newDumpLevel("script", "script_info");
            ScriptInfo si = new ScriptInfo(null); // do not create Traits in constructor
            si.init_index = ais.readU30("init_index");
            si.traits = ais.readTraits("traits");
            script_info.add(si);
            ais.endDumpLevel();
            si.setModified(false);
        }

        int bodies_count = ais.readU30("bodies_count");
        bodies = new ArrayList<>(bodies_count);
        for (int i = 0; i < bodies_count; i++) {
            ais.newDumpLevel("method_body", "method_body_info");
            MethodBody mb = new MethodBody(this, null, null, null); // do not create Traits in constructor
            mb.method_info = ais.readU30("method_info");
            mb.max_stack = ais.readU30("max_stack");
            mb.max_regs = ais.readU30("max_regs");
            mb.init_scope_depth = ais.readU30("init_scope_depth");
            mb.max_scope_depth = ais.readU30("max_scope_depth");
            int code_length = ais.readU30("code_length");
            mb.setCodeBytes(ais.readBytes(code_length, "code"));
            int ex_count = ais.readU30("ex_count");
            mb.exceptions = new ABCException[ex_count];
            for (int j = 0; j < ex_count; j++) {
                ABCException abce = new ABCException();
                abce.start = ais.readU30("start");
                abce.end = ais.readU30("end");
                abce.target = ais.readU30("target");
                abce.type_index = ais.readU30("type_index");
                abce.name_index = ais.readU30("name_index");
                mb.exceptions[j] = abce;
            }
            mb.traits = ais.readTraits("traits");
            bodies.add(mb);
            method_info.get(mb.method_info).setBody(mb);
            ais.endDumpLevel();

            SWFDecompilerPlugin.fireMethodBodyParsed(mb, swf);
        }

        /*for(int i=0;i<script_count;i++){
         MethodBody bod=bodies.get(bodyIdxFromMethodIdx.get(script_info.get(i).init_index));
         GraphTextWriter t=new HighlightedTextWriter(Configuration.getCodeFormatting(),false);
         try {
         bod.toString("script", ScriptExportMode.PCODE,  this, null, constants, method_info, t, new ArrayList<>());
         } catch (InterruptedException ex) {
         Logger.getLogger(ABC.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println(""+t.toString());
         }
         //System.exit(0);*/
        SWFDecompilerPlugin.fireAbcParsed(this, swf);
    }

    public void saveToStream(OutputStream os) throws IOException {
        ABCOutputStream aos = new ABCOutputStream(os);
        aos.writeU16(minor_version);
        aos.writeU16(major_version);

        aos.writeU30(constants.getIntCount());
        for (int i = 1; i < constants.getIntCount(); i++) {
            aos.writeS32(constants.getInt(i));
        }
        aos.writeU30(constants.getUIntCount());
        for (int i = 1; i < constants.getUIntCount(); i++) {
            aos.writeU32(constants.getUInt(i));
        }

        aos.writeU30(constants.getDoubleCount());
        for (int i = 1; i < constants.getDoubleCount(); i++) {
            aos.writeDouble(constants.getDouble(i));
        }

        if (minor_version >= MINORwithDECIMAL) {
            aos.writeU30(constants.getDecimalCount());
            for (int i = 1; i < constants.getDecimalCount(); i++) {
                aos.writeDecimal(constants.getDecimal(i));
            }
        }

        aos.writeU30(constants.getStringCount());
        for (int i = 1; i < constants.getStringCount(); i++) {
            aos.writeString(constants.getString(i));
        }

        aos.writeU30(constants.getNamespaceCount());
        for (int i = 1; i < constants.getNamespaceCount(); i++) {
            aos.writeNamespace(constants.getNamespace(i));
        }

        aos.writeU30(constants.getNamespaceSetCount());
        for (int i = 1; i < constants.getNamespaceSetCount(); i++) {
            aos.writeU30(constants.getNamespaceSet(i).namespaces.length);
            for (int j = 0; j < constants.getNamespaceSet(i).namespaces.length; j++) {
                aos.writeU30(constants.getNamespaceSet(i).namespaces[j]);
            }
        }

        aos.writeU30(constants.getMultinameCount());
        for (int i = 1; i < constants.getMultinameCount(); i++) {
            aos.writeMultiname(constants.getMultiname(i));
        }

        aos.writeU30(method_info.size());
        for (MethodInfo mi : method_info) {
            aos.writeMethodInfo(mi);
        }

        aos.writeU30(metadata_info.size());
        for (MetadataInfo mi : metadata_info) {
            aos.writeU30(mi.name_index);
            aos.writeU30(mi.values.length);
            for (int j = 0; j < mi.values.length; j++) {
                aos.writeU30(mi.keys[j]);
            }
            for (int j = 0; j < mi.values.length; j++) {
                aos.writeU30(mi.values[j]);
            }
        }

        aos.writeU30(class_info.size());
        for (InstanceInfo ii : instance_info) {
            aos.writeInstanceInfo(ii);
        }
        for (ClassInfo ci : class_info) {
            aos.writeU30(ci.cinit_index);
            aos.writeTraits(ci.static_traits);
        }
        aos.writeU30(script_info.size());
        for (ScriptInfo si : script_info) {
            aos.writeU30(si.init_index);
            aos.writeTraits(si.traits);
        }

        aos.writeU30(bodies.size());
        for (MethodBody mb : bodies) {
            aos.writeU30(mb.method_info);
            aos.writeU30(mb.max_stack);
            aos.writeU30(mb.max_regs);
            aos.writeU30(mb.init_scope_depth);
            aos.writeU30(mb.max_scope_depth);
            byte[] codeBytes = mb.getCodeBytes();
            aos.writeU30(codeBytes.length);
            aos.write(codeBytes);
            aos.writeU30(mb.exceptions.length);
            for (int j = 0; j < mb.exceptions.length; j++) {
                aos.writeU30(mb.exceptions[j].start);
                aos.writeU30(mb.exceptions[j].end);
                aos.writeU30(mb.exceptions[j].target);
                aos.writeU30(mb.exceptions[j].type_index);
                aos.writeU30(mb.exceptions[j].name_index);
            }
            aos.writeTraits(mb.traits);
        }
    }

    public MethodBody findBody(int methodInfo) {
        if (methodInfo < 0) {
            return null;
        }
        return method_info.get(methodInfo).getBody();
    }

    public int findBodyIndex(int methodInfo) {
        if (methodInfo == -1) {
            return -1;
        }

        Integer result = getBodyIdxFromMethodIdx().get(methodInfo);
        if (result == null) {
            return -1;
        }

        return result;
    }

    public MethodBody findBodyByClassAndName(String className, String methodName) {
        for (int i = 0; i < instance_info.size(); i++) {
            if (className.equals(constants.getMultiname(instance_info.get(i).name_index).getName(constants, null, true))) {
                for (Trait t : instance_info.get(i).instance_traits.traits) {
                    if (t instanceof TraitMethodGetterSetter) {
                        TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                        if (methodName.equals(t2.getName(this).getName(constants, null, true))) {
                            for (MethodBody body : bodies) {
                                if (body.method_info == t2.method_info) {
                                    return body;
                                }
                            }
                        }
                    }
                }
                //break;
            }
        }
        for (int i = 0; i < class_info.size(); i++) {
            if (className.equals(constants.getMultiname(instance_info.get(i).name_index).getName(constants, null, true))) {
                for (Trait t : class_info.get(i).static_traits.traits) {
                    if (t instanceof TraitMethodGetterSetter) {
                        TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                        if (methodName.equals(t2.getName(this).getName(constants, null, true))) {
                            for (MethodBody body : bodies) {
                                if (body.method_info == t2.method_info) {
                                    return body;
                                }
                            }
                        }
                    }
                }
                //break;
            }
        }

        return null;
    }

    public boolean isStaticTraitId(int classIndex, int traitId) {
        if (traitId < class_info.get(classIndex).static_traits.traits.size()) {
            return true;
        } else if (traitId < class_info.get(classIndex).static_traits.traits.size() + instance_info.get(classIndex).instance_traits.traits.size()) {
            return false;
        } else {
            return true; // Can be class or instance initializer
        }
    }

    public Trait findTraitByTraitId(int classIndex, int traitId) {
        if (classIndex == -1) {
            return null;
        }
        List<Trait> staticTraits = class_info.get(classIndex).static_traits.traits;
        if (traitId < staticTraits.size()) {
            return staticTraits.get(traitId);
        } else {
            List<Trait> instanceTraits = instance_info.get(classIndex).instance_traits.traits;
            if (traitId < staticTraits.size() + instanceTraits.size()) {
                traitId -= staticTraits.size();
                return instanceTraits.get(traitId);
            } else {
                return null; // Can be class or instance initializer
            }
        }
    }

    public int findMethodIdByTraitId(int classIndex, int traitId) {
        if (classIndex == -1) {
            return -1;
        }
        List<Trait> staticTraits = class_info.get(classIndex).static_traits.traits;
        if (traitId < staticTraits.size()) {
            if (staticTraits.get(traitId) instanceof TraitMethodGetterSetter) {
                return ((TraitMethodGetterSetter) staticTraits.get(traitId)).method_info;
            } else {
                return -1;
            }
        } else {
            List<Trait> instanceTraits = instance_info.get(classIndex).instance_traits.traits;
            if (traitId < staticTraits.size() + instanceTraits.size()) {
                traitId -= staticTraits.size();
                if (instanceTraits.get(traitId) instanceof TraitMethodGetterSetter) {
                    return ((TraitMethodGetterSetter) instanceTraits.get(traitId)).method_info;
                } else {
                    return -1;
                }
            } else {
                traitId -= staticTraits.size() + instanceTraits.size();
                if (traitId == 0) {
                    return instance_info.get(classIndex).iinit_index;
                } else if (traitId == 1) {
                    return class_info.get(classIndex).cinit_index;
                } else {
                    return -1;
                }
            }
        }
    }

    private Map<String, DottedChain> getNamespaceMap() {
        if (namespaceMap == null) {
            Map<String, DottedChain> map = new HashMap<>();
            for (ScriptInfo si : script_info) {
                for (Trait t : si.traits.traits) {
                    if (t instanceof TraitSlotConst) {
                        TraitSlotConst s = ((TraitSlotConst) t);
                        if (s.isNamespace()) {
                            String key = constants.getNamespace(s.value_index).getName(constants).toRawString(); // assume not null
                            DottedChain val = constants.getMultiname(s.name_index).getNameWithNamespace(constants);
                            map.put(key, val);
                        }
                    }
                }
            }
            namespaceMap = map;
        }

        return namespaceMap;
    }

    private AVM2Deobfuscation getDeobfuscation() {
        if (deobfuscation == null) {
            deobfuscation = new AVM2Deobfuscation(constants);
        }

        return deobfuscation;
    }

    private Map<Integer, Integer> getBodyIdxFromMethodIdx() {
        if (bodyIdxFromMethodIdx == null) {
            Map<Integer, Integer> map = new HashMap<>(bodies.size());
            for (int i = 0; i < bodies.size(); i++) {
                MethodBody mb = bodies.get(i);
                map.put(mb.method_info, i);
            }

            bodyIdxFromMethodIdx = map;
        }

        return bodyIdxFromMethodIdx;
    }

    public DottedChain nsValueToName(DottedChain value) {
        if (value == null) {
            return null;
        }

        String valueStr = value.toRawString();

        if (getNamespaceMap().containsKey(valueStr)) {
            return getNamespaceMap().get(valueStr);
        } else {
            DottedChain ns = getDeobfuscation().builtInNs(valueStr);
            if (ns == null) {
                return DottedChain.EMPTY;
            } else {
                return ns;
            }
        }
    }

    public List<ScriptPack> getScriptPacks(String packagePrefix, List<ABC> allAbcs) {
        List<ScriptPack> ret = new ArrayList<>();
        for (int i = 0; i < script_info.size(); i++) {
            ret.addAll(script_info.get(i).getPacks(this, i, packagePrefix, allAbcs));
        }
        return ret;
    }

    public void dump(OutputStream os) {
        Utf8PrintWriter output;
        output = new Utf8PrintWriter(os);
        constants.dump(output);
        for (int i = 0; i < method_info.size(); i++) {
            output.println("MethodInfo[" + i + "]:" + method_info.get(i).toString(constants, new ArrayList<>()));
        }
        for (int i = 0; i < metadata_info.size(); i++) {
            output.println("MetadataInfo[" + i + "]:" + metadata_info.get(i).toString(constants));
        }
        for (int i = 0; i < instance_info.size(); i++) {
            output.println("InstanceInfo[" + i + "]:" + instance_info.get(i).toString(this, new ArrayList<>()));
        }
        for (int i = 0; i < class_info.size(); i++) {
            output.println("ClassInfo[" + i + "]:" + class_info.get(i).toString(this, new ArrayList<>()));
        }
        for (int i = 0; i < script_info.size(); i++) {
            output.println("ScriptInfo[" + i + "]:" + script_info.get(i).toString(this, new ArrayList<>()));
        }
        for (int i = 0; i < bodies.size(); i++) {
            output.println("MethodBody[" + i + "]:"); //+ bodies[i].toString(this, constants, method_info));
        }
    }

    private void checkMultinameUsedInMethod(int multinameIndex, int methodInfo, List<MultinameUsage> ret, int classIndex, int traitIndex, boolean isStatic, boolean isInitializer, Traits traits, int parentTraitIndex) {
        for (int p = 0; p < method_info.get(methodInfo).param_types.length; p++) {
            if (method_info.get(methodInfo).param_types[p] == multinameIndex) {
                ret.add(new MethodParamsMultinameUsage(this, multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
                break;
            }
        }
        if (method_info.get(methodInfo).ret_type == multinameIndex) {
            ret.add(new MethodReturnTypeMultinameUsage(this, multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
        }
        MethodBody body = findBody(methodInfo);
        if (body != null) {
            findMultinameUsageInTraits(body.traits, multinameIndex, isStatic, classIndex, ret, traitIndex);
            for (ABCException e : body.exceptions) {
                if ((e.name_index == multinameIndex) || (e.type_index == multinameIndex)) {
                    ret.add(new MethodBodyMultinameUsage(this, multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
                    return;
                }
            }
            for (AVM2Instruction ins : body.getCode().code) {
                for (int o = 0; o < ins.definition.operands.length; o++) {
                    if (ins.definition.operands[o] == AVM2Code.DAT_MULTINAME_INDEX) {
                        if (ins.operands[o] == multinameIndex) {
                            ret.add(new MethodBodyMultinameUsage(this, multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
                            return;
                        }
                    }
                }
            }
        }
    }

    private void findMultinameUsageInTraits(Traits traits, int multinameIndex, boolean isStatic, int classIndex, List<MultinameUsage> ret, int parentTraitIndex) {
        for (int t = 0; t < traits.traits.size(); t++) {
            if (traits.traits.get(t) instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) traits.traits.get(t);
                if (tsc.name_index == multinameIndex) {
                    ret.add(new ConstVarNameMultinameUsage(this, multinameIndex, classIndex, t, isStatic, traits, parentTraitIndex));
                }
                if (tsc.type_index == multinameIndex) {
                    ret.add(new ConstVarTypeMultinameUsage(this, multinameIndex, classIndex, t, isStatic, traits, parentTraitIndex));
                }
            }
            if (traits.traits.get(t) instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) traits.traits.get(t);
                if (tmgs.name_index == multinameIndex) {
                    ret.add(new MethodNameMultinameUsage(this, multinameIndex, classIndex, t, isStatic, false, traits, parentTraitIndex));
                }
                checkMultinameUsedInMethod(multinameIndex, tmgs.method_info, ret, classIndex, t, isStatic, false, traits, parentTraitIndex);
            }
        }
    }

    public List<MultinameUsage> findMultinameDefinition(int multinameIndex) {
        List<MultinameUsage> usages = findMultinameUsage(multinameIndex);
        List<MultinameUsage> ret = new ArrayList<>();
        for (MultinameUsage u : usages) {
            if (u instanceof DefinitionUsage) {
                ret.add(u);
            }
        }
        return ret;
    }

    public List<MultinameUsage> findMultinameUsage(int multinameIndex) {
        List<MultinameUsage> ret = new ArrayList<>();
        if (multinameIndex == 0) {
            return ret;
        }
        for (int c = 0; c < instance_info.size(); c++) {
            if (instance_info.get(c).name_index == multinameIndex) {
                ret.add(new ClassNameMultinameUsage(this, multinameIndex, c));
            }
            if (instance_info.get(c).super_index == multinameIndex) {
                ret.add(new ExtendsMultinameUsage(this, multinameIndex, c));
            }
            for (int i = 0; i < instance_info.get(c).interfaces.length; i++) {
                if (instance_info.get(c).interfaces[i] == multinameIndex) {
                    ret.add(new ImplementsMultinameUsage(this, multinameIndex, c));
                }
            }
            checkMultinameUsedInMethod(multinameIndex, instance_info.get(c).iinit_index, ret, c, 0, false, true, null, -1);
            checkMultinameUsedInMethod(multinameIndex, class_info.get(c).cinit_index, ret, c, 0, true, true, null, -1);
            findMultinameUsageInTraits(instance_info.get(c).instance_traits, multinameIndex, false, c, ret, -1);
            findMultinameUsageInTraits(class_info.get(c).static_traits, multinameIndex, true, c, ret, -1);
        }
        loopm:
        for (int m = 1; m < constants.getMultinameCount(); m++) {
            if (constants.getMultiname(m).kind == Multiname.TYPENAME) {
                if (constants.getMultiname(m).qname_index == multinameIndex) {
                    ret.add(new TypeNameMultinameUsage(this, m));
                    continue;
                }
                for (int mp : constants.getMultiname(m).params) {
                    if (mp == multinameIndex) {
                        ret.add(new TypeNameMultinameUsage(this, m));
                        continue loopm;
                    }
                }
            }
        }
        return ret;
    }

    public int findMethodInfoByName(int classId, String methodName) {
        if (classId > -1) {
            for (Trait t : instance_info.get(classId).instance_traits.traits) {
                if (t instanceof TraitMethodGetterSetter) {
                    if (t.getName(this).getName(constants, null, true).equals(methodName)) {
                        return ((TraitMethodGetterSetter) t).method_info;
                    }
                }
            }
        }
        return -1;
    }

    public int findMethodBodyByName(int classId, String methodName) {
        if (classId > -1) {
            for (Trait t : instance_info.get(classId).instance_traits.traits) {
                if (t instanceof TraitMethodGetterSetter) {
                    if (t.getName(this).getName(constants, null, true).equals(methodName)) {
                        return findBodyIndex(((TraitMethodGetterSetter) t).method_info);
                    }
                }
            }
        }
        return -1;
    }

    public int findMethodBodyByName(String className, String methodName) {
        int classId = findClassByName(className);
        return findMethodBodyByName(classId, methodName);
    }

    public int findClassByName(DottedChain name) {
        String str = name == null ? null : name.toRawString();
        return findClassByName(str);
    }

    public int findClassByName(String name) {
        for (int c = 0; c < instance_info.size(); c++) {
            DottedChain s = constants.getMultiname(instance_info.get(c).name_index).getNameWithNamespace(constants);
            if (name.equals(s.toRawString())) {
                return c;
            }
        }
        return -1;
    }

    public List<ScriptPack> findScriptPacksByPath(String name, List<ABC> allAbcs) {
        List<ScriptPack> ret = new ArrayList<>();
        List<ScriptPack> allPacks = getScriptPacks(null, allAbcs); // todo: honfika: use filter parameter
        if (name.endsWith(".**") || name.equals("**") || name.endsWith(".++") || name.equals("++")) {
            name = name.substring(0, name.length() - 2);

            for (ScriptPack en : allPacks) {
                if (en.getClassPath().toString().startsWith(name)) {
                    ret.add(en);
                }
            }
        } else if (name.endsWith(".*") || name.equals("*") || name.endsWith(".+") || name.equals("+")) {
            name = name.substring(0, name.length() - 1);
            for (ScriptPack en : allPacks) {
                String classPathStr = en.getClassPath().toString();
                if (classPathStr.startsWith(name)) {
                    String rem = name.isEmpty() ? classPathStr : classPathStr.substring(name.length());
                    if (!rem.contains(".")) {
                        ret.add(en);
                    }
                }
            }
        } else {
            ScriptPack p = findScriptPackByPath(name, allAbcs);
            if (p != null) {
                ret.add(p);
            }
        }
        return ret;

    }

    public ScriptPack findScriptPackByPath(String name, List<ABC> allAbcs) {
        List<ScriptPack> packs = getScriptPacks(null, allAbcs);
        for (ScriptPack en : packs) {
            if (en.getClassPath().toString().equals(name)) {
                return en;
            }
        }
        return null;
    }

    private void removeClassFromTraits(Traits traits, int index) {
        for (Trait t : traits.traits) {
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                removeClassFromTraits(instance_info.get(tc.class_info).instance_traits, index);
                removeClassFromTraits(class_info.get(tc.class_info).static_traits, index);
                if (tc.class_info > index) {
                    tc.class_info--;
                }
            }
        }
    }

    public void addClass(ClassInfo ci, InstanceInfo ii, int index) {
        for (MethodBody b : bodies) {
            for (AVM2Instruction ins : b.getCode().code) {
                for (int i = 0; i < ins.definition.operands.length; i++) {
                    if (ins.definition.operands[i] == AVM2Code.DAT_CLASS_INDEX) {
                        if (ins.operands[i] >= index) {
                            ins.operands[i]++;
                        }
                    }
                }
            }
        }
        for (ScriptInfo si : script_info) {
            addClassInTraits(si.traits, index);
        }
        for (MethodBody b : bodies) {
            addClassInTraits(b.traits, index);
        }
        instance_info.add(index, ii);
        class_info.add(index, ci);
    }

    private void addClassInTraits(Traits traits, int index) {
        for (Trait t : traits.traits) {
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                addClassInTraits(instance_info.get(tc.class_info).instance_traits, index);
                addClassInTraits(class_info.get(tc.class_info).static_traits, index);
                if (tc.class_info >= index) {
                    tc.class_info++;
                }
            }
        }
    }

    public void removeClass(int index) {
        for (MethodBody b : bodies) {
            for (AVM2Instruction ins : b.getCode().code) {
                for (int i = 0; i < ins.definition.operands.length; i++) {
                    if (ins.definition.operands[i] == AVM2Code.DAT_CLASS_INDEX) {
                        if (ins.operands[i] > index) {
                            ins.operands[i]--;
                        }
                    }
                }
            }
        }
        for (ScriptInfo si : script_info) {
            removeClassFromTraits(si.traits, index);
        }
        for (MethodBody b : bodies) {
            removeClassFromTraits(b.traits, index);
        }
        instance_info.remove(index);
        class_info.remove(index);
    }

    private void removeMethodFromTraits(Traits traits, int index) {
        for (Trait t : traits.traits) {
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                removeMethodFromTraits(instance_info.get(tc.class_info).instance_traits, index);
                removeMethodFromTraits(class_info.get(tc.class_info).static_traits, index);
            }
            if (t instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
                if (tmgs.method_info > index) {
                    tmgs.method_info--;
                }
            }
            if (t instanceof TraitFunction) {
                TraitFunction tf = (TraitFunction) t;
                if (tf.method_info > index) {
                    tf.method_info--;
                }
            }
        }
    }

    public void removeMethod(int index) {

        int bindex = -1;
        for (int b = 0; b < bodies.size(); b++) {
            if (bodies.get(b).method_info == index) {
                bodies.remove(b);
                bindex = b;
                b--;
            }
        }

        for (MethodBody b : bodies) {
            if (b.method_info > index) {
                b.method_info--;
            }
            for (AVM2Instruction ins : b.getCode().code) {
                for (int i = 0; i < ins.definition.operands.length; i++) {
                    if (ins.definition.operands[i] == AVM2Code.DAT_METHOD_INDEX) {
                        if (ins.operands[i] > index) {
                            ins.operands[i]--;
                        }
                    }
                }
            }
            removeMethodFromTraits(b.traits, index);
        }

        for (int c = 0; c < instance_info.size(); c++) {
            InstanceInfo ii = instance_info.get(c);
            if (ii.iinit_index > index) {
                ii.iinit_index--;
            }
            ClassInfo ci = class_info.get(c);
            if (ci.cinit_index > index) {
                ci.cinit_index--;
            }
        }

        for (ScriptInfo si : script_info) {
            if (si.init_index > index) {
                si.init_index--;
            }
            removeMethodFromTraits(si.traits, index);
        }

        bodyIdxFromMethodIdx = null;

        method_info.remove(index);
    }

    public boolean replaceScriptPack(ScriptPack pack, String as) throws AVM2ParseException, CompilationException, IOException, InterruptedException {
        String scriptName = pack.getPathScriptName() + ".as";
        int oldIndex = pack.scriptIndex;
        int newIndex = script_info.size();
        String documentClass = getSwf().getDocumentClass();
        boolean isDocumentClass = documentClass != null && documentClass.equals(pack.getClassPath().toString());

        boolean isSimple = pack.isSimple;

        ScriptInfo si = script_info.get(oldIndex);
        if (isSimple) {
            si.delete(this, true);
        } else {
            for (int t : pack.traitIndices) {
                si.traits.traits.get(t).delete(this, true);
            }
        }

        int newClassIndex = instance_info.size();
        for (int t : pack.traitIndices) {
            if (si.traits.traits.get(t) instanceof TraitClass) {
                TraitClass tc = (TraitClass) si.traits.traits.get(t);
                newClassIndex = tc.class_info + 1;
            }

        }
        List<ABC> otherAbcs = new ArrayList<>(pack.allABCs);

        otherAbcs.remove(this);
        ActionScript3Parser.compile(as, this, otherAbcs, isDocumentClass, scriptName, newClassIndex);

        if (isSimple) {
            // Move newly added script to its position
            script_info.set(oldIndex, script_info.get(newIndex));
            script_info.remove(newIndex);
        } else {
            script_info.get(newIndex).setModified(true);
            //Note: Is deleting traits safe?
            List<Integer> todel = new ArrayList<>(new TreeSet<>(pack.traitIndices));
            for (int i = todel.size() - 1; i >= 0; i--) {
                si.traits.traits.remove((int) todel.get(i));
            }
        }

        script_info.get(oldIndex).setModified(true);
        pack(); // removes old classes/methods
        ((Tag) parentTag).setModified(true);
        return !isSimple;
    }

    public void pack() {
        for (int c = 0; c < instance_info.size(); c++) {
            if (instance_info.get(c).deleted) {
                removeClass(c);
                c--;
            }
        }
        for (int m = 0; m < method_info.size(); m++) {
            if (method_info.get(m).deleted) {
                removeMethod(m);
                m--;
            }
        }
    }
}
