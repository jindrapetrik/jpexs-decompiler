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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class Multiname {

    public static final int QNAME = 7;

    public static final int QNAMEA = 0x0d;

    public static final int RTQNAME = 0x0f;

    public static final int RTQNAMEA = 0x10;

    public static final int RTQNAMEL = 0x11;

    public static final int RTQNAMELA = 0x12;

    public static final int MULTINAME = 0x09;

    public static final int MULTINAMEA = 0x0e;

    public static final int MULTINAMEL = 0x1b;

    public static final int MULTINAMELA = 0x1c;

    public static final int TYPENAME = 0x1d;

    private static final int[] multinameKinds = new int[]{QNAME, QNAMEA, MULTINAME, MULTINAMEA, RTQNAME, RTQNAMEA, MULTINAMEL, RTQNAMEL, RTQNAMELA, MULTINAMELA, TYPENAME};

    private static final String[] multinameKindNames = new String[]{"QName", "QNameA", "Multiname", "MultinameA", "RTQName", "RTQNameA", "MultinameL", "RTQNameL", "RTQNameLA", "MultinameLA", "TypeName"};

    public int kind;

    public int name_index;

    public int namespace_index;

    public /*final JAVA 9*/ int namespace_set_index;

    public /*final JAVA 9*/ int qname_index; //for TypeName

    public /*final JAVA 9*/ int[] params; //for TypeName

    @Internal
    public boolean deleted;

    @Internal
    private boolean displayNamespace = false;

    public String getNamespaceSuffix() {
        if (displayNamespace) {
            return "#" + namespace_index;
        }
        return "";
    }

    public void setDisplayNamespace(boolean displayNamespace) {
        this.displayNamespace = displayNamespace;
    }

    private boolean validType() {
        boolean cnt = false;
        for (int i = 0; i < multinameKinds.length; i++) {
            if (multinameKinds[i] == kind) {
                cnt = true;
            }
        }
        return cnt;
    }

    public Multiname() {
        kind = -1;
        namespace_index = 0;
        namespace_set_index = 0;
        qname_index = 0;
        params = null;
    }

    private Multiname(int kind, int name_index, int namespace_index, int namespace_set_index, int qname_index, int[] params) {
        this.kind = kind;
        this.name_index = name_index;
        this.namespace_index = namespace_index;
        this.namespace_set_index = namespace_set_index;
        this.qname_index = qname_index;
        this.params = params;
        if (!validType()) {
            throw new RuntimeException("Invalid multiname kind:" + kind);
        }
    }

    public boolean hasOwnName() {
        return kind == QNAME || kind == QNAMEA || kind == RTQNAME || kind == RTQNAMEA || kind == MULTINAME || kind == MULTINAMEA;
    }

    public boolean hasOwnNamespace() {
        return kind == QNAME || kind == QNAMEA;
    }

    public boolean hasOwnNamespaceSet() {
        return kind == MULTINAME || kind == MULTINAMEA || kind == MULTINAMEL || kind == MULTINAMELA;
    }

    public static Multiname createQName(boolean attribute, int name_index, int namespace_index) {
        return new Multiname(attribute ? QNAMEA : QNAME, name_index, namespace_index, 0, 0, null);
    }

    public static Multiname createRTQName(boolean attribute, int name_index) {
        return new Multiname(attribute ? RTQNAMEA : RTQNAME, name_index, 0, 0, 0, null);
    }

    public static Multiname createRTQNameL(boolean attribute) {
        return new Multiname(attribute ? RTQNAMELA : RTQNAMEL, 0, 0, 0, 0, null);
    }

    public static Multiname createMultiname(boolean attribute, int name_index, int namespace_set_index) {
        return new Multiname(attribute ? MULTINAMEA : MULTINAME, name_index, 0, namespace_set_index, 0, null);
    }

    public static Multiname createMultinameL(boolean attribute, int namespace_set_index) {
        return new Multiname(attribute ? MULTINAMELA : MULTINAMEL, 0, 0, namespace_set_index, 0, null);
    }

    public static Multiname createTypeName(int qname_index, int[] params) {
        return new Multiname(TYPENAME, 0, 0, 0, qname_index, params);
    }

    public boolean isAttribute() {
        if (kind == QNAMEA) {
            return true;
        }
        if (kind == MULTINAMEA) {
            return true;
        }
        if (kind == RTQNAMEA) {
            return true;
        }
        if (kind == RTQNAMELA) {
            return true;
        }
        if (kind == MULTINAMELA) {
            return true;
        }
        return false;
    }

    public boolean isRuntime() {
        if (kind == RTQNAME) {
            return true;
        }
        if (kind == RTQNAMEA) {
            return true;
        }
        if (kind == MULTINAMEL) {
            return true;
        }
        if (kind == MULTINAMELA) {
            return true;
        }
        return false;
    }

    public boolean needsName() {
        if (kind == RTQNAMEL) {
            return true;
        }
        if (kind == RTQNAMELA) {
            return true;
        }
        if (kind == MULTINAMEL) {
            return true;
        }
        if (kind == MULTINAMELA) {
            return true;
        }
        return false;
    }

    public boolean needsNs() {
        if (kind == RTQNAME) {
            return true;
        }
        if (kind == RTQNAMEA) {
            return true;
        }
        if (kind == RTQNAMEL) {
            return true;
        }
        if (kind == RTQNAMELA) {
            return true;
        }
        return false;
    }

    public String getKindStr() {
        String kindStr = "?";
        for (int k = 0; k < multinameKinds.length; k++) {
            if (multinameKinds[k] == kind) {
                kindStr = multinameKindNames[k];
                break;
            }
        }
        return kindStr;
    }

    @Override
    public String toString() {
        String kindStr = getKindStr();
        StringBuilder sb = new StringBuilder();
        sb.append("kind=").append(kindStr);
        sb.append(" name_index=").append(name_index);
        sb.append(" namespace_index=").append(namespace_index);
        sb.append(" namespace_set_index=").append(namespace_set_index);
        sb.append(" qname_index=").append(qname_index);
        sb.append(" params_size:");
        sb.append(params == null ? "null" : params.length);
        return sb.toString();

    }

    public static String namespaceToString(AVM2ConstantPool constants, int index) {
        if (index == 0) {
            return "null";
        }
        int type = constants.getNamespace(index).kind;
        int name_index = constants.getNamespace(index).name_index;
        String name = name_index == 0 ? null : constants.getNamespace(index).getName(constants).toRawString();
        int sub = -1;
        for (int n = 1; n < constants.getNamespaceCount(); n++) {
            if (constants.getNamespace(n).kind == type && constants.getNamespace(n).name_index == name_index) {
                sub++;
            }
            if (n == index) {
                break;
            }
        }
        return constants.getNamespace(index).getKindStr() + "(" + (name == null ? "null" : "\"" + Helper.escapeActionScriptString(name) + "\"") + (sub > 0 ? ",\"" + sub + "\"" : "") + ")";
    }

    private static String namespaceSetToString(AVM2ConstantPool constants, int index) {
        if (index == 0) {
            return "null";
        }
        StringBuilder ret = new StringBuilder();
        ret.append("[");
        for (int n = 0; n < constants.getNamespaceSet(index).namespaces.length; n++) {
            if (n > 0) {
                ret.append(",");
            }
            int ns = constants.getNamespaceSet(index).namespaces[n];
            ret.append(namespaceToString(constants, ns));
        }
        ret.append("]");
        return ret.toString();
    }

    private static String multinameToString(AVM2ConstantPool constants, int index, List<DottedChain> fullyQualifiedNames) {
        if (index == 0) {
            return "null";
        }
        return constants.getMultiname(index).toString(constants, fullyQualifiedNames);
    }

    public String toString(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {

        switch (kind) {
            case QNAME:
            case QNAMEA:
                return getKindStr() + "(" + namespaceToString(constants, namespace_index) + "," + (name_index == 0 ? "null" : "\"" + Helper.escapeActionScriptString(constants.getString(name_index)) + "\"") + ")";
            case RTQNAME:
            case RTQNAMEA:
                return getKindStr() + "(" + (name_index == 0 ? "null" : "\"" + Helper.escapeActionScriptString(constants.getString(name_index))) + "\"" + ")";
            case RTQNAMEL:
            case RTQNAMELA:
                return getKindStr() + "()";
            case MULTINAME:
            case MULTINAMEA:
                return getKindStr() + "(" + (name_index == 0 ? "null" : "\"" + Helper.escapeActionScriptString(constants.getString(name_index)) + "\"") + "," + namespaceSetToString(constants, namespace_set_index) + ")";
            case MULTINAMEL:
            case MULTINAMELA:
                return getKindStr() + "(" + namespaceSetToString(constants, namespace_set_index) + ")";
            case TYPENAME:
                String tret = getKindStr() + "(";
                tret += multinameToString(constants, qname_index, fullyQualifiedNames);
                tret += "<";
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        if (i > 0) {
                            tret += ",";
                        }
                        tret += multinameToString(constants, params[i], fullyQualifiedNames);
                    }
                }
                tret += ">";
                tret += ")";
                return tret;
        }
        return null;
    }

    private String typeNameToStr(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames, boolean dontDeobfuscate, boolean withSuffix) {
        if (constants.getMultiname(qname_index).name_index == name_index) {
            return "ambiguousTypeName";
        }
        StringBuilder typeNameStr = new StringBuilder();
        typeNameStr.append(constants.getMultiname(qname_index).getName(constants, fullyQualifiedNames, dontDeobfuscate, withSuffix));
        if (params != null && params.length > 0) {
            typeNameStr.append(".<");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    typeNameStr.append(",");
                }
                int param = params[i];
                if (param == 0) {
                    typeNameStr.append("*");
                } else {
                    typeNameStr.append(constants.getMultiname(param).getName(constants, fullyQualifiedNames, dontDeobfuscate, withSuffix));
                }
            }
            typeNameStr.append(">");
        }
        return typeNameStr.toString();
    }

    public String getNameWithCustomNamespace(ABC abc, List<DottedChain> fullyQualifiedNames, boolean dontDeobfuscate, boolean withSuffix) {
        if (kind == TYPENAME) {
            return typeNameToStr(abc.constants, fullyQualifiedNames, dontDeobfuscate, withSuffix);
        }
        if (name_index == -1) {
            return "";
        }
        if (name_index == 0) {
            return isAttribute() ? "@*" : "*";
        } else {
            String name = abc.constants.getString(name_index);

            if (namespace_index > 0 && getNamespace(abc.constants).kind == Namespace.KIND_NAMESPACE) {
                DottedChain dc = abc.findCustomNs(namespace_index);
                String nsname = dc != null ? dc.getLast() : null;

                if (nsname != null) {
                    String identifier = dontDeobfuscate ? nsname : IdentifiersDeobfuscation.printIdentifier(true, nsname);
                    if (identifier != null && !identifier.isEmpty()) {
                        return nsname + "::" + name;
                    }
                } else {
                    //???
                }
            }

            if (fullyQualifiedNames != null && fullyQualifiedNames.contains(DottedChain.parseWithSuffix(name))) {
                DottedChain dc = getNameWithNamespace(abc.constants, withSuffix);
                return dontDeobfuscate ? dc.toRawString() : dc.toPrintableString(true);
            }
            return (isAttribute() ? "@" : "") + (dontDeobfuscate ? name : IdentifiersDeobfuscation.printIdentifier(true, name)) + (withSuffix ? getNamespaceSuffix() : "");
        }
    }

    public String getName(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames, boolean dontDeobfuscate, boolean withSuffix) {
        if (kind == TYPENAME) {
            return typeNameToStr(constants, fullyQualifiedNames, dontDeobfuscate, withSuffix);
        }
        if (name_index == -1) {
            return "";
        }
        if (name_index == 0) {
            return isAttribute() ? "@*" : "*";
        } else {
            String name = constants.getString(name_index);
            if (fullyQualifiedNames != null && fullyQualifiedNames.contains(DottedChain.parseWithSuffix(name))) {
                DottedChain dc = getNameWithNamespace(constants, withSuffix);
                return dontDeobfuscate ? dc.toRawString() : dc.toPrintableString(true);
            }
            return (isAttribute() ? "@" : "") + (dontDeobfuscate ? name : IdentifiersDeobfuscation.printIdentifier(true, name)) + (withSuffix ? getNamespaceSuffix() : "");
        }
    }

    public DottedChain getNameWithNamespace(AVM2ConstantPool constants, boolean withSuffix) {
        Namespace ns = getNamespace(constants);
        if (ns == null) {
            NamespaceSet nss = getNamespaceSet(constants);
            if (nss != null) {
                if (nss.namespaces.length == 1) {
                    ns = constants.getNamespace(nss.namespaces[0]);
                }
            }
        }
        String name = getName(constants, null, true, false);
        if (ns != null) {
            return ns.getName(constants).add(name, withSuffix ? getNamespaceSuffix() : "");
        }
        return new DottedChain(new String[]{name}, withSuffix ? getNamespaceSuffix() : "");
    }

    public Namespace getNamespace(AVM2ConstantPool constants) {
        if ((namespace_index == 0) || (namespace_index == -1)) {
            return null;
        } else {
            return constants.getNamespace(namespace_index);
        }
    }

    public NamespaceSet getNamespaceSet(AVM2ConstantPool constants) {
        if (namespace_set_index == 0) {
            return null;
        } else if (namespace_set_index == -1) {
            return null;
        } else {
            return constants.getNamespaceSet(namespace_set_index);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + kind;
        hash = 53 * hash + name_index;
        hash = 53 * hash + namespace_index;
        hash = 53 * hash + namespace_set_index;
        hash = 53 * hash + qname_index;
        hash = 53 * hash + Objects.hashCode(params);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Multiname other = (Multiname) obj;
        if (this.kind != other.kind) {
            return false;
        }
        if (this.name_index != other.name_index) {
            return false;
        }
        if (this.namespace_index != other.namespace_index) {
            return false;
        }
        if (this.namespace_set_index != other.namespace_set_index) {
            return false;
        }
        if (this.qname_index != other.qname_index) {
            return false;
        }
        if (!Arrays.equals(params, other.params)) {
            return false;
        }
        return true;
    }

    /**
     * Is this MULTINAME kind with only one namespace. Hint: it is sometimes
     * used for interfaces
     *
     * @param pool
     * @return
     */
    public boolean isMULTINAMEwithOneNs(AVM2ConstantPool pool) {
        return kind == MULTINAME && pool.getNamespaceSet(namespace_set_index).namespaces.length == 1;
    }

    /**
     * Gets single namespace index. It can be the namespace index of QNAME or
     * namespace index in namespace set of MULTINAME, if it has only one
     * namespace in the set.
     *
     * @param pool
     * @return
     */
    public int getSingleNamespaceIndex(AVM2ConstantPool pool) {
        if (isMULTINAMEwithOneNs(pool)) {
            return pool.getNamespaceSet(namespace_set_index).namespaces[0];
        }
        return namespace_index;
    }

    /**
     * Gets single namespace. It can be the namespace of QNAME or namespace in
     * namespace set of MULTINAME, if it has only one namespace in the set.
     *
     * @param pool
     * @return
     */
    public Namespace getSingleNamespace(AVM2ConstantPool pool) {
        int index = getSingleNamespaceIndex(pool);
        if (index < 0) {
            return null;
        }
        return pool.getNamespace(index);
    }

    private boolean isEfectivelyQname(AVM2ConstantPool thisCpool) {
        return kind == QNAME || kind == QNAMEA || isMULTINAMEwithOneNs(thisCpool);
    }

    public boolean qnameEquals(AVM2ConstantPool thisCpool, Multiname other, AVM2ConstantPool otherCpool) {
        if (!isEfectivelyQname(thisCpool) || !other.isEfectivelyQname(otherCpool)) {
            return false;
        }
        Namespace otherNs = other.getSingleNamespace(otherCpool);
        Namespace thisNs = getSingleNamespace(thisCpool);
        if (thisNs != null && otherNs != null) {
            if (otherNs.kind != thisNs.kind) {
                return false;
            }
            if (otherNs.kind == Namespace.KIND_PRIVATE) {
                return false;
            }
            if (!Objects.equals(otherNs.getName(otherCpool).toRawString(), thisNs.getName(thisCpool).toRawString())) {
                return false;
            }
        }

        if ((thisNs == null && otherNs != null) || (otherNs == null && thisNs != null)) {
            return false;
        }

        if (!Objects.equals(other.getName(otherCpool, new ArrayList<>(), true, true), getName(thisCpool, new ArrayList<>(), true, true))) {
            return false;
        }

        return true;
    }
}
