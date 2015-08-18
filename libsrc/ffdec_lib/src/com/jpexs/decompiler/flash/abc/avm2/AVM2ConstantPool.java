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
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.utf8.Utf8PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AVM2ConstantPool implements Cloneable {

    private static final Logger logger = Logger.getLogger(AVM2ConstantPool.class.getName());

    public List<Long> constant_int = new ArrayList<>();

    public List<Long> constant_uint = new ArrayList<>();

    public List<Double> constant_double = new ArrayList<>();
    /* Only for some minor versions */

    public List<Decimal> constant_decimal = new ArrayList<>();

    public List<String> constant_string = new ArrayList<>();

    public List<Namespace> constant_namespace = new ArrayList<>();

    public List<NamespaceSet> constant_namespace_set = new ArrayList<>();

    public List<Multiname> constant_multiname = new ArrayList<>();

    public Map<String, DottedChain> dottedChainCache = new HashMap<>();

    public synchronized int addInt(long value) {
        constant_int.add(value);
        return constant_int.size() - 1;
    }

    public synchronized int addNamespace(Namespace ns) {
        constant_namespace.add(ns);
        return constant_namespace.size() - 1;
    }

    public synchronized int addNamespaceSet(NamespaceSet nss) {
        constant_namespace_set.add(nss);
        return constant_namespace_set.size() - 1;
    }

    public synchronized int addMultiname(Multiname m) {
        constant_multiname.add(m);
        return constant_multiname.size() - 1;
    }

    public synchronized int addUInt(long value) {
        constant_uint.add(value);
        return constant_uint.size() - 1;
    }

    public synchronized int addDouble(double value) {
        constant_double.add(value);
        return constant_double.size() - 1;
    }

    public synchronized int addDecimal(Decimal value) {
        constant_decimal.add(value);
        return constant_decimal.size() - 1;
    }

    public synchronized int addString(String value) {
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
            return constant_uint.get(index);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, "UInt not found. Index: " + index, ex);
        }
        return 0;
    }

    public double getDouble(int index) {
        try {
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

    public int getNamespaceId(Namespace val, int index) {
        for (int n = 1; n < constant_namespace.size(); n++) {
            Namespace ns = constant_namespace.get(n);
            if (ns.name_index == val.name_index && (ns.kind == val.kind)) {
                if (index == 0) {
                    return n;
                }
                index--;
            }
        }
        return 0;
    }

    public int getIntId(long value) {
        for (int i = 1; i < constant_int.size(); i++) {
            if (constant_int.get(i) == value) {
                return i;
            }
        }
        return 0;
    }

    public int getUIntId(long value) {
        for (int i = 1; i < constant_uint.size(); i++) {
            if (constant_uint.get(i) == value) {
                return i;
            }
        }
        return 0;
    }

    public int getDoubleId(double value) {
        for (int i = 1; i < constant_double.size(); i++) {
            if (Double.isNaN(value) && Double.isNaN(constant_double.get(i))) {
                return i;
            }
            if (Double.compare(constant_double.get(i), value) == 0) {
                return i;
            }
        }
        return 0;
    }

    public int getStringId(String val) {
        if (val == null) {
            return 0;
        }
        for (int i = 1; i < constant_string.size(); i++) {
            if (constant_string.get(i).equals(val)) {
                return i;
            }
        }
        return 0;
    }

    public int getMultinameId(Multiname val) {
        loopm:
        for (int m = 1; m < constant_multiname.size(); m++) {
            Multiname mul = constant_multiname.get(m);
            if (mul.kind == val.kind && mul.name_index == val.name_index && mul.namespace_index == val.namespace_index && mul.namespace_set_index == val.namespace_set_index && mul.qname_index == val.qname_index && mul.params.size() == val.params.size()) {
                for (int p = 0; p < mul.params.size(); p++) {
                    if (mul.params.get(p) != val.params.get(p)) {
                        continue loopm;
                    }
                }
                return m;
            }
        }
        return 0;
    }

    public int getQnameId(String name, int namespaceKind, String namespaceName, boolean add) {
        return getMultinameId(new Multiname(Multiname.QNAME, getStringId(name, add), getNamespaceId(new Namespace(namespaceKind, getStringId(namespaceName, add)), 0, add), 0, 0, new ArrayList<>()), add);
    }

    public int getPublicQnameId(String name, boolean add) {
        return getQnameId(name, Namespace.KIND_PACKAGE, "", add);
    }

    public int getMultinameId(Multiname val, boolean add) {
        int id = getMultinameId(val);
        if (add && id == 0) {
            id = addMultiname(val);
        }
        return id;
    }

    public int getStringId(String val, boolean add) {
        if (val == null) {
            return 0;
        }
        int id = getStringId(val);
        if (add && id == 0) {
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
        if (add && id == 0) {
            id = addInt(val);
        }
        return id;
    }

    public int getNamespaceId(Namespace val, int index, boolean add) {
        int id = getNamespaceId(val, index);
        if (add && id == 0) {
            id = addNamespace(val);
        }
        return id;
    }

    public int getNamespaceSetId(NamespaceSet val) {
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
        return 0;
    }

    public int getNamespaceSetId(NamespaceSet val, boolean add) {
        int id = getNamespaceSetId(val);
        if (add && id == 0) {
            id = addNamespaceSet(val);
        }
        return id;
    }

    public int getUIntId(long val, boolean add) {
        int id = getUIntId(val);
        if (add && id == 0) {
            id = addUInt(val);
        }
        return id;
    }

    public int getDoubleId(double val, boolean add) {
        int id = getDoubleId(val);
        if (add && id == 0) {
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
            ret.constant_int = new ArrayList<>(constant_int);
            ret.constant_uint = new ArrayList<>(constant_uint);
            ret.constant_double = new ArrayList<>(constant_double);
            ret.constant_decimal = new ArrayList<>(constant_decimal);
            ret.constant_string = new ArrayList<>(constant_string);
            ret.constant_namespace = new ArrayList<>(constant_namespace);
            ret.constant_namespace_set = new ArrayList<>(constant_namespace_set);
            ret.constant_multiname = new ArrayList<>(constant_multiname);
            ret.dottedChainCache = new HashMap<>();
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }
}
