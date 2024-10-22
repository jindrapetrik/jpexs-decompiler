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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.abc.ABCVersionRequirements;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import macromedia.asc.util.Decimal128;

/**
 * AVM2 constant pool.
 *
 * @author JPEXS
 */
public class AVM2ConstantPool implements Cloneable {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(AVM2ConstantPool.class.getName());

    /**
     * Constant pool of integers
     */
    @SWFField
    private HashArrayList<Integer> constant_int = new HashArrayList<>();

    /**
     * Constant pool of unsigned integers
     */
    @SWFField
    private HashArrayList<Long> constant_uint = new HashArrayList<>();

    /**
     * Constant pool of doubles
     */
    @SWFField
    private HashArrayList<Double> constant_double = new HashArrayList<>();

    /**
     * Constant pool of decimals
     */
    @SWFField
    @ABCVersionRequirements(exactMinor = 17)
    private HashArrayList<Decimal128> constant_decimal = new HashArrayList<>();

    /**
     * Constant pool of floats
     */
    @SWFField
    @SWFVersion(from = 16)
    @ABCVersionRequirements(minMajor = 47, minMinor = 16)
    private HashArrayList<Float> constant_float = new HashArrayList<>();

    /**
     * Constant pool of float4
     */
    @SWFField
    @SWFVersion(from = 16)
    @ABCVersionRequirements(minMajor = 47, minMinor = 16)
    private HashArrayList<Float4> constant_float4 = new HashArrayList<>();

    /**
     * Constant pool of strings
     */
    @SWFField
    private HashArrayList<String> constant_string = new HashArrayList<>();

    /**
     * Constant pool of namespaces
     */
    @SWFField
    private HashArrayList<Namespace> constant_namespace = new HashArrayList<>();

    /**
     * Constant pool of namespace sets
     */
    @SWFField
    private HashArrayList<NamespaceSet> constant_namespace_set = new HashArrayList<>();

    /**
     * Constant pool of multinames
     */
    @SWFField
    private HashArrayList<Multiname> constant_multiname = new HashArrayList<>();

    /**
     * Constructs a new AVM2 constant pool.
     */
    public AVM2ConstantPool() {
    }

    /**
     * Cache of string to dotted chain conversion.
     */
    @Internal
    public Map<String, DottedChain> dottedChainCache = new HashMap<>();

    /**
     * Cache of multiname namespace to dotted chain conversion.
     */
    @Internal
    public Map<Multiname, DottedChain> multinameWithNamespaceCache = new HashMap<>();

    /**
     * Gets cached multiname with namespace dottedchain.
     *
     * @param multiName Multiname
     * @return Dotted chain
     */
    public DottedChain getCachedMultinameWithNamespace(Multiname multiName) {
        return multinameWithNamespaceCache.get(multiName);
    }

    /**
     * Caches multiname with namespace dotted chain.
     *
     * @param multiName Multiname
     * @param multinameWithNamespace Dotted chain
     */
    public void cacheMultinameWithNamespace(Multiname multiName, DottedChain multinameWithNamespace) {
        multinameWithNamespaceCache.put(multiName, multinameWithNamespace);
    }

    /**
     * Clears cached multinames.
     */
    public void clearCachedMultinames() {
        multinameWithNamespaceCache.clear();
    }

    /**
     * Gets cached dotted chains.
     */
    public void clearCachedDottedChains() {
        dottedChainCache.clear();
    }

    /**
     * Ensures that list has at least one - null - element.
     *
     * @param list List
     */
    private void ensureDefault(List<?> list) {
        if (list.isEmpty()) {
            list.add(null);
        }
    }

    /**
     * Ensures that list of integers has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureIntCapacity(int capacity) {
        constant_int.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_int);
        }
    }

    /**
     * Ensures that list of namespaces has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureNamespaceCapacity(int capacity) {
        constant_namespace.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_namespace);
        }
    }

    /**
     * Ensures that list of namespace sets has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureNamespaceSetCapacity(int capacity) {
        constant_namespace_set.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_namespace_set);
        }
    }

    /**
     * Ensures that list of multinames has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureMultinameCapacity(int capacity) {
        constant_multiname.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_multiname);
        }
    }

    /**
     * Ensures that list of unsigned integers has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureUIntCapacity(int capacity) {
        constant_uint.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_uint);
        }
    }

    /**
     * Ensures that list of doubles has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureDoubleCapacity(int capacity) {
        constant_double.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_double);
        }
    }

    /**
     * Ensures that list of decimals has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureDecimalCapacity(int capacity) {
        constant_decimal.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_decimal);
        }
    }

    /**
     * Ensures that list of floats has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureFloatCapacity(int capacity) {
        constant_float.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_float);
        }
    }

    /**
     * Ensures that list of float4 has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureFloat4Capacity(int capacity) {
        constant_float4.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_float4);
        }
    }

    /**
     * Ensures that list of strings has the initial capacity.
     *
     * @param capacity Capacity
     */
    public void ensureStringCapacity(int capacity) {
        constant_string.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_string);
        }
    }

    /**
     * Adds an integer to the constant pool.
     *
     * @param value Value
     * @return Index
     */
    public synchronized int addInt(int value) {
        ensureDefault(constant_int);
        value = (int) value;
        constant_int.add((Integer) value);
        return constant_int.size() - 1;
    }

    /**
     * Adds a namespace to the constant pool.
     *
     * @param ns Namespace
     * @return Index
     */
    public synchronized int addNamespace(Namespace ns) {
        ensureDefault(constant_namespace);
        constant_namespace.add(ns);
        return constant_namespace.size() - 1;
    }

    /**
     * Adds a namespace to the constant pool.
     *
     * @param kind Kind
     * @param nameIndex Name index
     * @return Index
     */
    public int addNamespace(int kind, int nameIndex) {
        return addNamespace(new Namespace(kind, nameIndex));
    }

    /**
     * Adds a namespace set to the constant pool.
     *
     * @param nss Namespace set
     * @return Index
     */
    public synchronized int addNamespaceSet(NamespaceSet nss) {
        ensureDefault(constant_namespace_set);
        constant_namespace_set.add(nss);
        return constant_namespace_set.size() - 1;
    }

    /**
     * Adds a multiname to the constant pool.
     *
     * @param m Multiname
     * @return Index
     */
    public synchronized int addMultiname(Multiname m) {
        ensureDefault(constant_multiname);
        constant_multiname.add(m);
        return constant_multiname.size() - 1;
    }

    /**
     * Adds an unsigned integer to the constant pool.
     *
     * @param value Value
     * @return Index
     */
    public synchronized int addUInt(long value) {
        ensureDefault(constant_uint);
        value &= 0xffffffffL;
        constant_uint.add(value);
        return constant_uint.size() - 1;
    }

    /**
     * Adds a double to the constant pool.
     *
     * @param value Value
     * @return Index
     */
    public synchronized int addDouble(double value) {
        ensureDefault(constant_double);
        constant_double.add(value);
        return constant_double.size() - 1;
    }

    /**
     * Adds a decimal to the constant pool.
     *
     * @param value Value
     * @return Index
     */
    public synchronized int addDecimal(Decimal128 value) {
        ensureDefault(constant_decimal);
        constant_decimal.add(value);
        return constant_decimal.size() - 1;
    }

    /**
     * Adds a float to the constant pool.
     *
     * @param value Value
     * @return Index
     */
    public synchronized int addFloat(Float value) {
        ensureDefault(constant_float);
        constant_float.add(value);
        return constant_float.size() - 1;
    }

    /**
     * Adds a float4 to the constant pool.
     *
     * @param value Value
     * @return Index
     */
    public synchronized int addFloat4(Float4 value) {
        ensureDefault(constant_float4);
        constant_float4.add(value);
        return constant_float4.size() - 1;
    }

    /**
     * Adds a string to the constant pool.
     *
     * @param value Value
     * @return Index
     */
    public synchronized int addString(String value) {
        ensureDefault(constant_string);
        constant_string.add(value);
        return constant_string.size() - 1;
    }

    /**
     * Sets an integer at the specified index.
     *
     * @param index Index
     * @param value Value
     * @return Value
     */
    public long setInt(int index, int value) {
        constant_int.set(index, value);
        return value;
    }

    /**
     * Sets a namespace at the specified index.
     *
     * @param index Index
     * @param ns Namespace
     * @return Namespace
     */
    public Namespace setNamespace(int index, Namespace ns) {
        constant_namespace.set(index, ns);
        return ns;
    }

    /**
     * Sets a namespace set at the specified index.
     *
     * @param index Index
     * @param nss Namespace set
     * @return Namespace set
     */
    public NamespaceSet setNamespaceSet(int index, NamespaceSet nss) {
        constant_namespace_set.set(index, nss);
        return nss;
    }

    /**
     * Sets a multiname at the specified index.
     *
     * @param index Index
     * @param m Multiname
     * @return Multiname
     */
    public Multiname setMultiname(int index, Multiname m) {
        constant_multiname.set(index, m);
        return m;
    }

    /**
     * Sets an unsigned integer at the specified index.
     *
     * @param index Index
     * @param value Value
     * @return Value
     */
    public long setUInt(int index, long value) {
        constant_uint.set(index, value);
        return value;
    }

    /**
     * Sets a double at the specified index.
     *
     * @param index Index
     * @param value Value
     * @return Value
     */
    public double setDouble(int index, double value) {
        constant_double.set(index, value);
        return value;
    }

    /**
     * Sets a decimal at the specified index.
     *
     * @param index Index
     * @param value Value
     * @return Value
     */
    public Decimal128 setDecimal(int index, Decimal128 value) {
        constant_decimal.set(index, value);
        return value;
    }

    /**
     * Sets a float at the specified index.
     *
     * @param index Index
     * @param value Value
     * @return Value
     */
    public float setFloat(int index, float value) {
        constant_float.set(index, value);
        return value;
    }

    /**
     * Sets a float4 at the specified index.
     *
     * @param index Index
     * @param value Value
     * @return Value
     */
    public Float4 setFloat4(int index, Float4 value) {
        constant_float4.set(index, value);
        return value;
    }

    /**
     * Sets a string at the specified index.
     *
     * @param index Index
     * @param value Value
     * @return Value
     */
    public String setString(int index, String value) {
        constant_string.set(index, value);
        return value;
    }

    /**
     * Gets an integer at the specified index.
     *
     * @param index Index
     * @return Value
     */
    public int getInt(int index) {
        if (index == 0) {
            return 0;
        }
        return constant_int.get(index);
    }

    /**
     * Gets a namespace at the specified index.
     *
     * @param index Index
     * @return Namespace
     */
    public Namespace getNamespace(int index) {
        return constant_namespace.get(index);
    }

    /**
     * Gets a namespace set at the specified index.
     *
     * @param index Index
     * @return Namespace set
     */
    public NamespaceSet getNamespaceSet(int index) {
        return constant_namespace_set.get(index);
    }

    /**
     * Converts kind MULTINAME with one namespace to QNAME with that namespace
     * (must exist in the abc). Ignores others.
     *
     * @param cpool Constant pool
     * @param index Multiname index
     * @return QName index
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

    /**
     * Gets a multiname at the specified index.
     *
     * @param index Index
     * @return Multiname
     */
    public Multiname getMultiname(int index) {
        return constant_multiname.get(index);
    }

    /**
     * Gets an unsigned integer at the specified index.
     *
     * @param index Index
     * @return Value
     */
    public long getUInt(int index) {
        if (index == 0) {
            return 0;
        }
        return constant_uint.get(index);
    }

    /**
     * Gets a double at the specified index.
     *
     * @param index Index
     * @return Value
     */
    public double getDouble(int index) {
        if (index == 0) {
            return 0;
        }
        return constant_double.get(index);
    }

    /**
     * Gets a decimal at the specified index.
     *
     * @param index Index
     * @return Value
     */
    public Decimal128 getDecimal(int index) {
        return constant_decimal.get(index);
    }

    /**
     * Gets a float at the specified index.
     *
     * @param index Index
     * @return Value
     */
    public Float getFloat(int index) {
        return constant_float.get(index);
    }

    /**
     * Gets a float4 at the specified index.
     *
     * @param index Index
     * @return Value
     */
    public Float4 getFloat4(int index) {
        return constant_float4.get(index);
    }

    /**
     * Gets a string at the specified index.
     *
     * @param index Index
     * @return Value
     */
    public String getString(int index) {
        return constant_string.get(index);
    }

    /**
     * Gets the number of integers in the constant pool.
     *
     * @return Count
     */
    public int getIntCount() {
        return constant_int.size();
    }

    /**
     * Gets the number of namespaces in the constant pool.
     *
     * @return Count
     */
    public int getNamespaceCount() {
        return constant_namespace.size();
    }

    /**
     * Gets the number of namespace sets in the constant pool.
     *
     * @return Count
     */
    public int getNamespaceSetCount() {
        return constant_namespace_set.size();
    }

    /**
     * Gets the number of multinames in the constant pool.
     *
     * @return Count
     */
    public int getMultinameCount() {
        return constant_multiname.size();
    }

    /**
     * Gets the number of unsigned integers in the constant pool.
     *
     * @return Count
     */
    public int getUIntCount() {
        return constant_uint.size();
    }

    /**
     * Gets the number of doubles in the constant pool.
     *
     * @return Count
     */
    public int getDoubleCount() {
        return constant_double.size();
    }

    /**
     * Gets the number of decimals in the constant pool.
     *
     * @return Count
     */
    public int getDecimalCount() {
        return constant_decimal.size();
    }

    /**
     * Gets the number of floats in the constant pool.
     *
     * @return Count
     */
    public int getFloatCount() {
        return constant_float.size();
    }

    /**
     * Gets the number of float4s in the constant pool.
     *
     * @return Count
     */
    public int getFloat4Count() {
        return constant_float4.size();
    }

    /**
     * Gets the number of strings in the constant pool.
     *
     * @return Count
     */
    public int getStringCount() {
        return constant_string.size();
    }

    /**
     * Gets sub index of namespace with the specified namespace id. The
     * namespaces that have the same name and kind are considered to be the
     * same, so they have assigned a sub index to distinguish them.
     *
     * @param namespaceId Namespace id
     * @return Sub index
     */
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

    /**
     * Gets the namespace id of the namespace with the specified kind, name
     * index and sub index.
     *
     * @param kind Kind
     * @param nameIndex Name index
     * @param index Sub index
     * @return Namespace id
     */
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

    /**
     * Gets the namespace id of the namespace with the specified kind, name and
     * sub index.
     *
     * @param kind Kind
     * @param nameIndex Name index
     * @param index Sub index
     * @param add Whether to add the namespace if it does not exist
     * @return Namespace id
     */
    public int getNamespaceId(int kind, int nameIndex, int index, boolean add) {
        int id = getNamespaceId(kind, nameIndex, index);
        if (add && id == -1) {
            id = addNamespace(kind, nameIndex);
        }
        return id;
    }

    /**
     * Gets the namespace id of the namespace with the specified kind, name and
     * sub index.
     *
     * @param kind Kind
     * @param name Name
     * @param index Sub index
     * @param add Whether to add the namespace if it does not exist
     * @return Namespace id
     */
    public int getNamespaceId(int kind, String name, int index, boolean add) {
        int nameIndex = getStringId(name, add);
        if (nameIndex == -1) {
            return -1;
        }

        return getNamespaceId(kind, nameIndex, index, add);
    }

    /**
     * Gets the namespace id of the namespace with the specified kind, name and
     * sub index.
     *
     * @param kind Kind
     * @param name Name
     * @param index Sub index
     * @param add Whether to add the namespace if it does not exist
     * @return Namespace id
     */
    public int getNamespaceId(int kind, DottedChain name, int index, boolean add) {
        int nameIndex = getStringId(name, add);
        if (nameIndex == -1) {
            return -1;
        }

        return getNamespaceId(kind, nameIndex, index, add);
    }

    /**
     * Gets decimal id.
     *
     * @param val Decimal
     * @param add Whether to add the decimal if it does not exist
     * @return Decimal id
     */
    public int getDecimalId(Decimal128 val, boolean add) {
        int id = getDecimalId(val);
        if (add && id == -1) {
            id = addDecimal(val);
        }
        return id;
    }

    /**
     * Gets decimal id.
     *
     * @param value Value
     * @return Decimal id
     */
    private int getDecimalId(Decimal128 value) {
        return constant_decimal.indexOf(value);
    }

    /**
     * Gets Qname id.
     *
     * @param name Name
     * @param namespaceKind Namespace kind
     * @param namespaceName Namespace name
     * @param add Whether to add the Qname if it does not exist
     * @return Qname id
     */
    public int getQnameId(String name, int namespaceKind, String namespaceName, boolean add) {
        return getMultinameId(Multiname.createQName(false, getStringId(name, add), getNamespaceId(namespaceKind, namespaceName, 0, add)), add);
    }

    /**
     * Gets public Qname id.
     *
     * @param name Name
     * @param add Whether to add the Qname if it does not exist
     * @return Qname id
     */
    public int getPublicQnameId(String name, boolean add) {
        return getQnameId(name, Namespace.KIND_PACKAGE, "", add);
    }

    /**
     * Gets Multiname id.
     *
     * @param value Multiname
     * @return Multiname id
     */
    private int getMultinameId(Multiname value) {
        return constant_multiname.indexOf(value);
    }

    /**
     * Gets Multiname id.
     *
     * @param val Multiname
     * @param add Whether to add the Multiname if it does not exist
     * @return Multiname id
     */
    public int getMultinameId(Multiname val, boolean add) {
        int id = getMultinameId(val);
        if (add && id == -1) {
            id = addMultiname(val);
        }
        return id;
    }

    /**
     * Finds multiname index based on multiname in other constant pool.
     *
     * @param val Value
     * @param origConst Original constant pool
     * @return List of multiname indexes
     */
    public List<Integer> getMultinameIds(Multiname val, AVM2ConstantPool origConst) {

        List<Integer> ret = new ArrayList<>();
        for (int i = 1; i < getMultinameCount(); i++) {
            if (getMultiname(i).qnameEquals(this, val, origConst)) {
                ret.add(i);
            }
        }
        return ret;
    }

    /**
     * Gets string id.
     *
     * @param value Value
     * @return String id
     */
    private int getStringId(String value) {
        return constant_string.indexOf(value);
    }

    /**
     * Gets string id.
     *
     * @param val Value
     * @param add Whether to add the string if it does not exist
     * @return String id
     */
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

    /**
     * Gets string id.
     *
     * @param val Value
     * @param add Whether to add the string if it does not exist
     * @return String id
     */
    public int getStringId(DottedChain val, boolean add) {
        if (val == null) {
            return 0;
        }

        return getStringId(val.toRawString(), add);
    }

    /**
     * Gets integer id.
     *
     * @param value Value
     * @return Integer id
     */
    private int getIntId(int value) {
        return constant_int.indexOf(value);
    }

    /**
     * Gets integer id.
     *
     * @param val Value
     * @param add Whether to add the integer if it does not exist
     * @return Integer id
     */
    public int getIntId(int val, boolean add) {
        int id = getIntId(val);
        if (add && id == -1) {
            id = addInt(val);
        }
        return id;
    }

    /**
     * Gets namespace set id.
     *
     * @param namespaces Namespaces
     * @return Namespace set id
     */
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

    /**
     * Gets namespace set id.
     *
     * @param namespaces Namespaces
     * @param add Whether to add the namespace set if it does not exist
     * @return Namespace set id
     */
    public int getNamespaceSetId(int[] namespaces, boolean add) {
        int id = getNamespaceSetId(namespaces);
        if (add && id == -1) {
            id = addNamespaceSet(new NamespaceSet(namespaces));
        }
        return id;
    }

    /**
     * Gets unsigned integer id.
     *
     * @param value Value
     * @return Unsigned integer id
     */
    private int getUIntId(long value) {
        return constant_uint.indexOf(value);
    }

    /**
     * Gets unsigned integer id.
     *
     * @param val Value
     * @param add Whether to add the unsigned integer if it does not exist
     * @return Unsigned integer id
     */
    public int getUIntId(long val, boolean add) {
        int id = getUIntId(val);
        if (add && id == -1) {
            id = addUInt(val);
        }
        return id;
    }

    /**
     * Gets double id.
     *
     * @param value Value
     * @return Double id
     */
    private int getDoubleId(double value) {
        return constant_double.indexOf(value);
    }

    /**
     * Gets double id.
     *
     * @param val Value
     * @param add Whether to add the double if it does not exist
     * @return Double id
     */
    public int getDoubleId(double val, boolean add) {
        int id = getDoubleId(val);
        if (add && id == -1) {
            id = addDouble(val);
        }
        return id;
    }

    /**
     * Gets float id.
     *
     * @param value Value
     * @return Float id
     */
    private int getFloatId(float value) {
        return constant_float.indexOf(value);
    }

    /**
     * Gets float id.
     *
     * @param val Value
     * @param add Whether to add the float if it does not exist
     * @return Float id
     */
    public int getFloatId(float val, boolean add) {
        int id = getFloatId(val);
        if (add && id == -1) {
            id = addFloat(val);
        }
        return id;
    }

    /**
     * Gets float4 id.
     *
     * @param value Value
     * @return Float4 id
     */
    private int getFloat4Id(Float4 value) {
        return constant_float4.indexOf(value);
    }

    /**
     * Gets float4 id.
     *
     * @param val Value
     * @param add Whether to add the float4 if it does not exist
     * @return Float4 id
     */
    public int getFloat4Id(Float4 val, boolean add) {
        int id = getFloat4Id(val);
        if (add && id == -1) {
            id = addFloat4(val);
        }
        return id;
    }

    /**
     * Gets the string at the specified index as dotted chain.
     *
     * @param index Index
     * @return Dotted chain
     */
    public DottedChain getDottedChain(int index) {
        String str = getString(index);
        DottedChain chain = dottedChainCache.get(str);
        if (chain == null) {
            chain = DottedChain.parseNoSuffix(str);
            dottedChainCache.put(str, chain);
        }

        return chain;
    }

    /**
     * Dumps the constant pool.
     *
     * @param writer Writer
     */
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

    /**
     * Converts multiname to string.
     *
     * @param index Multiname index
     * @return String
     */
    public String multinameToString(int index) {
        if (index == 0) {
            return "null";
        }
        return constant_multiname.get(index).toString(this, new ArrayList<>());
    }

    /**
     * Converts namespace to string.
     *
     * @param index Namespace index
     * @return String
     */
    public String namespaceToString(int index) {
        if (index == 0) {
            return "null";
        }
        return constant_namespace.get(index).toString(this);
    }

    /**
     * Converts namespace set to string.
     *
     * @param index Namespace set index
     * @return String
     */
    public String namespaceSetToString(int index) {
        if (index == 0) {
            return "null";
        }
        return constant_namespace_set.get(index).toString(this);
    }

    /**
     * Clones the constant pool.
     *
     * @return Cloned constant pool
     */
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
            ret.multinameWithNamespaceCache = new HashMap<>();
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }

    /**
     * Makes a push instruction for the specified value.
     *
     * @param ovalue Value
     * @return Instruction
     */
    public AVM2Instruction makePush(Object ovalue) {
        if (ovalue instanceof Integer) {
            int value = (Integer) ovalue;
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
     * Merges second constantpool into this one.
     *
     * @param secondPool Second constant pool
     * @param stringMap String map
     * @param intMap Integer map
     * @param uintMap Unsigned integer map
     * @param doubleMap Double map
     * @param floatMap Float map
     * @param float4Map Float4 map
     * @param decimalMap Decimal map
     * @param namespaceMap Namespace map
     * @param namespaceSetMap Namespace set map
     * @param multinameMap Multiname map
     */
    public void merge(AVM2ConstantPool secondPool, Map<Integer, Integer> stringMap, Map<Integer, Integer> intMap, Map<Integer, Integer> uintMap, Map<Integer, Integer> doubleMap, Map<Integer, Integer> floatMap, Map<Integer, Integer> float4Map, Map<Integer, Integer> decimalMap, Map<Integer, Integer> namespaceMap, Map<Integer, Integer> namespaceSetMap, Map<Integer, Integer> multinameMap) {
        stringMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_string.size(); i++) {
            String val = secondPool.constant_string.get(i);
            stringMap.put(i, getStringId(val, true));
        }
        intMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_int.size(); i++) {
            int val = secondPool.constant_int.get(i);
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
            Decimal128 val = secondPool.constant_decimal.get(i);
            decimalMap.put(i, getDecimalId(val, true));
        }
        namespaceMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_namespace.size(); i++) {
            Namespace secondNamespace = secondPool.constant_namespace.get(i);
            int mappedId;
            int newNameIndex = stringMap.get(secondNamespace.name_index);
            if (secondNamespace.kind == Namespace.KIND_PRIVATE) { //always add, this does not exists in this ABC. Conflicting private namespaces can have same names.
                mappedId = addNamespace(secondNamespace.kind, newNameIndex);
            } else {
                mappedId = getNamespaceId(secondNamespace.kind, newNameIndex, 0, true);
            }
            namespaceMap.put(i, mappedId);
        }
        namespaceSetMap.put(0, 0);
        for (int i = 1; i < secondPool.constant_namespace_set.size(); i++) {
            NamespaceSet secondNamespaceSet = secondPool.constant_namespace_set.get(i);
            int[] mappedsNss = new int[secondNamespaceSet.namespaces.length];
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
                    int[] newParams = new int[secondMultiname.params.length];
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

    /**
     * Checks cyclic type names. A type name is cyclic if it references itself
     * or any of its children.
     */
    public void checkCyclicTypeNames() {
        for (int i = 0; i < constant_multiname.size(); i++) {
            Multiname.checkTypeNameCyclic(this, i);
        }
    }
}
