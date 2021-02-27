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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.EndOfStreamException;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Deobfuscation;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushStringIns;
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
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.TraitType;
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
import com.jpexs.decompiler.flash.abc.usages.TraitMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.TypeNameMultinameUsage;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecial;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecialType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceException;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerInterface;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.utf8.Utf8PrintWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ABC {

    public ABCVersion version = new ABCVersion(46, 16);

    public AVM2ConstantPool constants = new AVM2ConstantPool();

    public List<MethodInfo> method_info = new ArrayList<>();

    public List<MetadataInfo> metadata_info = new ArrayList<>();

    public List<InstanceInfo> instance_info = new ArrayList<>();

    public List<ClassInfo> class_info = new ArrayList<>();

    public List<ScriptInfo> script_info = new ArrayList<>();

    public List<MethodBody> bodies = new ArrayList<>();

    private ABCMethodIndexing abcMethodIndexing;

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
    }

    public SWF getSwf() {
        return parentTag.getSwf();
    }

    public List<ABCContainerTag> getAbcTags() {
        return getSwf().getAbcList();
    }

    public int addMethodBody(MethodBody body) {
        bodies.add(body);
        abcMethodIndexing = null;
        return bodies.size() - 1;
    }

    public int addMethodInfo(MethodInfo mi) {
        method_info.add(mi);
        return method_info.size() - 1;
    }

    public void deleteClass(int class_info, boolean d) {
        ABC abc = this;
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
            abc.constants.getNamespace(protectedNS).deleted = d;
        }
    }

    /**
     * Gets id of metadata/add metadata
     *
     * @param newMetadata
     * @param add Add if not found?
     * @return New index or -1 if not found (add=false)
     */
    public int getMetadataId(MetadataInfo newMetadata, boolean add) {
        for (int m = 0; m < metadata_info.size(); m++) {
            MetadataInfo metadata = metadata_info.get(m);
            if (metadata.name_index == newMetadata.name_index && Arrays.equals(metadata.keys, newMetadata.keys) && Arrays.equals(metadata.values, newMetadata.values)) {
                return m;
            }
        }
        if (add) {
            int newIndex = metadata_info.size();
            metadata_info.add(newMetadata);
            ((Tag) parentTag).setModified(true);
            return newIndex;
        }
        return -1;
    }

    public TraitMethodGetterSetter addMethod(int classId, String name, boolean isStatic) {
        Multiname multiname = new Multiname();
        multiname.kind = Multiname.QNAME;
        multiname.name_index = constants.getStringId(name, true);
        multiname.namespace_index = constants.getNamespaceId(Namespace.KIND_PACKAGE, "", 0, true);
        int multinameId = constants.getMultinameId(multiname, true);

        MethodInfo methodInfo = new MethodInfo();
        int methodInfoId = addMethodInfo(methodInfo);
        MethodBody methodBody = new MethodBody();
        methodBody.method_info = methodInfoId;
        addMethodBody(methodBody);

        TraitMethodGetterSetter trait = new TraitMethodGetterSetter();
        trait.name_index = multinameId;
        trait.kindType = Trait.TRAIT_METHOD;
        if (isStatic) {
            trait.kindFlags = Trait.ATTR_Final;
        }

        trait.method_info = methodInfoId;
        if (isStatic) {
            ClassInfo classInfo = class_info.get(classId);
            classInfo.static_traits.addTrait(trait);
            trait.disp_id = classInfo.getNextDispId();
        } else {
            InstanceInfo instanceInfo = instance_info.get(classId);
            instanceInfo.instance_traits.addTrait(trait);
        }

        return trait;
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

    public void deobfuscateIdentifiers(HashMap<DottedChain, DottedChain> namesMap, RenameType renameType, boolean classesOnly) {
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
                        if (m.getNameWithNamespace(constants, true).toRawString().equals("flash.utils.getDefinitionByName")) {
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

    public boolean hasDecimalSupport() {
        return version.minor >= MINORwithDECIMAL;
    }

    public void setDecimalSupport(boolean val) {
        if (val) {
            if (version.minor != MINORwithDECIMAL) {
                version.minor = MINORwithDECIMAL;
                ((Tag) parentTag).setModified(true);
            }
        } else if (version.minor == MINORwithDECIMAL) {
            version.minor = MINORwithDECIMAL - 1;
            ((Tag) parentTag).setModified(true);
        }
    }

    private boolean minVersionCheck(int minMajor, int minMinor) {
        return version.compareTo(new ABCVersion(minMajor, minMinor)) >= 0;
    }

    public boolean hasFloatSupport() {
        return minVersionCheck(47, 16);
    }

    public void setFloatSupport(boolean val) {
        if (val) {
            if (version.major < 47) {
                version.major = 47;
                ((Tag) parentTag).setModified(true);
            }
        } else if (version.major > 46) {
            version.major = 46;
            ((Tag) parentTag).setModified(true);
        }
    }

    public boolean hasExceptionSupport() {
        return version.compareTo(new ABCVersion(46, 15)) > 0;
    }

    public ABC(ABCInputStream ais, SWF swf, ABCContainerTag tag) throws IOException {
        this.parentTag = tag;
        int minor_version = ais.readU16("minor_version");
        int major_version = ais.readU16("major_version");
        version = new ABCVersion(major_version, minor_version);
        logger.log(Level.FINE, "ABC minor_version: {0}, major_version: {1}", new Object[]{minor_version, major_version});

        ais.newDumpLevel("constant_pool", "cpool_info");

        // constant integers
        int constant_int_pool_count = ais.readU30("int_count");
        constants.ensureIntCapacity(constant_int_pool_count);
        if (constant_int_pool_count > 1) {
            ais.newDumpLevel("integers", "integer[]");
            for (int i = 1; i < constant_int_pool_count; i++) { // index 0 not used. Values 1..n-1
                constants.addInt(ais.readS32("int"));
            }
            ais.endDumpLevel();
        }

        // constant unsigned integers
        int constant_uint_pool_count = ais.readU30("uint_count");
        constants.ensureUIntCapacity(constant_uint_pool_count);
        if (constant_uint_pool_count > 1) {
            ais.newDumpLevel("uintegers", "uinteger[]");
            for (int i = 1; i < constant_uint_pool_count; i++) { // index 0 not used. Values 1..n-1
                constants.addUInt(ais.readU32("uint"));
            }
            ais.endDumpLevel();
        }

        // constant double
        int constant_double_pool_count = ais.readU30("double_count");
        constants.ensureDoubleCapacity(constant_double_pool_count);
        if (constant_double_pool_count > 1) {
            ais.newDumpLevel("doubles", "double[]");
            for (int i = 1; i < constant_double_pool_count; i++) { // index 0 not used. Values 1..n-1
                constants.addDouble(ais.readDouble("double"));
            }
            ais.endDumpLevel();
        }

        // constant decimal
        if (hasDecimalSupport()) {
            int constant_decimal_pool_count = ais.readU30("decimal_count");
            constants.ensureDecimalCapacity(constant_decimal_pool_count);
            if (constant_decimal_pool_count > 1) {
                ais.newDumpLevel("decimals", "decimal[]");
                for (int i = 1; i < constant_decimal_pool_count; i++) { // index 0 not used. Values 1..n-1
                    constants.addDecimal(ais.readDecimal("decimal"));
                }
                ais.endDumpLevel();
            }
        }

        if (hasFloatSupport()) {
            // constant float
            int constant_float_pool_count = ais.readU30("float_count");
            if (constant_float_pool_count > 1) {
                ais.newDumpLevel("floats", "float[]");
                for (int i = 1; i < constant_float_pool_count; i++) { // index 0 not used. Values 1..n-1
                    constants.addFloat(ais.readFloat("float"));
                }
                ais.endDumpLevel();
            }
            // constant float4
            int constant_float4_pool_count = ais.readU30("float4_count");
            if (constant_float4_pool_count > 1) {
                ais.newDumpLevel("floats4", "float4[]");
                for (int i = 1; i < constant_float4_pool_count; i++) { // index 0 not used. Values 1..n-1
                    constants.addFloat4(ais.readFloat4("float4"));
                }
                ais.endDumpLevel();
            }
        }

        // constant string
        int constant_string_pool_count = ais.readU30("string_count");
        constants.ensureStringCapacity(constant_string_pool_count);
        if (constant_string_pool_count > 1) {
            ais.newDumpLevel("strings", "string[]");
            for (int i = 1; i < constant_string_pool_count; i++) { // index 0 not used. Values 1..n-1
                long pos = ais.getPosition();
                constants.addString(ais.readString("string"));
            }
            ais.endDumpLevel();
        }

        // constant namespace
        int constant_namespace_pool_count = ais.readU30("namespace_count");
        constants.ensureNamespaceCapacity(constant_namespace_pool_count);
        if (constant_namespace_pool_count > 1) {
            ais.newDumpLevel("namespaces", "namespace[]");
            for (int i = 1; i < constant_namespace_pool_count; i++) { // index 0 not used. Values 1..n-1
                constants.addNamespace(ais.readNamespace("namespace"));
            }
            ais.endDumpLevel();
        }

        // constant namespace set
        int constant_namespace_set_pool_count = ais.readU30("ns_set_count");
        constants.ensureNamespaceSetCapacity(constant_namespace_set_pool_count);
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
        constants.ensureMultinameCapacity(constant_multiname_pool_count);
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
            DumpInfo di = ais.dumpInfo;
            DumpInfoSpecial dis = (DumpInfoSpecial) ais.newDumpLevel("method_body", "method_body_info", DumpInfoSpecialType.ABC_METHOD_BODY);
            MethodBody mb = new MethodBody(this, null, null, null); // do not create Traits in constructor
            try {
                mb.method_info = ais.readU30("method_info");
                if (dis != null) {
                    dis.specialValue = mb.method_info;
                }

                mb.max_stack = ais.readU30("max_stack");
                mb.max_regs = ais.readU30("max_regs");
                mb.init_scope_depth = ais.readU30("init_scope_depth");
                mb.max_scope_depth = ais.readU30("max_scope_depth");
                int code_length = ais.readU30("code_length");
                mb.setCodeBytes(ais.readBytes(code_length, "code", DumpInfoSpecialType.ABC_CODE));
                int ex_count = ais.readU30("ex_count");
                mb.exceptions = new ABCException[ex_count];
                for (int j = 0; j < ex_count; j++) {
                    ABCException abce = new ABCException();
                    abce.start = ais.readU30("start");
                    abce.end = ais.readU30("end");
                    abce.target = ais.readU30("target");
                    abce.type_index = ais.readU30("type_index");
                    if (hasExceptionSupport()) {
                        abce.name_index = ais.readU30("name_index");
                    } else {
                        abce.name_index = 0;
                    }
                    mb.exceptions[j] = abce;
                }
                mb.traits = ais.readTraits("traits");
                bodies.add(mb);
                ais.endDumpLevel();
            } catch (EndOfStreamException ex) {
                logger.log(Level.SEVERE, "MethodBody reading: End of stream", ex);
                ais.endDumpLevelUntil(di);
                break;
            }

            SWFDecompilerPlugin.fireMethodBodyParsed(this, mb, swf);
        }

        //this will read all method body codes. TODO: make this ondemand
        refreshMultinameNamespaceSuffixes();
        getMethodIndexing();

        SWFDecompilerPlugin.fireAbcParsed(this, swf);
    }

    public void saveToStream(OutputStream os) throws IOException {
        ABCOutputStream aos = new ABCOutputStream(os);
        aos.writeU16(version.minor);
        aos.writeU16(version.major);

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

        if (hasDecimalSupport()) {
            aos.writeU30(constants.getDecimalCount());
            for (int i = 1; i < constants.getDecimalCount(); i++) {
                aos.writeDecimal(constants.getDecimal(i));
            }
        }
        if (hasFloatSupport()) {
            aos.writeU30(constants.getFloatCount());
            for (int i = 1; i < constants.getFloatCount(); i++) {
                aos.writeFloat(constants.getFloat(i));
            }
            aos.writeU30(constants.getFloat4Count());
            for (int i = 1; i < constants.getFloat4Count(); i++) {
                aos.writeFloat4(constants.getFloat4(i));
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

    public MethodBody findBody(MethodInfo methodInfo) {
        return getMethodIndexing().findMethodBody(methodInfo);
    }

    public MethodBody findBody(int methodInfo) {
        return getMethodIndexing().findMethodBody(methodInfo);
    }

    public int findBodyIndex(MethodInfo methodInfo) {
        return getMethodIndexing().findMethodBodyIndex(methodInfo);
    }

    public int findBodyIndex(int methodInfo) {
        return getMethodIndexing().findMethodBodyIndex(methodInfo);
    }

    public MethodBody findBodyClassInitializerByClass(String classNameWithSuffix) {
        for (int i = 0; i < instance_info.size(); i++) {
            if (classNameWithSuffix.equals(constants.getMultiname(instance_info.get(i).name_index).getName(constants, null, true, true))) {
                MethodBody body = findBody(class_info.get(i).cinit_index);
                if (body != null) {
                    return body;
                }
            }
        }

        return null;
    }

    public MethodBody findBodyInstanceInitializerByClass(String classNameWithSuffix) {
        for (int i = 0; i < instance_info.size(); i++) {
            if (classNameWithSuffix.equals(constants.getMultiname(instance_info.get(i).name_index).getName(constants, null, true, true))) {
                MethodBody body = findBody(instance_info.get(i).iinit_index);
                if (body != null) {
                    return body;
                }
            }
        }

        return null;
    }

    public MethodBody findBodyByClassAndName(String classNameWithSuffix, String methodNameWithSuffix) {
        for (int i = 0; i < instance_info.size(); i++) {
            if (classNameWithSuffix.equals(constants.getMultiname(instance_info.get(i).name_index).getName(constants, null, true, true))) {
                for (Trait t : instance_info.get(i).instance_traits.traits) {
                    if (t instanceof TraitMethodGetterSetter) {
                        TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                        if (methodNameWithSuffix.equals(t2.getName(this).getName(constants, null, true, true))) {
                            MethodBody body = findBody(t2.method_info);
                            if (body != null) {
                                return body;
                            }
                        }
                    }
                }

                for (Trait t : class_info.get(i).static_traits.traits) {
                    if (t instanceof TraitMethodGetterSetter) {
                        TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                        if (methodNameWithSuffix.equals(t2.getName(this).getName(constants, null, true, true))) {
                            MethodBody body = findBody(t2.method_info);
                            if (body != null) {
                                return body;
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
        if (traitId >= 0 && traitId < staticTraits.size()) {
            return staticTraits.get(traitId);
        } else {
            List<Trait> instanceTraits = instance_info.get(classIndex).instance_traits.traits;
            if (traitId >= 0 && traitId < staticTraits.size() + instanceTraits.size()) {
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
                            DottedChain val = constants.getMultiname(s.name_index).getNameWithNamespace(constants, true);
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
            deobfuscation = new AVM2Deobfuscation(getSwf(), constants);
        }

        return deobfuscation;
    }

    public final ABCMethodIndexing getMethodIndexing() {
        if (abcMethodIndexing == null) {
            abcMethodIndexing = new ABCMethodIndexing(this);
        }

        return abcMethodIndexing;
    }

    public DottedChain nsValueToName(String valueStr) {
        if (valueStr == null) {
            return DottedChain.EMPTY;
        }

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
            if (!script_info.get(i).deleted) {
                ret.addAll(script_info.get(i).getPacks(this, i, packagePrefix, allAbcs));
            }
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

    private void checkMultinameUsedInMethod(int multinameIndex, int methodInfo, List<MultinameUsage> ret, int scriptIndex, int classIndex, int traitIndex, int traitsType, boolean isInitializer, Traits traits, int parentTraitIndex) {
        for (int p = 0; p < method_info.get(methodInfo).param_types.length; p++) {
            if (method_info.get(methodInfo).param_types[p] == multinameIndex) {
                ret.add(new MethodParamsMultinameUsage(this, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                break;
            }
        }
        if (method_info.get(methodInfo).ret_type == multinameIndex) {
            ret.add(new MethodReturnTypeMultinameUsage(this, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
        }
        MethodBody body = findBody(methodInfo);
        if (body != null) {
            findMultinameUsageInTraits(body.traits, multinameIndex, traitsType, scriptIndex, classIndex, ret, traitIndex);
            for (ABCException e : body.exceptions) {
                if ((e.name_index == multinameIndex) || (e.type_index == multinameIndex)) {
                    ret.add(new MethodBodyMultinameUsage(this, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                    return;
                }
            }
            for (AVM2Instruction ins : body.getCode().code) {
                for (int o = 0; o < ins.definition.operands.length; o++) {
                    if (ins.definition.operands[o] == AVM2Code.DAT_MULTINAME_INDEX) {
                        if (ins.operands[o] == multinameIndex) {
                            ret.add(new MethodBodyMultinameUsage(this, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                            return;
                        }
                    }
                }
            }
        }
    }

    private void checkAllMultinameUsedInMethod(int methodInfo, List<List<MultinameUsage>> ret, int scriptIndex, int classIndex, int traitIndex, int traitsType, boolean isInitializer, Traits traits, int parentTraitIndex) {
        boolean[] foundMultinames = new boolean[constants.getMultinameCount()];
        for (int p = 0; p < method_info.get(methodInfo).param_types.length; p++) {
            int methodParamsMultinameIndex = method_info.get(methodInfo).param_types[p];
            if (!foundMultinames[methodParamsMultinameIndex]) {
                ret.get(methodParamsMultinameIndex).add(new MethodParamsMultinameUsage(this, methodParamsMultinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                foundMultinames[methodParamsMultinameIndex] = true;
            }
        }
        int methodReturnTypeMultinameIndex = method_info.get(methodInfo).ret_type;
        ret.get(methodReturnTypeMultinameIndex).add(new MethodReturnTypeMultinameUsage(this, methodReturnTypeMultinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));

        MethodBody body = findBody(methodInfo);
        if (body != null) {
            findAllMultinameUsageInTraits(body.traits, traitsType, scriptIndex, classIndex, ret, traitIndex);
            foundMultinames = new boolean[constants.getMultinameCount()];
            for (ABCException e : body.exceptions) {
                if (!foundMultinames[e.name_index]) {
                    ret.get(e.name_index).add(new MethodBodyMultinameUsage(this, e.name_index, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                    foundMultinames[e.name_index] = true;
                }

                if (!foundMultinames[e.type_index]) {
                    ret.get(e.type_index).add(new MethodBodyMultinameUsage(this, e.type_index, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                    foundMultinames[e.type_index] = true;
                }
            }
            for (AVM2Instruction ins : body.getCode().code) {
                for (int o = 0; o < ins.definition.operands.length; o++) {
                    if (ins.definition.operands[o] == AVM2Code.DAT_MULTINAME_INDEX) {
                        int mi = ins.operands[o];
                        if (!foundMultinames[mi]) {
                            ret.get(mi).add(new MethodBodyMultinameUsage(this, mi, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                            foundMultinames[mi] = true;
                        }
                    }
                }
            }
        }
    }

    private void findMultinameUsageInTraits(Traits traits, int multinameIndex, int traitsType, int scriptIndex, int classIndex, List<MultinameUsage> ret, int parentTraitIndex) {
        for (int t = 0; t < traits.traits.size(); t++) {
            if (traits.traits.get(t) instanceof TraitClass) {
                TraitClass tc = (TraitClass) traits.traits.get(t);
                if (tc.name_index == multinameIndex) {
                    ret.add(new ClassNameMultinameUsage(this, multinameIndex, tc.class_info, scriptIndex));
                }
                int c = tc.class_info;
                if (instance_info.get(c).super_index == multinameIndex) {
                    ret.add(new ExtendsMultinameUsage(this, multinameIndex, c, scriptIndex));
                }
                for (int i = 0; i < instance_info.get(c).interfaces.length; i++) {
                    if (instance_info.get(c).interfaces[i] == multinameIndex) {
                        ret.add(new ImplementsMultinameUsage(this, multinameIndex, c, scriptIndex));
                    }
                }
                checkMultinameUsedInMethod(multinameIndex, instance_info.get(c).iinit_index, ret, -1/*FIXME*/, c, 0, TraitMultinameUsage.TRAITS_TYPE_INSTANCE, true, null, -1);
                checkMultinameUsedInMethod(multinameIndex, class_info.get(c).cinit_index, ret, -1/*FIXME*/, c, 0, TraitMultinameUsage.TRAITS_TYPE_CLASS, true, null, -1);
                findMultinameUsageInTraits(instance_info.get(c).instance_traits, multinameIndex, TraitMultinameUsage.TRAITS_TYPE_INSTANCE, -1/*FIXME*/, c, ret, -1);
                findMultinameUsageInTraits(class_info.get(c).static_traits, multinameIndex, TraitMultinameUsage.TRAITS_TYPE_CLASS, -1/*FIXME*/, c, ret, -1);
            }
            if (traits.traits.get(t) instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) traits.traits.get(t);
                if (tsc.name_index == multinameIndex) {
                    ret.add(new ConstVarNameMultinameUsage(this, multinameIndex, scriptIndex, classIndex, t, traitsType, traits, parentTraitIndex));
                }
                if (tsc.type_index == multinameIndex) {
                    ret.add(new ConstVarTypeMultinameUsage(this, multinameIndex, scriptIndex, classIndex, t, traitsType, traits, parentTraitIndex));
                }
            }
            if (traits.traits.get(t) instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) traits.traits.get(t);
                if (tmgs.name_index == multinameIndex) {
                    ret.add(new MethodNameMultinameUsage(this, multinameIndex, scriptIndex, classIndex, t, traitsType, false, traits, parentTraitIndex));
                }
                checkMultinameUsedInMethod(multinameIndex, tmgs.method_info, ret, scriptIndex, classIndex, t, traitsType, false, traits, parentTraitIndex);
            }
        }
    }

    private void findAllMultinameUsageInTraits(Traits traits, int traitsType, int scriptIndex, int classIndex, List<List<MultinameUsage>> ret, int parentTraitIndex) {
        for (int t = 0; t < traits.traits.size(); t++) {
            if (traits.traits.get(t) instanceof TraitClass) {
                TraitClass tc = (TraitClass) traits.traits.get(t);
                ret.get(tc.name_index).add(new ClassNameMultinameUsage(this, tc.name_index, tc.class_info, scriptIndex));

                int c = tc.class_info;

                int classNameMultinameIndex = instance_info.get(c).name_index;
                ret.get(classNameMultinameIndex).add(new ClassNameMultinameUsage(this, classNameMultinameIndex, c, scriptIndex));
                int extendsMultinameIndex = instance_info.get(c).super_index;
                ret.get(extendsMultinameIndex).add(new ExtendsMultinameUsage(this, extendsMultinameIndex, c, scriptIndex));
                for (int i = 0; i < instance_info.get(c).interfaces.length; i++) {
                    int implementsMultinameIndex = instance_info.get(c).interfaces[i];
                    ret.get(implementsMultinameIndex).add(new ImplementsMultinameUsage(this, implementsMultinameIndex, c, scriptIndex));
                }
                checkAllMultinameUsedInMethod(instance_info.get(c).iinit_index, ret, -1/*FIXME*/, c, 0, TraitMultinameUsage.TRAITS_TYPE_INSTANCE, true, null, -1);
                checkAllMultinameUsedInMethod(class_info.get(c).cinit_index, ret, -1/*FIXME*/, c, 0, TraitMultinameUsage.TRAITS_TYPE_CLASS, true, null, -1);
                findAllMultinameUsageInTraits(instance_info.get(c).instance_traits, TraitMultinameUsage.TRAITS_TYPE_INSTANCE, -1/*FIXME*/, c, ret, -1);
                findAllMultinameUsageInTraits(class_info.get(c).static_traits, TraitMultinameUsage.TRAITS_TYPE_CLASS, -1/*FIXME*/, c, ret, -1);
            }
            if (traits.traits.get(t) instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) traits.traits.get(t);
                ret.get(tsc.name_index).add(new ConstVarNameMultinameUsage(this, tsc.name_index, scriptIndex, classIndex, t, traitsType, traits, parentTraitIndex));
                ret.get(tsc.type_index).add(new ConstVarTypeMultinameUsage(this, tsc.type_index, scriptIndex, classIndex, t, traitsType, traits, parentTraitIndex));
            }
            if (traits.traits.get(t) instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) traits.traits.get(t);
                ret.get(tmgs.name_index).add(new MethodNameMultinameUsage(this, tmgs.name_index, scriptIndex, classIndex, t, traitsType, false, traits, parentTraitIndex));
                checkAllMultinameUsedInMethod(tmgs.method_info, ret, scriptIndex, classIndex, t, traitsType, false, traits, parentTraitIndex);
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

    public List<MultinameUsage> findMultinameUsageOfNamespace(int namespaceIndex) {
        List<MultinameUsage> ret = new ArrayList<>();
        for (int multinameIndex = 1; multinameIndex < constants.getMultinameCount(); multinameIndex++) {
            if (constants.getMultiname(multinameIndex).namespace_index == namespaceIndex) {
                ret.addAll(findMultinameUsage(multinameIndex));
            }
        }
        return ret;
    }

    /**
     * Gets colliding usages of multinames. For example same name
     * consts/vars/methods or same class names. Mostly in obfuscated files.
     */
    public Set<MultinameUsage> getCollidingMultinameUsages() {
        //Reset
        for (int multinameIndex = 1; multinameIndex < constants.getMultinameCount(); multinameIndex++) {
            constants.getMultiname(multinameIndex).setDisplayNamespace(false);
        }

        //group qnames with same name
        Map<String, List<Integer>> nameToQNameIndices = new HashMap<>();
        for (int multinameIndex = 1; multinameIndex < constants.getMultinameCount(); multinameIndex++) {
            Multiname m = constants.getMultiname(multinameIndex);
            if (m.kind == Multiname.QNAME || m.kind == Multiname.QNAMEA) {
                String name = m.getName(constants, new ArrayList<>(), true, false);
                List<Integer> indices = nameToQNameIndices.get(name);
                if (indices == null) {
                    indices = new ArrayList<>();
                    nameToQNameIndices.put(name, indices);
                }

                indices.add(multinameIndex);
            }
        }
        Set<MultinameUsage> collidingUsages = new HashSet<>();

        // find context of names with count 2 or more
        List<List<MultinameUsage>> usagesList = findAllMultinameUsage();
        for (String name : nameToQNameIndices.keySet()) {
            List<Integer> multinameIndices = nameToQNameIndices.get(name);
            if (multinameIndices.size() > 1) {
                List<List<MultinameUsage>> allUsages = new ArrayList<>();
                for (int multinameIndex : multinameIndices) {
                    List<MultinameUsage> usages = usagesList.get(multinameIndex);
                    for (MultinameUsage usage : usages) {
                        for (List<MultinameUsage> prevUsages : allUsages) {
                            for (MultinameUsage prevUsage : prevUsages) {
                                if (prevUsage.collides(usage)) {
                                    collidingUsages.add(usage);
                                    collidingUsages.add(prevUsage);
                                }
                            }
                        }
                    }
                    allUsages.add(usages);
                }
            }
        }
        return collidingUsages;
    }

    /**
     * Appends namespace (#123) suffix to multinames which collide with each
     * other. For example same name consts/vars/methods or same class names.
     */
    public void refreshMultinameNamespaceSuffixes() {

        Set<MultinameUsage> collidingMultinameUsages = getCollidingMultinameUsages();

        Set<Integer> collidingMultinameIndices = new HashSet<>();

        for (MultinameUsage col : collidingMultinameUsages) {
            //System.err.println("collides " + col);
            collidingMultinameIndices.add(col.getMultinameIndex());
        }

        for (int multinameIndex : collidingMultinameIndices) {
            constants.getMultiname(multinameIndex).setDisplayNamespace(true);
        }
    }

    public List<MultinameUsage> findMultinameUsage(int multinameIndex) {
        List<MultinameUsage> ret = new ArrayList<>();
        if (multinameIndex == 0) {
            return ret;
        }
        for (int s = 0; s < script_info.size(); s++) {
            findMultinameUsageInTraits(script_info.get(s).traits, multinameIndex, TraitMultinameUsage.TRAITS_TYPE_SCRIPT, s, -1, ret, -1);
        }
        loopm:
        for (int t = 1; t < constants.getMultinameCount(); t++) {
            Multiname multiname = constants.getMultiname(t);
            if (multiname.kind == Multiname.TYPENAME) {
                if (multiname.qname_index == multinameIndex) {
                    ret.add(new TypeNameMultinameUsage(this, multinameIndex, t, -1));
                    continue;
                }
                for (int mp : multiname.params) {
                    if (mp == multinameIndex) {
                        ret.add(new TypeNameMultinameUsage(this, multinameIndex, t, -1));
                        continue loopm;
                    }
                }
            }
        }
        return ret;
    }

    public List<List<MultinameUsage>> findAllMultinameUsage() {
        List<List<MultinameUsage>> ret = new ArrayList<>();
        for (int i = 0; i < constants.getMultinameCount(); i++) {
            ret.add(new ArrayList<>());
        }

        for (int s = 0; s < script_info.size(); s++) {
            findAllMultinameUsageInTraits(script_info.get(s).traits, TraitMultinameUsage.TRAITS_TYPE_SCRIPT, s, -1, ret, -1);
        }

        boolean[] foundMultinames = new boolean[constants.getMultinameCount()];
        for (int t = 1; t < constants.getMultinameCount(); t++) {
            Multiname multiname = constants.getMultiname(t);
            if (multiname.kind == Multiname.TYPENAME) {
                if (!foundMultinames[multiname.qname_index]) {
                    ret.get(multiname.qname_index).add(new TypeNameMultinameUsage(this, multiname.qname_index, t, -1));
                    foundMultinames[multiname.qname_index] = true;
                }
                for (int mp : multiname.params) {
                    if (!foundMultinames[mp]) {
                        ret.get(mp).add(new TypeNameMultinameUsage(this, mp, t, -1));
                        foundMultinames[mp] = true;
                    }
                }
            }
        }
        return ret;
    }

    public int findMethodInfoByName(int classId, String methodNameWithSuffix) {
        if (classId > -1) {
            for (Trait t : instance_info.get(classId).instance_traits.traits) {
                if (t instanceof TraitMethodGetterSetter) {
                    if (t.getName(this).getName(constants, null, true, true).equals(methodNameWithSuffix)) {
                        return ((TraitMethodGetterSetter) t).method_info;
                    }
                }
            }
        }
        return -1;
    }

    public int findMethodBodyByName(int classId, String methodNameWithSuffix) {
        if (classId > -1) {
            for (Trait t : instance_info.get(classId).instance_traits.traits) {
                if (t instanceof TraitMethodGetterSetter) {
                    if (t.getName(this).getName(constants, null, true, true).equals(methodNameWithSuffix)) {
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

    public int findClassByName(String nameWithSuffix) {
        for (int c = 0; c < instance_info.size(); c++) {
            if (instance_info.get(c).deleted) {
                continue;
            }
            DottedChain s = constants.getMultiname(instance_info.get(c).name_index).getNameWithNamespace(constants, true);
            if (nameWithSuffix.equals(s.toRawString())) {
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

    public int getGlobalTraitId(TraitType type, boolean isStatic, int classIndex, int index) {
        if (type == TraitType.INITIALIZER) {
            if (!isStatic) {
                return GraphTextWriter.TRAIT_INSTANCE_INITIALIZER;
            } else {
                return GraphTextWriter.TRAIT_CLASS_INITIALIZER;
            }
        }

        if (type == TraitType.SCRIPT_INITIALIZER) {
            return GraphTextWriter.TRAIT_SCRIPT_INITIALIZER;
        }

        if (classIndex == -1) {
            return index;
        }

        if (isStatic) {
            return index;
        } else {
            return class_info.get(classIndex).static_traits.traits.size() + index;
        }
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
                            ins.setOperand(i, ins.operands[i] + 1, b.getCode(), b);
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

    public void reorganizeClasses(Map<Integer, Integer> classIndexMap) {
        for (MethodBody b : bodies) {
            for (AVM2Instruction ins : b.getCode().code) {
                for (int i = 0; i < ins.definition.operands.length; i++) {
                    if (ins.definition.operands[i] == AVM2Code.DAT_CLASS_INDEX) {
                        if (classIndexMap.containsKey(ins.operands[i])) {
                            ins.setOperand(i, classIndexMap.get(ins.operands[i]), b.getCode(), b);
                        }
                    }
                }
            }
        }
        for (ScriptInfo si : script_info) {
            reorganizeClassesInTraits(si.traits, classIndexMap);
        }
        for (MethodBody b : bodies) {
            reorganizeClassesInTraits(b.traits, classIndexMap);
        }
        Map<Integer, InstanceInfo> backupInstanceInfos = new HashMap<>();
        Map<Integer, ClassInfo> backupClassInfos = new HashMap<>();
        for (int from : classIndexMap.keySet()) {
            backupInstanceInfos.put(from, instance_info.get(from));
            backupClassInfos.put(from, class_info.get(from));
        }
        for (int from : classIndexMap.keySet()) {
            int to = classIndexMap.get(from);
            instance_info.set(to, backupInstanceInfos.get(from));
            class_info.set(to, backupClassInfos.get(from));
        }
    }

    private void reorganizeClassesInTraits(Traits traits, Map<Integer, Integer> classIndexMap) {
        for (Trait t : traits.traits) {
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                reorganizeClassesInTraits(instance_info.get(tc.class_info).instance_traits, classIndexMap);
                reorganizeClassesInTraits(class_info.get(tc.class_info).static_traits, classIndexMap);
                if (classIndexMap.containsKey(tc.class_info)) {
                    tc.class_info = classIndexMap.get(tc.class_info);
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
                            ins.setOperand(i, ins.operands[i] - 1, b.getCode(), b);
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
                            ins.setOperand(i, ins.operands[i] - 1, b.getCode(), b);
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

        abcMethodIndexing = null;

        method_info.remove(index);
    }

    public boolean replaceScriptPack(As3ScriptReplacerInterface replacer, ScriptPack pack, String as) throws As3ScriptReplaceException, IOException, InterruptedException {
        replacer.replaceScript(pack, as);
        ((Tag) parentTag).setModified(true);
        return pack.isSimple;
    }

    private void packMethods() {
        for (int m = 0; m < method_info.size(); m++) {
            if (method_info.get(m).deleted) {
                removeMethod(m);
                m--;
            }
        }
    }

    public void pack() {
        packMethods();
        for (int c = 0; c < instance_info.size(); c++) {
            if (instance_info.get(c).deleted) {
                removeClass(c);
                c--;
            }
        }
        packMethods();
        for (int s = 0; s < script_info.size(); s++) {
            if (script_info.get(s).deleted) {
                script_info.remove(s);
                s--;
            }
        }
        getSwf().clearAbcListCache();
        getSwf().clearScriptCache();
        getMethodIndexing();
    }

    /**
     * Merges second ABC to this one.
     *
     * @param secondABC
     */
    public void mergeABC(ABC secondABC) {
        Map<Integer, Integer> mergeStringMap = new HashMap<>();
        Map<Integer, Integer> mergeIntMap = new HashMap<>();
        Map<Integer, Integer> mergeUIntMap = new HashMap<>();
        Map<Integer, Integer> mergeDoubleMap = new HashMap<>();
        Map<Integer, Integer> mergeFloatMap = new HashMap<>();
        Map<Integer, Integer> mergeFloat4Map = new HashMap<>();
        Map<Integer, Integer> mergeDecimalMap = new HashMap<>();

        Map<Integer, Integer> mergeNamespaceMap = new HashMap<>();
        Map<Integer, Integer> mergeNamespaceSetMap = new HashMap<>();
        Map<Integer, Integer> mergeMultinameMap = new HashMap<>();
        Map<Integer, Integer> mergeMethodInfoMap = new HashMap<>();
        Map<Integer, Integer> mergeMethodBodyMap = new HashMap<>();
        Map<Integer, Integer> mergeClassIndexMap = new HashMap<>();
        Map<Integer, Integer> mergeMetaDataMap = new HashMap<>();
        Map<Integer, Integer> mergeScriptInfoMap = new HashMap<>();

        mergeABC(secondABC, mergeStringMap, mergeIntMap, mergeUIntMap, mergeDoubleMap, mergeFloatMap, mergeFloat4Map, mergeDecimalMap, mergeNamespaceMap, mergeNamespaceSetMap, mergeMultinameMap, mergeMethodInfoMap, mergeMethodBodyMap, mergeClassIndexMap, mergeMetaDataMap, mergeScriptInfoMap);
    }

    /**
     * Merges second ABC to this one. Gets mapping of indices.
     *
     * @param secondABC
     * @param mergeStringMap
     * @param mergeIntMap
     * @param mergeUIntMap
     * @param mergeDoubleMap
     * @param mergeFloatMap
     * @param mergeFloat4Map
     * @param mergeDecimalMap
     * @param mergeNamespaceMap
     * @param mergeNamespaceSetMap
     * @param mergeMultinameMap
     * @param mergeMethodInfoMap
     * @param mergeMethodBodyMap
     * @param mergeClassIndexMap
     * @param mergeMetaDataMap
     * @param mergeScriptInfoMap
     */
    public void mergeABC(ABC secondABC,
            Map<Integer, Integer> mergeStringMap,
            Map<Integer, Integer> mergeIntMap,
            Map<Integer, Integer> mergeUIntMap,
            Map<Integer, Integer> mergeDoubleMap,
            Map<Integer, Integer> mergeFloatMap,
            Map<Integer, Integer> mergeFloat4Map,
            Map<Integer, Integer> mergeDecimalMap,
            Map<Integer, Integer> mergeNamespaceMap,
            Map<Integer, Integer> mergeNamespaceSetMap,
            Map<Integer, Integer> mergeMultinameMap,
            Map<Integer, Integer> mergeMethodInfoMap,
            Map<Integer, Integer> mergeMethodBodyMap,
            Map<Integer, Integer> mergeClassIndexMap,
            Map<Integer, Integer> mergeMetaDataMap,
            Map<Integer, Integer> mergeScriptInfoMap
    ) {

        if (!version.equals(secondABC.version)) {
            throw new RuntimeException("ABC versions mismatch");
        }
        //Constants
        constants.merge(secondABC.constants, mergeStringMap, mergeIntMap, mergeUIntMap, mergeDoubleMap, mergeFloatMap, mergeFloat4Map, mergeDecimalMap, mergeNamespaceMap, mergeNamespaceSetMap, mergeMultinameMap);

        //Metadata
        for (int m = 0; m < secondABC.metadata_info.size(); m++) {
            MetadataInfo secondMetadataInfo = secondABC.metadata_info.get(m);
            MetadataInfo newMetadataInfo = new MetadataInfo();
            newMetadataInfo.name_index = mergeStringMap.get(secondMetadataInfo.name_index);
            newMetadataInfo.keys = new int[secondMetadataInfo.keys.length];
            newMetadataInfo.values = new int[secondMetadataInfo.values.length];
            for (int i = 0; i < secondMetadataInfo.keys.length; i++) {
                newMetadataInfo.keys[i] = mergeStringMap.get(secondMetadataInfo.keys[i]);
                newMetadataInfo.values[i] = mergeStringMap.get(secondMetadataInfo.values[i]);
            }
            int newIndex = metadata_info.size();
            metadata_info.add(newMetadataInfo);
            mergeMetaDataMap.put(m, newIndex);
        }

        //Method Info
        for (int i = 0; i < secondABC.method_info.size(); i++) {
            MethodInfo secondMethodInfo = secondABC.method_info.get(i);
            int newParamTypes[] = new int[secondMethodInfo.param_types.length];
            for (int t = 0; t < secondMethodInfo.param_types.length; t++) {
                newParamTypes[t] = mergeMultinameMap.get(secondMethodInfo.param_types[t]);
            }
            int newParamNames[] = new int[secondMethodInfo.paramNames.length];
            for (int n = 0; n < secondMethodInfo.paramNames.length; n++) {
                newParamNames[n] = mergeStringMap.get(secondMethodInfo.paramNames[n]);
            }
            int newRetType = mergeMultinameMap.get(secondMethodInfo.ret_type);
            int newNameIndex = mergeStringMap.get(secondMethodInfo.name_index);
            ValueKind newOptional[] = new ValueKind[secondMethodInfo.optional.length];
            for (int k = 0; k < secondMethodInfo.optional.length; k++) {
                int vkind = secondMethodInfo.optional[k].value_kind;
                Map<Integer, Integer> valueMergeMap = null;
                switch (vkind) {
                    case ValueKind.CONSTANT_Utf8:
                        valueMergeMap = mergeStringMap;
                        break;
                    case ValueKind.CONSTANT_Int:
                        valueMergeMap = mergeIntMap;
                        break;
                    case ValueKind.CONSTANT_UInt:
                        valueMergeMap = mergeUIntMap;
                        break;
                    case ValueKind.CONSTANT_Double:
                        valueMergeMap = mergeDoubleMap;
                        break;
                    case ValueKind.CONSTANT_DecimalOrFloat:
                        //assuming the second ABC has same decimal/float support
                        if (hasDecimalSupport()) {
                            valueMergeMap = mergeDecimalMap;
                        } else if (hasFloatSupport()) {
                            valueMergeMap = mergeFloatMap;
                        } else {
                            //should not happen
                        }
                        break;
                    case ValueKind.CONSTANT_Float4:
                        if (hasFloatSupport()) {
                            valueMergeMap = mergeFloat4Map;
                        }
                        break;
                    case ValueKind.CONSTANT_ExplicitNamespace:
                    case ValueKind.CONSTANT_Namespace:
                    case ValueKind.CONSTANT_PackageInternalNs:
                    case ValueKind.CONSTANT_PackageNamespace:
                    case ValueKind.CONSTANT_ProtectedNamespace:
                    case ValueKind.CONSTANT_PrivateNs:
                    case ValueKind.CONSTANT_StaticProtectedNs:
                        valueMergeMap = mergeNamespaceMap;
                        break;
                }

                int newValueIndex = valueMergeMap != null ? valueMergeMap.get(secondMethodInfo.optional[k].value_index) : secondMethodInfo.optional[k].value_index;
                newOptional[k] = new ValueKind(newValueIndex, vkind);
            }

            MethodInfo newMethodInfo = new MethodInfo(newParamTypes, newRetType, newNameIndex, secondMethodInfo.flags, newOptional, newParamNames);
            int newIndex = addMethodInfo(newMethodInfo);
            mergeMethodInfoMap.put(i, newIndex);
        }

        int classFirstIndex = class_info.size();
        //Class/Instance
        for (int c = 0; c < secondABC.class_info.size(); c++) {
            ClassInfo secondClassInfo = secondABC.class_info.get(c);
            ClassInfo newClassInfo = new ClassInfo();
            int newIndex = class_info.size();
            class_info.add(newClassInfo);
            mergeClassIndexMap.put(c, newIndex);
            newClassInfo.cinit_index = mergeMethodInfoMap.get(secondClassInfo.cinit_index);

            newClassInfo.lastDispId = secondClassInfo.lastDispId;
            InstanceInfo secondInstanceInfo = secondABC.instance_info.get(c);
            InstanceInfo newInstanceInfo = new InstanceInfo();
            newInstanceInfo.iinit_index = mergeMethodInfoMap.get(secondInstanceInfo.iinit_index);
            newInstanceInfo.super_index = mergeMultinameMap.get(secondInstanceInfo.super_index);
            newInstanceInfo.interfaces = new int[secondInstanceInfo.interfaces.length];
            for (int i = 0; i < secondInstanceInfo.interfaces.length; i++) {
                newInstanceInfo.interfaces[i] = mergeMultinameMap.get(secondInstanceInfo.interfaces[i]);
            }
            newInstanceInfo.protectedNS = mergeNamespaceMap.get(secondInstanceInfo.protectedNS);
            newInstanceInfo.name_index = mergeMultinameMap.get(secondInstanceInfo.name_index);
            newInstanceInfo.flags = secondInstanceInfo.flags;
            instance_info.add(newInstanceInfo);

            //do traits in second step as some classes may depend on other classes
        }

        //Class/Instance traits
        for (int c = 0; c < secondABC.class_info.size(); c++) {
            ClassInfo secondClassInfo = secondABC.class_info.get(c);
            InstanceInfo secondInstanceInfo = secondABC.instance_info.get(c);
            ClassInfo newClassInfo = class_info.get(classFirstIndex + c);
            InstanceInfo newInstanceInfo = instance_info.get(classFirstIndex + c);
            newClassInfo.static_traits = mergeTraits(secondClassInfo.static_traits, mergeMultinameMap, mergeStringMap, mergeIntMap, mergeUIntMap, mergeDoubleMap, mergeFloatMap, mergeFloat4Map, mergeDecimalMap, mergeNamespaceMap, mergeMetaDataMap, mergeMethodInfoMap, mergeClassIndexMap);
            newInstanceInfo.instance_traits = mergeTraits(secondInstanceInfo.instance_traits, mergeMultinameMap, mergeStringMap, mergeIntMap, mergeUIntMap, mergeDoubleMap, mergeFloatMap, mergeFloat4Map, mergeDecimalMap, mergeNamespaceMap, mergeMetaDataMap, mergeMethodInfoMap, mergeClassIndexMap);
        }

        //Scripts
        for (int s = 0; s < secondABC.script_info.size(); s++) {
            ScriptInfo secondScriptInfo = secondABC.script_info.get(s);
            ScriptInfo newScriptInfo = new ScriptInfo();
            newScriptInfo.init_index = mergeMethodInfoMap.get(secondScriptInfo.init_index);
            newScriptInfo.traits = mergeTraits(secondScriptInfo.traits, mergeMultinameMap, mergeStringMap, mergeIntMap, mergeUIntMap, mergeDoubleMap, mergeFloatMap, mergeFloat4Map, mergeDecimalMap, mergeNamespaceMap, mergeMetaDataMap, mergeMethodInfoMap, mergeClassIndexMap);
            int newIndex = script_info.size();
            script_info.add(newScriptInfo);
            mergeScriptInfoMap.put(s, newIndex);
        }

        //Method bodies
        for (int b = 0; b < secondABC.bodies.size(); b++) {
            MethodBody secondBody = secondABC.bodies.get(b);
            MethodBody newBody = secondBody.clone(true);
            newBody.method_info = mergeMethodInfoMap.get(secondBody.method_info);
            for (int e = 0; e < secondBody.exceptions.length; e++) {
                newBody.exceptions[e].name_index = mergeMultinameMap.get(secondBody.exceptions[e].name_index);
                newBody.exceptions[e].type_index = mergeMultinameMap.get(secondBody.exceptions[e].type_index);
            }
            AVM2Code newCode = newBody.getCode();
            for (AVM2Instruction newIns : newCode.code) {
                int newOperands[] = newIns.operands == null ? null : newIns.operands.clone();
                boolean modified = false;
                if (newIns.operands != null) {
                    for (int i = 0; i < newIns.definition.operands.length; i++) {
                        Map<Integer, Integer> mergeMap = null;
                        switch (newIns.definition.operands[i]) {
                            case AVM2Code.DAT_CLASS_INDEX:
                                mergeMap = mergeClassIndexMap;
                                break;
                            case AVM2Code.DAT_STRING_INDEX:
                                mergeMap = mergeStringMap;
                                break;
                            case AVM2Code.DAT_INT_INDEX:
                                mergeMap = mergeIntMap;
                                break;
                            case AVM2Code.DAT_UINT_INDEX:
                                mergeMap = mergeUIntMap;
                                break;
                            case AVM2Code.DAT_DOUBLE_INDEX:
                                mergeMap = mergeDoubleMap;
                                break;
                            case AVM2Code.DAT_DECIMAL_INDEX:
                                mergeMap = mergeDecimalMap;
                                break;
                            case AVM2Code.DAT_FLOAT_INDEX:
                                mergeMap = mergeFloatMap;
                                break;
                            case AVM2Code.DAT_FLOAT4_INDEX:
                                mergeMap = mergeFloat4Map;
                                break;
                            case AVM2Code.DAT_NAMESPACE_INDEX:
                                mergeMap = mergeNamespaceMap;
                                break;
                            case AVM2Code.DAT_METHOD_INDEX:
                                mergeMap = mergeMethodInfoMap;
                                break;
                            case AVM2Code.DAT_MULTINAME_INDEX:
                                mergeMap = mergeMultinameMap;
                                break;
                        }
                        if (mergeMap != null) {
                            newOperands[i] = mergeMap.get(newIns.operands[i]);
                            modified = true;
                        }
                    }
                }
                if (modified) {
                    newIns.setOperands(newOperands, newCode, newBody);
                }
                newBody.traits = mergeTraits(secondBody.traits, mergeMultinameMap, mergeStringMap, mergeIntMap, mergeUIntMap, mergeDoubleMap, mergeFloatMap, mergeFloat4Map, mergeDecimalMap, mergeNamespaceMap, mergeMetaDataMap, mergeMethodInfoMap, mergeClassIndexMap);
            }
            newBody.setModified();
            int newIndex = bodies.size();
            bodies.add(newBody);
            mergeMethodBodyMap.put(b, newIndex);
        }

        //clear caches
        abcMethodIndexing = null;
        getSwf().clearScriptCache();
        ((Tag) parentTag).setModified(true);
    }

    private Traits mergeTraits(Traits secondTraits, Map<Integer, Integer> mergeMultinameMap,
            Map<Integer, Integer> mergeStringMap,
            Map<Integer, Integer> mergeIntMap,
            Map<Integer, Integer> mergeUIntMap,
            Map<Integer, Integer> mergeDoubleMap,
            Map<Integer, Integer> mergeFloatMap,
            Map<Integer, Integer> mergeFloat4Map,
            Map<Integer, Integer> mergeDecimalMap,
            Map<Integer, Integer> mergeNamespaceMap,
            Map<Integer, Integer> mergeMetaDataMap,
            Map<Integer, Integer> mergeMethodInfoMap,
            Map<Integer, Integer> mergeClassIndexMap) {
        Traits newTraits = new Traits();
        for (Trait t : secondTraits.traits) {
            Trait newTrait = null;
            if (t instanceof TraitMethodGetterSetter) {
                newTrait = mergeTrait((TraitMethodGetterSetter) t, mergeMultinameMap, mergeMethodInfoMap, mergeMetaDataMap);
            } else if (t instanceof TraitSlotConst) {
                newTrait = mergeTrait((TraitSlotConst) t, mergeMultinameMap, mergeStringMap, mergeIntMap, mergeUIntMap, mergeDoubleMap, mergeFloatMap, mergeFloat4Map, mergeDecimalMap, mergeNamespaceMap, mergeMetaDataMap);
            } else if (t instanceof TraitFunction) {
                newTrait = mergeTrait((TraitFunction) t, mergeMultinameMap, mergeMethodInfoMap, mergeMetaDataMap);
            } else if (t instanceof TraitClass) {
                newTrait = mergeTrait((TraitClass) t, mergeClassIndexMap, mergeMultinameMap, mergeMetaDataMap);
            } else {
                //should not happen
            }

            newTraits.addTrait(newTrait);

        }
        return newTraits;
    }

    private TraitMethodGetterSetter mergeTrait(TraitMethodGetterSetter secondTrait, Map<Integer, Integer> mergeMultinameMap, Map<Integer, Integer> mergeMethodInfoMap, Map<Integer, Integer> mergeMetaDataMap) {
        TraitMethodGetterSetter newTrait = secondTrait.clone();
        newTrait.method_info = mergeMethodInfoMap.get(secondTrait.method_info);
        newTrait.name_index = mergeMultinameMap.get(secondTrait.name_index);
        for (int m = 0; m < secondTrait.metadata.length; m++) {
            newTrait.metadata[m] = mergeMetaDataMap.get(secondTrait.metadata[m]);
        }
        return newTrait;
    }

    private TraitSlotConst mergeTrait(TraitSlotConst secondTrait, Map<Integer, Integer> mergeMultinameMap,
            Map<Integer, Integer> mergeStringMap,
            Map<Integer, Integer> mergeIntMap,
            Map<Integer, Integer> mergeUIntMap,
            Map<Integer, Integer> mergeDoubleMap,
            Map<Integer, Integer> mergeFloatMap,
            Map<Integer, Integer> mergeFloat4Map,
            Map<Integer, Integer> mergeDecimalMap,
            Map<Integer, Integer> mergeNamespaceMap,
            Map<Integer, Integer> mergeMetaDataMap
    ) {
        TraitSlotConst newTrait = secondTrait.clone();
        newTrait.name_index = mergeMultinameMap.get(secondTrait.name_index);
        for (int m = 0; m < secondTrait.metadata.length; m++) {
            newTrait.metadata[m] = mergeMetaDataMap.get(secondTrait.metadata[m]);
        }
        newTrait.type_index = mergeMultinameMap.get(secondTrait.type_index);
        Map<Integer, Integer> valueMergeMap = null;

        switch (newTrait.value_kind) {
            case ValueKind.CONSTANT_Utf8:
                valueMergeMap = mergeStringMap;
                break;
            case ValueKind.CONSTANT_Int:
                valueMergeMap = mergeIntMap;
                break;
            case ValueKind.CONSTANT_UInt:
                valueMergeMap = mergeUIntMap;
                break;
            case ValueKind.CONSTANT_Double:
                valueMergeMap = mergeDoubleMap;
                break;
            case ValueKind.CONSTANT_DecimalOrFloat:
                //assuming the second ABC has same decimal/float support
                if (hasDecimalSupport()) {
                    valueMergeMap = mergeDecimalMap;
                } else if (hasFloatSupport()) {
                    valueMergeMap = mergeFloatMap;
                } else {
                    //should not happen
                }
                break;
            case ValueKind.CONSTANT_Float4:
                if (hasFloatSupport()) {
                    valueMergeMap = mergeFloat4Map;
                }
                break;
            case ValueKind.CONSTANT_ExplicitNamespace:
            case ValueKind.CONSTANT_Namespace:
            case ValueKind.CONSTANT_PackageInternalNs:
            case ValueKind.CONSTANT_PackageNamespace:
            case ValueKind.CONSTANT_PrivateNs:
            case ValueKind.CONSTANT_ProtectedNamespace:
            case ValueKind.CONSTANT_StaticProtectedNs:
                valueMergeMap = mergeNamespaceMap;
                break;
        }
        if (valueMergeMap != null) {
            newTrait.value_index = valueMergeMap.get(secondTrait.value_index);
        }
        return newTrait; //TODO
    }

    private TraitFunction mergeTrait(TraitFunction secondTrait, Map<Integer, Integer> mergeMultinameMap, Map<Integer, Integer> mergeMethodInfoMap, Map<Integer, Integer> mergeMetaDataMap) {
        TraitFunction newTrait = secondTrait.clone();
        newTrait.method_info = mergeMethodInfoMap.get(secondTrait.method_info);
        newTrait.name_index = mergeMultinameMap.get(secondTrait.name_index);
        for (int m = 0; m < secondTrait.metadata.length; m++) {
            newTrait.metadata[m] = mergeMetaDataMap.get(secondTrait.metadata[m]);
        }
        return newTrait;
    }

    private TraitClass mergeTrait(TraitClass secondTrait, Map<Integer, Integer> mergeClassMap, Map<Integer, Integer> mergeMultinameMap, Map<Integer, Integer> mergeMetaDataMap) {
        TraitClass newTrait = secondTrait.clone();
        newTrait.class_info = mergeClassMap.get(secondTrait.class_info);
        newTrait.name_index = mergeMultinameMap.get(secondTrait.name_index);
        for (int m = 0; m < secondTrait.metadata.length; m++) {
            newTrait.metadata[m] = mergeMetaDataMap.get(secondTrait.metadata[m]);
        }
        return newTrait;
    }

    public DottedChain findCustomNs(int link_ns_index) {
        String nsname;
        if (link_ns_index <= 0) {
            return null;
        }
        Namespace ns = constants.getNamespace(link_ns_index);
        if (ns.kind != Namespace.KIND_NAMESPACE) {
            return null;
        }
        String name = constants.getString(ns.name_index);
        for (ABCContainerTag abcTag : getAbcTags()) {
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

    public void clearPacksCache() {
        for (ScriptInfo si : script_info) {
            si.clearPacksCache();
        }
    }

    public void free() {
        deobfuscation = null;
        abcMethodIndexing = null;
    }
}
