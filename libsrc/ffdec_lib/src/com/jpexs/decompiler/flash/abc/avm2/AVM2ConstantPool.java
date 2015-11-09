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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.abc.types.Decimal;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.HashArrayList;
import com.jpexs.helpers.utf8.Utf8PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AVM2ConstantPool implements Cloneable {

    private static final Logger logger = Logger.getLogger(AVM2ConstantPool.class.getName());

    private HashArrayList<Long> constant_int = new HashArrayList<>();

    private HashArrayList<Long> constant_uint = new HashArrayList<>();

    private HashArrayList<Double> constant_double = new HashArrayList<>();

    /* Only for some minor versions */
    private HashArrayList<Decimal> constant_decimal = new HashArrayList<>();

    private HashArrayList<String> constant_string = new HashArrayList<>();

    private HashArrayList<Namespace> constant_namespace = new HashArrayList<>();

    private HashArrayList<NamespaceSet> constant_namespace_set = new HashArrayList<>();

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

    public void ensureStringCapacity(int capacity) {
        constant_string.ensureCapacity(capacity);
        if (capacity > 0) {
            ensureDefault(constant_string);
        }
    }

    public synchronized int addInt(long value) {
        ensureDefault(constant_int);
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

    private int getStringId(String value) {
        return constant_string.indexOf(value);
    }

    private int getMultinameId(Multiname value) {
        return constant_multiname.indexOf(value);
    }

    public int getQnameId(String name, int namespaceKind, String namespaceName, boolean add) {
        return getMultinameId(new Multiname(Multiname.QNAME, getStringId(name, add), getNamespaceId(namespaceKind, namespaceName, 0, add), 0), add);
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

    private int getNamespaceSetId(NamespaceSet val) {
        loopi:
        for (int i = 1; i < constant_namespace_set.size(); i++) {
            NamespaceSet ts = constant_namespace_set.get(i);
            if (ts.namespaces.length != val.namespaces.length) {
                continue;
            }
            for (int j = 0; j < val.namespaces.length; j++) {
                boolean found = false;
                for (int k = 0; k < val.namespaces.length; k++) {
                    if (ts.namespaces[j] == val.namespaces[k]) {
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

    public int getNamespaceSetId(NamespaceSet val, boolean add) {
        int id = getNamespaceSetId(val);
        if (add && id == -1) {
            id = addNamespaceSet(val);
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

    public DottedChain getDottedChain(int index) {
        String str = getString(index);
        DottedChain chain = dottedChainCache.get(str);
        if (chain == null) {
            chain = DottedChain.parse(str);
            dottedChainCache.put(str, chain);
        }

        return chain;
    }

    public void dump(Utf8PrintWriter writer) {
        String s = "";
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
}
