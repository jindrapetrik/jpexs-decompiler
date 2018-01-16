/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.abc.ABCVersionRequirements;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.Decimal;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.HashArrayList;
import com.jpexs.helpers.utf8.Utf8PrintWriter;
import java.util.ArrayList;
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
public class AVM2ConstantPool implements Cloneable {

    private static final Logger logger = Logger.getLogger(AVM2ConstantPool.class.getName());

    @SWFField
    private HashArrayList<Long> constant_int = new HashArrayList<>();

    @SWFField
    private HashArrayList<Long> constant_uint = new HashArrayList<>();

    @SWFField
    private HashArrayList<Double> constant_double = new HashArrayList<>();

    @SWFField
    @ABCVersionRequirements(exactMinor = 17)
    private HashArrayList<Decimal> constant_decimal = new HashArrayList<>();

    @SWFField
    @SWFVersion(from = 16)
    @ABCVersionRequirements(minMajor = 47, minMinor = 16)
    private HashArrayList<Float> constant_float = new HashArrayList<>();

    /* Only for some versions */
    @SWFField
    @SWFVersion(from = 16)
    @ABCVersionRequirements(minMajor = 47, minMinor = 16)
    private HashArrayList<Float4> constant_float4 = new HashArrayList<>();

    @SWFField
    private HashArrayList<String> constant_string = new HashArrayList<>();

    @SWFField
    private HashArrayList<Namespace> constant_namespace = new HashArrayList<>();

    @SWFField
    private HashArrayList<NamespaceSet> constant_namespace_set = new HashArrayList<>();

    @SWFField
    private HashArrayList<Multiname> constant_multiname = new HashArrayList<>();

    public AVM2ConstantPool() {
    }

    @Internal
    public Map<String, DottedChain> dottedChainCache = new HashMap<>();

    private void ensureDefault(List<?> list) {
        if (list.isEmpty()) {
            list.add(null);
        }
    }

    public void ensureIntCapacity(int capacity) {
        constant_int.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_int);
        }
    }

    public void ensureNamespaceCapacity(int capacity) {
        constant_namespace.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_namespace);
        }
    }

    public void ensureNamespaceSetCapacity(int capacity) {
        constant_namespace_set.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_namespace_set);
        }
    }

    public void ensureMultinameCapacity(int capacity) {
        constant_multiname.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_multiname);
        }
    }

    public void ensureUIntCapacity(int capacity) {
        constant_uint.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_uint);
        }
    }

    public void ensureDoubleCapacity(int capacity) {
        constant_double.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_double);
        }
    }

    public void ensureDecimalCapacity(int capacity) {
        constant_decimal.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_decimal);
        }
    }

    public void ensureFloatCapacity(int capacity) {
        constant_float.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_float);
        }
    }

    public void ensureFloat4Capacity(int capacity) {
        constant_float4.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_float4);
        }
    }

    public void ensureStringCapacity(int capacity) {
        constant_string.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_string);
        }
    }

    public synchronized int addInt(long value) {
        ensureDefault(constant_int);
        value = (int) value;
        constant_int.add(value);
        return constant_int.size() - 1;
    }

    public synchronized int addNamespace(Namespace ns) {
        ensureDefault(constant_namespace);
        constant_namespace.add(ns);
        return constant_namespace.size() - 1;
    }

    public int addNamespace(int kind, int nameIndex) {
        return addNamespace(new Namespace(kind, nameIndex));
    }

    public synchronized int addNamespaceSet(NamespaceSet nss) {
        ensureDefault(constant_namespace_set);
        constant_namespace_set.add(nss);
        return constant_namespace_set.size() - 1;
    }

    public synchronized int addMultiname(Multiname m) {
        ensureDefault(constant_multiname);
        constant_multiname.add(m);
        return constant_multiname.size() - 1;
    }

    public synchronized int addUInt(long value) {
        ensureDefault(constant_uint);
        value &= 0xffffffffl;
        constant_uint.add(value);
        return constant_uint.size() - 1;
    }

    public synchronized int addDouble(double value) {
        ensureDefault(constant_double);
        constant_double.add(value);
        return constant_double.size() - 1;
    }

    public synchronized int addDecimal(Decimal value) {
        ensureDefault(constant_decimal);
        constant_decimal.add(value);
        return constant_decimal.size() - 1;
    }

    public synchronized int addFloat(Float value) {
        ensureDefault(constant_float);
        constant_float.add(value);
        return constant_float.size() - 1;
    }

    public synchronized int addFloat4(Float4 value) {
        ensureDefault(constant_float4);
        constant_float4.add(value);
        return constant_float4.size() - 1;
    }

    public synchronized int addString(String value) {
        ensureDefault(constant_string);
        constant_string.add(value);
        return constant_string.size() - 1;
    }

    public long setInt(int index, long value) {
        constant_int.set(index, value);
        return value;
    }

    public Namespace setNamespace(int index, Namespace ns) {
        constant_namespace.set(index, ns);
        return ns;
    }

    public NamespaceSet setNamespaceSet(int index, NamespaceSet nss) {
        constant_namespace_set.set(index, nss);
        return nss;
    }

    public Multiname setMultiname(int index, Multiname m) {
        constant_multiname.set(index, m);
        return m;
    }

    public long setUInt(int index, long value) {
        constant_uint.set(index, value);
        return value;
    }

    public double setDouble(int index, double value) {
        constant_double.set(index, value);
        return value;
    }

    public Decimal setDecimal(int index, Decimal value) {
        constant_decimal.set(index, value);
        return value;
    }

    public float setFloat(int index, float value) {
        constant_float.set(index, value);
        return value;
    }

    public Float4 setFloat4(int index, Float4 value) {
        constant_float4.set(index, value);
        return value;
    }

    public String setString(int index, String value) {
        constant_string.set(index, value);
        return value;
    }

    public long getInt(int index) {
        try {
            if (index == 0) {
                return 0;
            }
            return constant_int.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "Int not found. Index: " + index, ex);
        }
        return 0;
    }

    public Namespace getNamespace(int index) {
        try {
            return constant_namespace.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "Namespace not found. Index: " + index, ex);
        }
        return null;
    }

    public NamespaceSet getNamespaceSet(int index) {
        try {
            return constant_namespace_set.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "NamespaceSet not found. Index: " + index, ex);
        }
        return null;
    }

    /**
     * Converts kind MULTINAME with one namespace to QNAME with that namespace
     * (must exist in the abc). Ignores others.
     *
     * @param cpool
     * @param index MULTINAME index
     * @return
     */
    public int convertToQname(AVM2ConstantPool cpool, int index) {
        Multiname mx = cpool.getMultiname(index);
        if (mx.isMULTINAMEwithOneNs(cpool)) {
            Multiname mx2 = new Multiname();
            mx2.kind = Multiname.QNAME;
            mx2.name_index = mx.name_index;
            mx2.namespace_index = mx.getSingleNamespaceIndex(cpool);
            //and same QNAME exists within this ABC
            int newMultinameIndex = cpool.getMultinameId(mx2, false);
            if (newMultinameIndex > 0) {
                index = newMultinameIndex; //use that QNAME
            }
        }
        return index;
    }

    public Multiname getMultiname(int index) {
        try {
            return constant_multiname.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "Multiname not found. Index: " + index, ex);
        }
        return null;
    }

    public long getUInt(int index) {
        try {
            if (index == 0) {
                return 0;
            }
            return constant_uint.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "UInt not found. Index: " + index, ex);
        }
        return 0;
    }

    public double getDouble(int index) {
        try {
            if (index == 0) {
                return 0;
            }
            return constant_double.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "Double not found. Index: " + index, ex);
        }
        return 0;
    }

    public Decimal getDecimal(int index) {
        try {
            return constant_decimal.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "Decimal not found. Index: " + index, ex);
        }
        return null;
    }

    public int getDecimalId(Decimal val, boolean add) {
        int id = getDecimalId(val);
        if (add && id == -1) {
            id = addDecimal(val);
        }
        return id;
    }

    public Float getFloat(int index) {
        try {
            return constant_float.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "Float not found. Index: " + index, ex);
        }
        return null;
    }

    public Float4 getFloat4(int index) {
        try {
            return constant_float4.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "Float4 not found. Index: " + index, ex);
        }
        return null;
    }

    public String getString(int index) {
        try {
            return constant_string.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "String not found. Index: " + index, ex);
        }
        return null;
    }

    public int getIntCount() {
        return constant_int.size();
    }

    public int getNamespaceCount() {
        return constant_namespace.size();
    }

    public int getNamespaceSetCount() {
        return constant_namespace_set.size();
    }

    public int getMultinameCount() {
        return constant_multiname.size();
    }

    public int getUIntCount() {
        return constant_uint.size();
    }

    public int getDoubleCount() {
        return constant_double.size();
    }

    public int getDecimalCount() {
        return constant_decimal.size();
    }

    public int getFloatCount() {
        return constant_float.size();
    }

    public int getFloat4Count() {
        return constant_float4.size();
    }

    public int getStringCount() {
        return constant_string.size();
    }

    public int getNamespaceSubIndex(int namespaceId) {
        Namespace ns = constant_namespace.get(namespaceId);
        int index = 0;
        for (int n = 1; n < namespaceId; n++) {
            if (constant_namespace.get(n).name_index == ns.name_index && constant_namespace.get(n).kind == ns.kind) {
                index++;
            }
        }
        return index;
    }

    private int getNamespaceId(int kind, int nameIndex, int index) {
        for (int n = 1; n < constant_namespace.size(); n++) {
            Namespace ns = constant_namespace.get(n);
            if (ns.name_index == nameIndex && (ns.kind == kind)) {
                if (index == 0) {
                    return n;
                }
                index--;
            }
        }
        return -1;
    }

    public int getNamespaceId(int kind, int nameIndex, int index, boolean add) {
        int id = getNamespaceId(kind, nameIndex, index);
        if (add && id == -1) {
            id = addNamespace(kind, nameIndex);
        }
        return id;
    }

    public int getNamespaceId(int kind, String name, int index, boolean add) {
        int nameIndex = getStringId(name, add);
        if (nameIndex == -1) {
            return -1;
        }

        return getNamespaceId(kind, nameIndex, index, add);
    }

    public int getNamespaceId(int kind, DottedChain name, int index, boolean add) {
        int nameIndex = getStringId(name, add);
        if (nameIndex == -1) {
            return -1;
        }

        return getNamespaceId(kind, nameIndex, index, add);
    }

    private int getIntId(long value) {
        return constant_int.indexOf(value);
    }

    private int getUIntId(long value) {
        return constant_uint.indexOf(value);
    }

    private int getDoubleId(double value) {
        return constant_double.indexOf(value);
    }

    private int getFloatId(float value) {
        return constant_float.indexOf(value);
    }

    private int getFloat4Id(Float4 value) {
        return constant_float4.indexOf(value);
    }

    private int getDecimalId(Decimal value) {
        return constant_decimal.indexOf(value);
    }

    private int getStringId(String value) {
        return constant_string.indexOf(value);
    }

    private int getMultinameId(Multiname value) {
        return constant_multiname.indexOf(value);
    }

    public int getQnameId(String name, int namespaceKind, String namespaceName, boolean add) {
        return getMultinameId(Multiname.createQName(false, getStringId(name, add), getNamespaceId(namespaceKind, namespaceName, 0, add)), add);
    }

    public int getPublicQnameId(String name, boolean add) {
        return getQnameId(name, Namespace.KIND_PACKAGE, "", add);
    }

    public int getMultinameId(Multiname val, boolean add) {
        int id = getMultinameId(val);
        if (add && id == -1) {
            id = addMultiname(val);
        }
        return id;
    }

    /**
     * Finds multiname index based on multiname in other constant pool
     *
     * @param val
     * @param origConst
     * @return
     */
    public int getMultinameId(Multiname val, AVM2ConstantPool origConst) {

        for (int i = 1; i < getMultinameCount(); i++) {
            if (getMultiname(i).qnameEquals(this, val, origConst)) {
                return i;
            }
        }
        return -1;
    }

    public int getStringId(String val, boolean add) {
        if (val == null) {
            return 0;
        }
        int id = getStringId(val);
        if (add && id == -1) {
            id = addString(val);
        }
        return id;
    }

    public int getStringId(DottedChain val, boolean add) {
        if (val == null) {
            return 0;
        }

        return getStringId(val.toRawString(), add);
    }

    public int getIntId(long val, boolean add) {
        int id = getIntId(val);
        if (add && id == -1) {
            id = addInt(val);
        }
        return id;
    }

    private int getNamespaceSetId(int[] namespaces) {
        loopi:
        for (int i = 1; i < constant_namespace_set.size(); i++) {
            NamespaceSet ts = constant_namespace_set.get(i);
            if (ts.namespaces.length != namespaces.length) {
                continue;
            }
            for (int j = 0; j < namespaces.length; j++) {
                boolean found = false;
                for (int k = 0; k < namespaces.length; k++) {
                    if (ts.namespaces[j] == namespaces[k]) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue loopi;
                }
            }
            return i;
        }
        return -1;
    }

    public int getNamespaceSetId(int[] namespaces, boolean add) {
        int id = getNamespaceSetId(namespaces);
        if (add && id == -1) {
            id = addNamespaceSet(new NamespaceSet(namespaces));
        }
        return id;
    }

    public int getUIntId(long val, boolean add) {
        int id = getUIntId(val);
        if (add && id == -1) {
            id = addUInt(val);
        }
        return id;
    }

    public int getDoubleId(double val, boolean add) {
        int id = getDoubleId(val);
        if (add && id == -1) {
            id = addDouble(val);
        }
        return id;
    }

    public int getFloatId(float val, boolean add) {
        int id = getFloatId(val);
        if (add && id == -1) {
            id = addFloat(val);
        }
        return id;
    }

    public int getFloat4Id(Float4 val, boolean add) {
        int id = getFloat4Id(val);
        if (add && id == -1) {
            id = addFloat4(val);
        }
        return id;
    }

    public DottedChain getDottedChain(int index) {
        String str = getString(index);
        DottedChain chain = dottedChainCache.get(str);
        if (chain == null) {
            chain = DottedChain.parseWithSuffix(str);
            dottedChainCache.put(str, chain);
        }

        return chain;
    }

    public void dump(Utf8PrintWriter writer) {
        for (int i = 1; i < constant_int.size(); i++) {
            writer.println("INT[" + i + "]=" + constant_int.get(i));
        }
        for (int i = 1; i < constant_uint.size(); i++) {
            writer.println("UINT[" + i + "]=" + constant_uint.get(i));
        }
        for (int i = 1; i < constant_double.size(); i++) {
            writer.println("Double[" + i + "]=" + constant_double.get(i));
        }
        for (int i = 1; i < constant_string.size(); i++) {
            writer.println("String[" + i + "]=" + constant_string.get(i));
        }
        for (int i = 1; i < constant_namespace.size(); i++) {
            writer.println("Namespace[" + i + "]=" + constant_namespace.get(i).toString(this));
        }
        for (int i = 1; i < constant_namespace_set.size(); i++) {
            writer.println("NamespaceSet[" + i + "]=" + constant_namespace_set.get(i).toString(this));
        }

        for (int i = 1; i < constant_multiname.size(); i++) {
            writer.println("Multiname[" + i + "]=" + constant_multiname.get(i).toString(this, new ArrayList<>()));
        }
    }

    public String multinameToString(int index) {
        if (index == 0) {
            return "null";
        }
        return constant_multiname.get(index).toString(this, new ArrayList<>());
    }

    public String namespaceToString(int index) {
        if (index == 0) {
            return "null";
        }
        return constant_namespace.get(index).toString(this);
    }

    public String namespaceSetToString(int index) {
        if (index == 0) {
            return "null";
        }
        return constant_namespace_set.get(index).toString(this);
    }

    @Override
    public AVM2ConstantPool clone() {
        try {
            AVM2ConstantPool ret = (AVM2ConstantPool) super.clone();
            ret.constant_int = new HashArrayList<>(constant_int);
            ret.constant_uint = new HashArrayList<>(constant_uint);
            ret.constant_double = new HashArrayList<>(constant_double);
            ret.constant_decimal = new HashArrayList<>(constant_decimal);
            ret.constant_float = new HashArrayList<>(constant_float);
            ret.constant_float4 = new HashArrayList<>(constant_float4);
            ret.constant_string = new HashArrayList<>(constant_string);
            ret.constant_namespace = new HashArrayList<>(constant_namespace);
            ret.constant_namespace_set = new HashArrayList<>(constant_namespace_set);
            ret.constant_multiname = new HashArrayList<>(constant_multiname);
            ret.dottedChainCache = new HashMap<>();
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }

    public AVM2Instruction makePush(Object ovalue) {
        if (ovalue instanceof Long) {
            long value = (Long) ovalue;
            if (value >= -128 && value <= 127) {
                return new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{(int) (long) value});
            } else if (value >= -32768 && value <= 32767) {
                return new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{((int) (long) value) & 0xffff});
            } else {
                return new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{getIntId(value, true)});
            }
        }
        if (ovalue instanceof Double) {
            return new AVM2Instruction(0, AVM2Instructions.PushDouble, new int[]{getDoubleId((Double) ovalue, true)});
        }
        if (ovalue instanceof String) {
            return new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{getStringId((String) ovalue, true)});
        }
        if (ovalue instanceof Boolean) {
            if ((Boolean) ovalue) {
                return new AVM2Instruction(0, AVM2Instructions.PushTrue, null);
            }
            return new AVM2Instruction(0, AVM2Instructions.PushFalse, null);
        }
        if (ovalue == Null.INSTANCE) {
            return new AVM2Instruction(0, AVM2Instructions.PushNull, null);
        }
        if (ovalue == Undefined.INSTANCE) {
            return new AVM2Instruction(0, AVM2Instructions.PushUndefined, null);
        }
        return null;
    }

    /**
     * Merges second constantpool into this one
     *
     * @param secondPool
     * @param stringMap
     * @param intMap
     * @param uintMap
     * @param doubleMap
     * @param floatMap
     * @param float4Map
     * @param decimalMap
     * @param namespaceMap
     * @param namespaceSetMap
     * @param multinameMap
     */
    public void merge(AVM2ConstantPool secondPool, Map<Integer, Integer> stringMap, Map<Integer, Integer> intMap, Map<Integer, Integer> uintMap, Map<Integer, Integer> doubleMap, Map<Integer, Integer> floatMap, Map<Integer, Integer> float4Map, Map<Integer, Integer> decimalMap, Map<Integer, Integer> namespaceMap, Map<Integer, Integer> namespaceSetMap, Map<Integer, Integer> multinameMap) {
        stringMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_string.size(); i++) {
            String val = secondPool.constant_string.get(i);
            stringMap.put(i, getStringId(val, true));
        }
        intMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_int.size(); i++) {
            Long val = secondPool.constant_int.get(i);
            intMap.put(i, getIntId(val, true));
        }
        uintMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_uint.size(); i++) {
            Long val = secondPool.constant_uint.get(i);
            uintMap.put(i, getUIntId(val, true));
        }
        doubleMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_double.size(); i++) {
            Double val = secondPool.constant_double.get(i);
            doubleMap.put(i, getDoubleId(val, true));
        }
        floatMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_float.size(); i++) {
            Float val = secondPool.constant_float.get(i);
            floatMap.put(i, getFloatId(val, true));
        }
        float4Map.put(0, 0);
        for (int i = 1; i < secondPool.constant_float4.size(); i++) {
            Float4 val = secondPool.constant_float4.get(i);
            float4Map.put(i, getFloat4Id(val, true));
        }
        decimalMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_decimal.size(); i++) {
            Decimal val = secondPool.constant_decimal.get(i);
            decimalMap.put(i, getDecimalId(val, true));
        }
        namespaceMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_namespace.size(); i++) {
            Namespace secondNamespace = secondPool.constant_namespace.get(i);
            int mappedId;
            int newNameIndex = stringMap.get(secondNamespace.name_index);
            if (secondNamespace.kind == Namespace.KIND_PRIVATE) {//always add, this does not exists in this ABC. Conflicting private namespaces can have same names.
                mappedId = addNamespace(secondNamespace.kind, newNameIndex);
            } else {
                mappedId = getNamespaceId(secondNamespace.kind, newNameIndex, 0, true);
            }
            namespaceMap.put(i, mappedId);
        }
        namespaceSetMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_namespace_set.size(); i++) {
            NamespaceSet secondNamespaceSet = secondPool.constant_namespace_set.get(i);
            int mappedsNss[] = new int[secondNamespaceSet.namespaces.length];
            for (int n = 0; n < secondNamespaceSet.namespaces.length; n++) {
                mappedsNss[n] = namespaceMap.get(secondNamespaceSet.namespaces[n]);
            }
            int mappedId = getNamespaceSetId(mappedsNss, true);
            namespaceSetMap.put(i, mappedId);
        }
        multinameMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_multiname.size(); i++) {
            Multiname secondMultiname = secondPool.constant_multiname.get(i);
            Multiname newMultiname = null;
            int newNameIndex = secondMultiname.name_index <= 0 ? secondMultiname.name_index : stringMap.get(secondMultiname.name_index);
            int newNsIndex = secondMultiname.namespace_index <= 0 ? secondMultiname.namespace_index : namespaceMap.get(secondMultiname.namespace_index);
            int newNssIndex = secondMultiname.namespace_set_index <= 0 ? secondMultiname.namespace_set_index : namespaceSetMap.get(secondMultiname.namespace_set_index);

            switch (secondMultiname.kind) {
                case Multiname.MULTINAME:
                    newMultiname = Multiname.createMultiname(false, newNameIndex, newNssIndex);
                    break;
                case Multiname.MULTINAMEA:
                    newMultiname = Multiname.createMultiname(true, newNameIndex, newNssIndex);
                    break;
                case Multiname.MULTINAMEL:
                    newMultiname = Multiname.createMultinameL(false, newNssIndex);
                    break;
                case Multiname.MULTINAMELA:
                    newMultiname = Multiname.createMultinameL(true, newNssIndex);
                    break;
                case Multiname.QNAME:
                    newMultiname = Multiname.createQName(false, newNameIndex, newNsIndex);
                    break;
                case Multiname.QNAMEA:
                    newMultiname = Multiname.createQName(true, newNameIndex, newNsIndex);
                    break;
                case Multiname.RTQNAME:
                    newMultiname = Multiname.createRTQName(false, newNameIndex);
                    break;
                case Multiname.RTQNAMEA:
                    newMultiname = Multiname.createRTQName(true, newNameIndex);
                    break;
                case Multiname.RTQNAMEL:
                    newMultiname = Multiname.createRTQNameL(false);
                    break;
                case Multiname.RTQNAMELA:
                    newMultiname = Multiname.createRTQNameL(true);
                    break;
                case Multiname.TYPENAME:
                    int newQnameIndex = multinameMap.get(secondMultiname.qname_index);
                    int newParams[] = new int[secondMultiname.params.length];
                    for (int p = 0; p < secondMultiname.params.length; p++) {
                        newParams[p] = multinameMap.get(secondMultiname.params[p]);
                    }
                    newMultiname = Multiname.createTypeName(newQnameIndex, newParams);
                    break;
            }

            int mappedId = getMultinameId(newMultiname, true);
            multinameMap.put(i, mappedId);
        }

    }
}
