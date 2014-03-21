/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Deobfuscation;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.UnknownInstructionCode;
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
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.abc.usages.ClassNameMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.ConstVarNameMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.ConstVarTypeMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.ExtendsMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.ImplementsMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MethodBodyMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MethodNameMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MethodParamsMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MethodReturnTypeMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.TypeNameMultinameUsage;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.helpers.utf8.Utf8PrintWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class ABC {

    public int major_version = 46;
    public int minor_version = 16;
    public ConstantPool constants=new ConstantPool();
    public List<MethodInfo> method_info = new ArrayList<>();
    public List<MetadataInfo> metadata_info = new ArrayList<>();
    public List<InstanceInfo> instance_info=new ArrayList<>();
    public List<ClassInfo> class_info = new ArrayList<>();
    public List<ScriptInfo> script_info = new ArrayList<>();
    public List<MethodBody> bodies = new ArrayList<>();
    private List<Integer> bodyIdxFromMethodIdx = new ArrayList<>();
    public long[] stringOffsets;
    public static final int MINORwithDECIMAL = 17;
    protected Set<EventListener> listeners = new HashSet<>();
    private static final Logger logger = Logger.getLogger(ABC.class.getName());
    private final AVM2Deobfuscation deobfuscation;
    public SWF swf;
    public ABCContainerTag parentTag;

    public ABC(SWF swf) {
        this.deobfuscation = null;        
        this.swf = swf;        
    }

    
    
    public int addMethodBody(MethodBody body) {
        bodies.add(body);
        if (body.method_info >= bodyIdxFromMethodIdx.size()) {
            int newlen = body.method_info + 1;
            int oldlen = bodyIdxFromMethodIdx.size();
            for (int i = oldlen; i < newlen; i++) {
                bodyIdxFromMethodIdx.add(-1);
            }
            bodyIdxFromMethodIdx.set(body.method_info,bodies.size() - 1);
        }
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
            for (AVM2Instruction ins : body.code.code) {
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
        if (stringUsages.contains(strIndex) || namespaceUsages.contains(strIndex)) { //name is used elsewhere as string literal            
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
        for (int i = 0; i < instance_info.size(); i++) {
            informListeners("deobfuscate", "class " + i + "/" + instance_info.size());
            InstanceInfo insti=instance_info.get(i);
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
            if (constants.getNamespace(i).kind != Namespace.KIND_PACKAGE) { //only packages
                continue;
            }
            constants.getNamespace(i).name_index = deobfuscation.deobfuscatePackageName(stringUsageTypes, stringUsages, namesMap, constants.getNamespace(i).name_index, renameType);
        }

        //process reflection using getDefinitionByName too
        for (MethodBody body : bodies) {
            for (int ip = 0; ip < body.code.code.size(); ip++) {
                if (body.code.code.get(ip).definition instanceof CallPropertyIns) {
                    int mIndex = body.code.code.get(ip).operands[0];
                    if (mIndex > 0) {
                        Multiname m = constants.getMultiname(mIndex);
                        if (m.getNameWithNamespace(constants).equals("flash.utils.getDefinitionByName")) {
                            if (ip > 0) {
                                if (body.code.code.get(ip - 1).definition instanceof PushStringIns) {
                                    int strIndex = body.code.code.get(ip - 1).operands[0];
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
                                    body.code.code.get(ip - 1).operands[0] = strIndex;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public ABC(InputStream is, SWF swf, ABCContainerTag tag) throws IOException {
        this.swf = swf;
        this.parentTag = tag;
        ABCInputStream ais = new ABCInputStream(is);
        minor_version = ais.readU16();
        major_version = ais.readU16();
        logger.log(Level.FINE, "ABC minor_version: {0}, major_version: {1}", new Object[]{minor_version, major_version});
        constants = new ConstantPool();
        deobfuscation = new AVM2Deobfuscation(constants);
        //constant integers
        int constant_int_pool_count = ais.readU30();
        constants.constant_int = new ArrayList<>(constant_int_pool_count);
        if (constant_int_pool_count > 0) {
            constants.addInt(0);
        }
        for (int i = 1; i < constant_int_pool_count; i++) { //index 0 not used. Values 1..n-1         
            constants.addInt(ais.readS32());
        }

        //constant unsigned integers
        int constant_uint_pool_count = ais.readU30();
        constants.constant_uint = new ArrayList<>(constant_uint_pool_count);
        if (constant_uint_pool_count > 0) {
            constants.addUInt(0);
        }
        for (int i = 1; i < constant_uint_pool_count; i++) { //index 0 not used. Values 1..n-1
            constants.addUInt(ais.readU32());
        }

        //constant double
        int constant_double_pool_count = ais.readU30();
        constants.constant_double = new ArrayList<>(constant_double_pool_count);
        if (constant_double_pool_count > 0) {
            constants.addDouble(0);
        }
        for (int i = 1; i < constant_double_pool_count; i++) { //index 0 not used. Values 1..n-1
            constants.addDouble(ais.readDouble());
        }

        //constant decimal
        if (minor_version >= MINORwithDECIMAL) {
            int constant_decimal_pool_count = ais.readU30();
            constants.constant_decimal = new ArrayList<>(constant_decimal_pool_count);
            if (constant_decimal_pool_count > 0) {
                constants.addDecimal(null);
            }
            for (int i = 1; i < constant_decimal_pool_count; i++) { //index 0 not used. Values 1..n-1
                constants.addDecimal(ais.readDecimal());
            }
        } else {
            constants.constant_decimal = new ArrayList<>(0);
        }

        //constant string
        int constant_string_pool_count = ais.readU30();
        constants.constant_string = new ArrayList<>(constant_string_pool_count);
        stringOffsets = new long[constant_string_pool_count];
        if (constant_string_pool_count > 0) {
            constants.addString("");
        }
        for (int i = 1; i < constant_string_pool_count; i++) { //index 0 not used. Values 1..n-1
            long pos = ais.getPosition();
            constants.addString(ais.readString());
            stringOffsets[i] = pos;
        }

        //constant namespace
        int constant_namespace_pool_count = ais.readU30();
        constants.constant_namespace = new ArrayList<>(constant_namespace_pool_count);
        if (constant_namespace_pool_count > 0) {
            constants.addNamespace(null);
        }
        for (int i = 1; i < constant_namespace_pool_count; i++) { //index 0 not used. Values 1..n-1
            constants.addNamespace(ais.readNamespace());
        }

        //constant namespace set
        int constant_namespace_set_pool_count = ais.readU30();
        constants.constant_namespace_set = new ArrayList<>(constant_namespace_set_pool_count);
        if (constant_namespace_set_pool_count > 0) {
            constants.addNamespaceSet(null);
        }
        for (int i = 1; i < constant_namespace_set_pool_count; i++) { //index 0 not used. Values 1..n-1
            constants.addNamespaceSet(new NamespaceSet());
            int namespace_count = ais.readU30();
            constants.getNamespaceSet(i).namespaces = new int[namespace_count];
            for (int j = 0; j < namespace_count; j++) {
                constants.getNamespaceSet(i).namespaces[j] = ais.readU30();
            }
        }

        //constant multiname
        int constant_multiname_pool_count = ais.readU30();
        constants.constant_multiname = new ArrayList<>(constant_multiname_pool_count);
        if (constant_multiname_pool_count > 0) {
            constants.addMultiname(null);
        }
        for (int i = 1; i < constant_multiname_pool_count; i++) { //index 0 not used. Values 1..n-1
            constants.addMultiname(ais.readMultiname());
        }

        //method info
        int methods_count = ais.readU30();
        method_info = new ArrayList<>();//MethodInfo[methods_count];
        bodyIdxFromMethodIdx = new ArrayList<>(); //[methods_count];
        for (int i = 0; i < methods_count; i++) {
            method_info.add(ais.readMethodInfo());
            bodyIdxFromMethodIdx.add(-1);
        }

        //metadata info
        int metadata_count = ais.readU30();
        metadata_info = new ArrayList<>();
        for (int i = 0; i < metadata_count; i++) {
            int name_index = ais.readU30();
            int values_count = ais.readU30();
            int[] keys = new int[values_count];
            for (int v = 0; v < values_count; v++) {
                keys[v] = ais.readU30();
            }
            int[] values = new int[values_count];
            for (int v = 0; v < values_count; v++) {
                values[v] = ais.readU30();
            }
            metadata_info.add(new MetadataInfo(name_index, keys, values));
        }

        int class_count = ais.readU30();
        instance_info = new ArrayList<>(); //InstanceInfo[class_count];
        for (int i = 0; i < class_count; i++) {
            instance_info.add(ais.readInstanceInfo());
        }
        class_info = new ArrayList<>(); //ClassInfo[class_count];
        for (int i = 0; i < class_count; i++) {
            ClassInfo ci = new ClassInfo();
            ci.cinit_index = ais.readU30();
            ci.static_traits = ais.readTraits();
            class_info.add(ci);
        }
        int script_count = ais.readU30();
        script_info = new ArrayList<>(); //ScriptInfo[script_count];
        for (int i = 0; i < script_count; i++) {
            ScriptInfo si = new ScriptInfo();
            si.init_index = ais.readU30();
            si.traits = ais.readTraits();
            script_info.add(si);
        }

        int bodies_count = ais.readU30();
        bodies = new ArrayList<>(); //MethodBody[bodies_count];
        for (int i = 0; i < bodies_count; i++) {
            MethodBody mb = new MethodBody();
            mb.method_info = ais.readU30();
            mb.max_stack = ais.readU30();
            mb.max_regs = ais.readU30();
            mb.init_scope_depth = ais.readU30();
            mb.max_scope_depth = ais.readU30();
            int code_length = ais.readU30();
            mb.codeBytes = ais.readBytes(code_length);
            try {
                mb.code = new AVM2Code(new ByteArrayInputStream(mb.codeBytes));
            } catch (UnknownInstructionCode re) {
                mb.code = new AVM2Code();
                Logger.getLogger(ABC.class.getName()).log(Level.SEVERE, null, re);
            }
            mb.code.compact();
            int ex_count = ais.readU30();
            mb.exceptions = new ABCException[ex_count];
            for (int j = 0; j < ex_count; j++) {
                ABCException abce = new ABCException();
                abce.start = ais.readU30();
                abce.end = ais.readU30();
                abce.target = ais.readU30();
                abce.type_index = ais.readU30();
                abce.name_index = ais.readU30();
                mb.exceptions[j] = abce;
            }
            mb.traits = ais.readTraits();
            bodies.add(mb);
            method_info.get(mb.method_info).setBody(mb);
            bodyIdxFromMethodIdx.set(mb.method_info,i);
        }
        loadNamespaceMap();
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
        for (MethodInfo mi:method_info) {
            aos.writeMethodInfo(mi);
        }

        aos.writeU30(metadata_info.size());
        for (MetadataInfo mi:metadata_info) {
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
        for (InstanceInfo ii:instance_info) {
            aos.writeInstanceInfo(ii);
        }
        for (ClassInfo ci:class_info) {
            aos.writeU30(ci.cinit_index);
            aos.writeTraits(ci.static_traits);
        }
        aos.writeU30(script_info.size());
        for (ScriptInfo si:script_info) {
            aos.writeU30(si.init_index);
            aos.writeTraits(si.traits);
        }

        aos.writeU30(bodies.size());
        for (MethodBody mb: bodies) {
            aos.writeU30(mb.method_info);
            aos.writeU30(mb.max_stack);
            aos.writeU30(mb.max_regs);
            aos.writeU30(mb.init_scope_depth);
            aos.writeU30(mb.max_scope_depth);
            byte[] codeBytes = mb.code.getBytes();
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
        return bodyIdxFromMethodIdx.get(methodInfo);
    }

    public MethodBody findBodyByClassAndName(String className, String methodName) {
        for (int i = 0; i < instance_info.size(); i++) {
            if (className.equals(constants.getMultiname(instance_info.get(i).name_index).getName(constants, new ArrayList<String>()))) {
                for (Trait t : instance_info.get(i).instance_traits.traits) {
                    if (t instanceof TraitMethodGetterSetter) {
                        TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                        if (methodName.equals(t2.getName(this).getName(constants, new ArrayList<String>()))) {
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
            if (className.equals(constants.getMultiname(instance_info.get(i).name_index).getName(constants, new ArrayList<String>()))) {
                for (Trait t : class_info.get(i).static_traits.traits) {
                    if (t instanceof TraitMethodGetterSetter) {
                        TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                        if (methodName.equals(t2.getName(this).getName(constants, new ArrayList<String>()))) {
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
        if (traitId < class_info.get(classIndex).static_traits.traits.length) {
            return true;
        } else if (traitId < class_info.get(classIndex).static_traits.traits.length + instance_info.get(classIndex).instance_traits.traits.length) {
            return false;
        } else {
            return true; //Can be class or instance initializer
        }
    }

    public Trait findTraitByTraitId(int classIndex, int traitId) {
        if (classIndex == -1) {
            return null;
        }
        if (traitId < class_info.get(classIndex).static_traits.traits.length) {
            return class_info.get(classIndex).static_traits.traits[traitId];
        } else if (traitId < class_info.get(classIndex).static_traits.traits.length + instance_info.get(classIndex).instance_traits.traits.length) {
            traitId -= class_info.get(classIndex).static_traits.traits.length;
            return instance_info.get(classIndex).instance_traits.traits[traitId];
        } else {
            return null; //Can be class or instance initializer
        }
    }

    public int findMethodIdByTraitId(int classIndex, int traitId) {
        if (classIndex == -1) {
            return -1;
        }
        if (traitId < class_info.get(classIndex).static_traits.traits.length) {
            if (class_info.get(classIndex).static_traits.traits[traitId] instanceof TraitMethodGetterSetter) {
                return ((TraitMethodGetterSetter) class_info.get(classIndex).static_traits.traits[traitId]).method_info;
            } else {
                return -1;
            }
        } else if (traitId < class_info.get(classIndex).static_traits.traits.length + instance_info.get(classIndex).instance_traits.traits.length) {
            traitId -= class_info.get(classIndex).static_traits.traits.length;
            if (instance_info.get(classIndex).instance_traits.traits[traitId] instanceof TraitMethodGetterSetter) {
                return ((TraitMethodGetterSetter) instance_info.get(classIndex).instance_traits.traits[traitId]).method_info;
            } else {
                return -1;
            }
        } else {
            traitId -= class_info.get(classIndex).static_traits.traits.length + instance_info.get(classIndex).instance_traits.traits.length;
            if (traitId == 0) {
                return instance_info.get(classIndex).iinit_index;
            } else if (traitId == 1) {
                return class_info.get(classIndex).cinit_index;
            } else {
                return -1;
            }
        }
    }
    /* Map from multiname index of namespace value to namespace name**/
    private HashMap<String, String> namespaceMap;

    private void loadNamespaceMap() {
        namespaceMap = new HashMap<>();
        for (ScriptInfo si : script_info) {
            for (Trait t : si.traits.traits) {
                if (t instanceof TraitSlotConst) {
                    TraitSlotConst s = ((TraitSlotConst) t);
                    if (s.isNamespace()) {
                        String key = constants.getNamespace(s.value_index).getName(constants);
                        String val = constants.getMultiname(s.name_index).getNameWithNamespace(constants);
                        namespaceMap.put(key, val);
                    }
                }
            }
        }
    }

    public String nsValueToName(String value) {
        if (namespaceMap.containsKey(value)) {
            return namespaceMap.get(value);
        } else {
            String ns = deobfuscation.builtInNs(value);
            if (ns == null) {
                return "";
            } else {
                return ns;
            }
        }
    }

    public List<MyEntry<ClassPath, ScriptPack>> getScriptPacks() {
        List<MyEntry<ClassPath, ScriptPack>> ret = new ArrayList<>();
        for (int i = 0; i < script_info.size(); i++) {
            ret.addAll(script_info.get(i).getPacks(this, i));
        }
        return ret;
    }

    public void dump(OutputStream os) {
        Utf8PrintWriter output;
        output = new Utf8PrintWriter(os);
        constants.dump(output);
        for (int i = 0; i < method_info.size(); i++) {
            output.println("MethodInfo[" + i + "]:" + method_info.get(i).toString(constants, new ArrayList<String>()));
        }
        for (int i = 0; i < metadata_info.size(); i++) {
            output.println("MetadataInfo[" + i + "]:" + metadata_info.get(i).toString(constants));
        }
        for (int i = 0; i < instance_info.size(); i++) {
            output.println("InstanceInfo[" + i + "]:" + instance_info.get(i).toString(this, new ArrayList<String>()));
        }
        for (int i = 0; i < class_info.size(); i++) {
            output.println("ClassInfo[" + i + "]:" + class_info.get(i).toString(this, new ArrayList<String>()));
        }
        for (int i = 0; i < script_info.size(); i++) {
            output.println("ScriptInfo[" + i + "]:" + script_info.get(i).toString(this, new ArrayList<String>()));
        }
        for (int i = 0; i < bodies.size(); i++) {
            output.println("MethodBody[" + i + "]:"); //+ bodies[i].toString(this, constants, method_info));
        }
    }

    private void checkMultinameUsedInMethod(int multinameIndex, int methodInfo, List<MultinameUsage> ret, int classIndex, int traitIndex, boolean isStatic, boolean isInitializer, Traits traits, int parentTraitIndex) {
        for (int p = 0; p < method_info.get(methodInfo).param_types.length; p++) {
            if (method_info.get(methodInfo).param_types[p] == multinameIndex) {
                ret.add(new MethodParamsMultinameUsage(multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
                break;
            }
        }
        if (method_info.get(methodInfo).ret_type == multinameIndex) {
            ret.add(new MethodReturnTypeMultinameUsage(multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
        }
        MethodBody body = findBody(methodInfo);
        if (body != null) {
            findMultinameUsageInTraits(body.traits, multinameIndex, isStatic, classIndex, ret, traitIndex);
            for (ABCException e : body.exceptions) {
                if ((e.name_index == multinameIndex) || (e.type_index == multinameIndex)) {
                    ret.add(new MethodBodyMultinameUsage(multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
                    return;
                }
            }
            for (AVM2Instruction ins : body.code.code) {
                for (int o = 0; o < ins.definition.operands.length; o++) {
                    if (ins.definition.operands[o] == AVM2Code.DAT_MULTINAME_INDEX) {
                        if (ins.operands[o] == multinameIndex) {
                            ret.add(new MethodBodyMultinameUsage(multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
                            return;
                        }
                    }
                }
            }
        }
    }

    private void findMultinameUsageInTraits(Traits traits, int multinameIndex, boolean isStatic, int classIndex, List<MultinameUsage> ret, int parentTraitIndex) {
        for (int t = 0; t < traits.traits.length; t++) {
            if (traits.traits[t] instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) traits.traits[t];
                if (tsc.name_index == multinameIndex) {
                    ret.add(new ConstVarNameMultinameUsage(multinameIndex, classIndex, t, isStatic, traits, parentTraitIndex));
                }
                if (tsc.type_index == multinameIndex) {
                    ret.add(new ConstVarTypeMultinameUsage(multinameIndex, classIndex, t, isStatic, traits, parentTraitIndex));
                }
            }
            if (traits.traits[t] instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) traits.traits[t];
                if (tmgs.name_index == multinameIndex) {
                    ret.add(new MethodNameMultinameUsage(multinameIndex, classIndex, t, isStatic, false, traits, parentTraitIndex));
                }
                checkMultinameUsedInMethod(multinameIndex, tmgs.method_info, ret, classIndex, t, isStatic, false, traits, parentTraitIndex);
            }
        }
    }

    public List<MultinameUsage> findMultinameUsage(int multinameIndex) {
        List<MultinameUsage> ret = new ArrayList<>();
        if (multinameIndex == 0) {
            return ret;
        }
        for (int c = 0; c < instance_info.size(); c++) {
            if (instance_info.get(c).name_index == multinameIndex) {
                ret.add(new ClassNameMultinameUsage(multinameIndex, c));
            }
            if (instance_info.get(c).super_index == multinameIndex) {
                ret.add(new ExtendsMultinameUsage(multinameIndex, c));
            }
            for (int i = 0; i < instance_info.get(c).interfaces.length; i++) {
                if (instance_info.get(c).interfaces[i] == multinameIndex) {
                    ret.add(new ImplementsMultinameUsage(multinameIndex, c));
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
                    ret.add(new TypeNameMultinameUsage(m));
                    continue;
                }
                for (int mp : constants.getMultiname(m).params) {
                    if (mp == multinameIndex) {
                        ret.add(new TypeNameMultinameUsage(m));
                        continue loopm;
                    }
                }
            }
        }
        return ret;
    }

    public void autoFillAllBodyParams() {
        for (int i = 0; i < bodies.size(); i++) {
            bodies.get(i).autoFillStats(this);
        }
    }

    public int findMethodInfoByName(int classId, String methodName) {
        if (classId > -1) {
            for (Trait t : instance_info.get(classId).instance_traits.traits) {
                if (t instanceof TraitMethodGetterSetter) {
                    if (t.getName(this).getName(constants, new ArrayList<String>()).equals(methodName)) {
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
                    if (t.getName(this).getName(constants, new ArrayList<String>()).equals(methodName)) {
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

    public int findClassByName(String name) {
        for (int c = 0; c < instance_info.size(); c++) {
            String s = constants.getMultiname(instance_info.get(c).name_index).getNameWithNamespace(constants);
            if (name.equals(s)) {
                return c;
            }
        }
        return -1;
    }

    public ScriptPack findScriptPackByPath(String name) {
        List<MyEntry<ClassPath, ScriptPack>> packs = getScriptPacks();
        for (MyEntry<ClassPath, ScriptPack> en : packs) {
            if (en.key.toString().equals(name)) {
                return en.value;
            }
        }
        return null;
    }
}
