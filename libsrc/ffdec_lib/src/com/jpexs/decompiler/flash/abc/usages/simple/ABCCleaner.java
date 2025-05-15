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
package com.jpexs.decompiler.flash.abc.usages.simple;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCOutputStream;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
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
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.NulStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cleans unused items from ABC file.
 *
 * @author JPEXS
 */
public class ABCCleaner {

    /**
     * Constructs ABCCleaner.
     */
    public ABCCleaner() {
    }  
    
    /**
     * Cleans unused items from ABC file.
     *
     * @param abc ABC file
     */
    public void clean(ABC abc) {
        ABCSimpleUsageDetector usageDetector = new ABCSimpleUsageDetector(abc);
        usageDetector.detect();
        Map<ABCSimpleUsageDetector.ItemKind, List<List<String>>> usages = usageDetector.getUsages();
        Map<ABCSimpleUsageDetector.ItemKind, List<Integer>> notReferencedIndices = new HashMap<>();
        Map<ABCSimpleUsageDetector.ItemKind, Map<Integer, Integer>> replaceMap = new HashMap<>();

        for (ABCSimpleUsageDetector.ItemKind kind : usages.keySet()) {
            List<List<String>> usagesList = usages.get(kind);
            notReferencedIndices.put(kind, new ArrayList<>());
            replaceMap.put(kind, new HashMap<>());
            if (kind.hasReservedZeroIndex()) {
                replaceMap.get(kind).put(0, 0);
            }
            int pos = kind.hasReservedZeroIndex() ? 1 : 0;
            for (int i = pos; i < usagesList.size(); i++) {
                if (usagesList.get(i).isEmpty()) {
                    notReferencedIndices.get(kind).add(i);
                } else {
                    replaceMap.get(kind).put(i, pos);
                    pos++;
                }
            }
        }

        for (int i = 0; i < abc.script_info.size(); i++) {
            ScriptInfo m = abc.script_info.get(i);
            m.init_index = handleReplace(ABCSimpleUsageDetector.ItemKind.METHODINFO, m.init_index, replaceMap);
            walkTraits(abc, m.traits, replaceMap);
        }

        for (int i = 0; i < abc.method_info.size(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.METHODINFO).contains(i)) {
                continue;
            }
            MethodInfo m = abc.method_info.get(i);
            m.name_index = handleReplace(ABCSimpleUsageDetector.ItemKind.STRING, m.name_index, replaceMap);
            if (m.flagHas_paramnames()) {
                for (int j = 0; j < m.paramNames.length; j++) {
                    m.paramNames[j] = handleReplace(ABCSimpleUsageDetector.ItemKind.STRING, m.paramNames[j], replaceMap);
                }
            }
            if (m.flagHas_optional()) {
                for (int j = 0; j < m.optional.length; j++) {
                    m.optional[j].value_index = handleReplaceValueKind(abc, m.optional[j].value_kind, m.optional[j].value_index, replaceMap);
                }
            }

            for (int j = 0; j < m.param_types.length; j++) {
                m.param_types[j] = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, m.param_types[j], replaceMap);
            }

            m.ret_type = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, m.ret_type, replaceMap);
        }

        for (int i = 0; i < abc.metadata_info.size(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.METADATAINFO).contains(i)) {
                continue;
            }
            MetadataInfo m = abc.metadata_info.get(i);
            m.name_index = handleReplace(ABCSimpleUsageDetector.ItemKind.STRING, m.name_index, replaceMap);
            for (int j = 0; j < m.keys.length; j++) {
                m.keys[j] = handleReplace(ABCSimpleUsageDetector.ItemKind.STRING, m.keys[j], replaceMap);
            }
            for (int j = 0; j < m.values.length; j++) {
                m.values[j] = handleReplace(ABCSimpleUsageDetector.ItemKind.STRING, m.values[j], replaceMap);
            }
        }

        for (int i = 1; i < abc.constants.getMultinameCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.MULTINAME).contains(i)) {
                continue;
            }
            Multiname m = abc.constants.getMultiname(i);
            if (m.hasOwnName()) {
                m.name_index = handleReplace(ABCSimpleUsageDetector.ItemKind.STRING, m.name_index, replaceMap);
            }
            if (m.hasOwnNamespace()) {
                m.namespace_index = handleReplace(ABCSimpleUsageDetector.ItemKind.NAMESPACE, m.namespace_index, replaceMap);
            }
            if (m.hasOwnNamespaceSet()) {
                m.namespace_set_index = handleReplace(ABCSimpleUsageDetector.ItemKind.NAMESPACESET, m.namespace_set_index, replaceMap);
            }
            if (m.kind == Multiname.TYPENAME) {
                m.qname_index = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, m.qname_index, replaceMap);
                for (int j = 0; j < m.params.length; j++) {
                    m.params[j] = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, m.params[j], replaceMap);
                }
            }
        }

        for (int i = 1; i < abc.constants.getNamespaceCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.NAMESPACE).contains(i)) {
                continue;
            }
            Namespace m = abc.constants.getNamespace(i);
            m.name_index = handleReplace(ABCSimpleUsageDetector.ItemKind.STRING, m.name_index, replaceMap);
        }

        for (int i = 1; i < abc.constants.getNamespaceSetCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.MULTINAME).contains(i)) {
                continue;
            }
            NamespaceSet m = abc.constants.getNamespaceSet(i);
            for (int j = 0; j < m.namespaces.length; j++) {
                m.namespaces[j] = handleReplace(ABCSimpleUsageDetector.ItemKind.NAMESPACE, m.namespaces[j], replaceMap);
            }
        }

        for (int i = 0; i < abc.bodies.size(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.METHODBODY).contains(i)) {
                continue;
            }
            MethodBody m = abc.bodies.get(i);
            for (int j = 0; j < m.exceptions.length; j++) {
                m.exceptions[j].name_index = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, m.exceptions[j].name_index, replaceMap);
                m.exceptions[j].type_index = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, m.exceptions[j].type_index, replaceMap);
            }
            m.method_info = handleReplace(ABCSimpleUsageDetector.ItemKind.METHODINFO, m.method_info, replaceMap);
            walkTraits(abc, m.traits, replaceMap);
            AVM2Code acode = m.getCode();
            List<AVM2Instruction> code = acode.code;
            boolean bodyModified = false;
            for (int ip = 0; ip < code.size(); ip++) {
                AVM2Instruction ins = code.get(ip);
                for (int operandIndex = 0; operandIndex < ins.definition.operands.length; operandIndex++) {
                    int oldOperand = ins.operands[operandIndex];
                    switch (ins.definition.operands[operandIndex]) {
                        case AVM2Code.DAT_CLASS_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.CLASS, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_DECIMAL_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.DECIMAL, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_DOUBLE_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.DOUBLE, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_FLOAT_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.FLOAT, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_FLOAT4_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.FLOAT4, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_INT_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.INT, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_METHOD_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.METHODINFO, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_MULTINAME_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_NAMESPACE_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.NAMESPACE, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_STRING_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.STRING, ins.operands[operandIndex], replaceMap);
                            break;
                        case AVM2Code.DAT_UINT_INDEX:
                            ins.operands[operandIndex] = handleReplace(ABCSimpleUsageDetector.ItemKind.UINT, ins.operands[operandIndex], replaceMap);
                            break;                        
                    }
                    int newOperand = ins.operands[operandIndex];
                    if (oldOperand != newOperand) {
                        bodyModified = true;
                        int byteDelta = ABCOutputStream.getU30ByteLength(newOperand) - ABCOutputStream.getU30ByteLength(oldOperand);
                        if (byteDelta != 0) {
                            acode.updateInstructionByteCount(ip, byteDelta, m);
                        }
                    }
                }
            }
            if (bodyModified) {
                m.setModified();
            }
        }

        AVM2ConstantPool newCpool = new AVM2ConstantPool();
        for (int i = 1; i < abc.constants.getIntCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.INT).contains(i)) {
                continue;
            }
            newCpool.addInt(abc.constants.getInt(i));
        }
        for (int i = 1; i < abc.constants.getUIntCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.UINT).contains(i)) {
                continue;
            }
            newCpool.addUInt(abc.constants.getUInt(i));
        }
        for (int i = 1; i < abc.constants.getDoubleCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.DOUBLE).contains(i)) {
                continue;
            }
            newCpool.addDouble(abc.constants.getDouble(i));
        }
        
        if (abc.hasDecimalSupport()) {
            for (int i = 1; i < abc.constants.getDecimalCount(); i++) {
                if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.DECIMAL).contains(i)) {
                    continue;
                }
                newCpool.addDecimal(abc.constants.getDecimal(i));
            }
        }
        
        if (abc.hasFloatSupport()) {
            for (int i = 1; i < abc.constants.getFloatCount(); i++) {
                if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.FLOAT).contains(i)) {
                    continue;
                }
                newCpool.addFloat(abc.constants.getFloat(i));
            }
        }
        if (abc.hasFloat4Support()) {
            for (int i = 1; i < abc.constants.getFloat4Count(); i++) {
                if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.FLOAT4).contains(i)) {
                    continue;
                }
                newCpool.addFloat4(abc.constants.getFloat4(i));
            }
        }

        for (int i = 1; i < abc.constants.getStringCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.STRING).contains(i)) {
                continue;
            }
            newCpool.addString(abc.constants.getString(i));
        }

        for (int i = 1; i < abc.constants.getNamespaceCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.NAMESPACE).contains(i)) {
                continue;
            }
            newCpool.addNamespace(abc.constants.getNamespace(i));
        }

        for (int i = 1; i < abc.constants.getNamespaceSetCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.NAMESPACESET).contains(i)) {
                continue;
            }
            newCpool.addNamespaceSet(abc.constants.getNamespaceSet(i));
        }

        for (int i = 1; i < abc.constants.getMultinameCount(); i++) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.MULTINAME).contains(i)) {
                continue;
            }
            newCpool.addMultiname(abc.constants.getMultiname(i));
        }
        abc.constants = newCpool;

        for (int i = abc.metadata_info.size() - 1; i >= 0; i--) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.METADATAINFO).contains(i)) {
                abc.metadata_info.remove(i);
            }
        }
        for (int i = abc.bodies.size() - 1; i >= 0; i--) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.METHODBODY).contains(i)) {
                abc.bodies.remove(i);
            }
        }

        for (int i = abc.method_info.size() - 1; i >= 0; i--) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.METHODINFO).contains(i)) {
                abc.method_info.remove(i);
            }
        }

        for (int i = abc.instance_info.size() - 1; i >= 0; i--) {
            if (notReferencedIndices.get(ABCSimpleUsageDetector.ItemKind.CLASS).contains(i)) {
                abc.instance_info.remove(i);
                abc.class_info.remove(i);
            }
        }
        abc.clearAllCaches();
        try {
            abc.saveToStream(new NulStream()); //To recalculate dataSize
        } catch (IOException ex) {
            //ignore
        }
        if (abc.parentTag != null) {
            ((Tag) abc.parentTag).setModified(true);
        }
        abc.fireChanged();
    }

    private int handleReplace(ABCSimpleUsageDetector.ItemKind kind, int index, Map<ABCSimpleUsageDetector.ItemKind, Map<Integer, Integer>> replaceMap) {
        if (!replaceMap.get(kind).containsKey(index)) {
            return index;
        }
        return replaceMap.get(kind).get(index);
    }

    private int handleReplaceValueKind(ABC abc, int value_kind, int value_index, Map<ABCSimpleUsageDetector.ItemKind, Map<Integer, Integer>> replaceMap) {
        switch (value_kind) {
            case ValueKind.CONSTANT_Int:
                return handleReplace(ABCSimpleUsageDetector.ItemKind.INT, value_index, replaceMap);
            case ValueKind.CONSTANT_UInt:
                return handleReplace(ABCSimpleUsageDetector.ItemKind.UINT, value_index, replaceMap);
            case ValueKind.CONSTANT_Double:
                return handleReplace(ABCSimpleUsageDetector.ItemKind.DOUBLE, value_index, replaceMap);
            case ValueKind.CONSTANT_DecimalOrFloat:
                if (abc.hasFloatSupport()) {
                    return handleReplace(ABCSimpleUsageDetector.ItemKind.FLOAT, value_index, replaceMap);
                }
                if (abc.hasDecimalSupport()) {
                    return handleReplace(ABCSimpleUsageDetector.ItemKind.DECIMAL, value_index, replaceMap);
                }
                break;
            case ValueKind.CONSTANT_Float4:
                return handleReplace(ABCSimpleUsageDetector.ItemKind.FLOAT4, value_index, replaceMap);                
            case ValueKind.CONSTANT_Utf8:
                return handleReplace(ABCSimpleUsageDetector.ItemKind.STRING, value_index, replaceMap);
            case ValueKind.CONSTANT_Namespace:
            case ValueKind.CONSTANT_PackageNamespace:
            case ValueKind.CONSTANT_PackageInternalNs:
            case ValueKind.CONSTANT_ProtectedNamespace:
            case ValueKind.CONSTANT_ExplicitNamespace:
            case ValueKind.CONSTANT_StaticProtectedNs:
            case ValueKind.CONSTANT_PrivateNs:
                return handleReplace(ABCSimpleUsageDetector.ItemKind.NAMESPACE, value_index, replaceMap);
        }
        return value_index;
    }

    private void walkTraits(ABC abc, Traits traits, Map<ABCSimpleUsageDetector.ItemKind, Map<Integer, Integer>> replaceMap) {
        for (Trait t : traits.traits) {
            if ((t.kindFlags & Trait.ATTR_Metadata) > 0) {
                for (int i = 0; i < t.metadata.length; i++) {
                    t.metadata[i] = handleReplace(ABCSimpleUsageDetector.ItemKind.METADATAINFO, t.metadata[i], replaceMap);
                }
            }
            t.name_index = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, t.name_index, replaceMap);
            if (t instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
                tmgs.method_info = handleReplace(ABCSimpleUsageDetector.ItemKind.METHODINFO, tmgs.method_info, replaceMap);
            }
            if (t instanceof TraitFunction) {
                TraitFunction tf = (TraitFunction) t;
                tf.method_info = handleReplace(ABCSimpleUsageDetector.ItemKind.METHODINFO, tf.method_info, replaceMap);
            }
            if (t instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) t;
                tsc.type_index = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, tsc.type_index, replaceMap);
                tsc.value_index = handleReplaceValueKind(abc, tsc.value_kind, tsc.value_index, replaceMap);
            }
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                int classIndex = tc.class_info;
                InstanceInfo ii = abc.instance_info.get(classIndex);
                if ((ii.flags & InstanceInfo.CLASS_PROTECTEDNS) != 0) {
                    ii.protectedNS = handleReplace(ABCSimpleUsageDetector.ItemKind.NAMESPACE, ii.protectedNS, replaceMap);
                }
                ii.name_index = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, ii.name_index, replaceMap);
                ii.super_index = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, ii.super_index, replaceMap);
                for (int i = 0; i < ii.interfaces.length; i++) {
                    ii.interfaces[i] = handleReplace(ABCSimpleUsageDetector.ItemKind.MULTINAME, ii.interfaces[i], replaceMap);
                }
                ii.iinit_index = handleReplace(ABCSimpleUsageDetector.ItemKind.METHODINFO, ii.iinit_index, replaceMap);
                walkTraits(abc, ii.instance_traits, replaceMap);

                ClassInfo ci = abc.class_info.get(classIndex);
                ci.cinit_index = handleReplace(ABCSimpleUsageDetector.ItemKind.METHODINFO, ci.cinit_index, replaceMap);
                walkTraits(abc, ci.static_traits, replaceMap);

                tc.class_info = handleReplace(ABCSimpleUsageDetector.ItemKind.CLASS, tc.class_info, replaceMap);
            }
        }
    }
}
