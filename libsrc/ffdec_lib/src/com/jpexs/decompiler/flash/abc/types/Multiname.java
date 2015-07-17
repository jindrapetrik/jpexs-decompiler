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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.util.List;
import java.util.Objects;

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

    private static final String[] multinameKindNames = new String[]{"Qname", "QnameA", "Multiname", "MultinameA", "RTQname", "RTQnameA", "MultinameL", "RTQnameL", "RTQnameLA", "MultinameLA", "TypeName"};

    public final int kind;

    public int name_index;

    public int namespace_index;

    public final int namespace_set_index;

    public final int qname_index; //for TypeName

    public final List<Integer> params; //for TypeName

    @Internal
    public boolean deleted;

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

    public Multiname(int kind, int name_index, int namespace_index, int namespace_set_index, int qname_index, List<Integer> params) {
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
        return "kind=" + kindStr + " name_index=" + name_index + " namespace_index=" + namespace_index + " namespace_set_index=" + namespace_set_index + " qname_index=" + qname_index + " params_size:" + params.size();

    }

    private static String namespaceToString(AVM2ConstantPool constants, int index) {
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
                for (int i = 0; i < params.size(); i++) {
                    if (i > 0) {
                        tret += ",";
                    }
                    tret += multinameToString(constants, params.get(i), fullyQualifiedNames);
                }
                tret += ">";
                tret += ")";
                return tret;
        }
        return null;
    }

    private String typeNameToStr(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames, boolean raw) {
        if (constants.getMultiname(qname_index).name_index == name_index) {
            return "ambiguousTypeName";
        }
        StringBuilder typeNameStr = new StringBuilder();
        typeNameStr.append(constants.getMultiname(qname_index).getName(constants, fullyQualifiedNames, raw));
        if (!params.isEmpty()) {
            typeNameStr.append(".<");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    typeNameStr.append(",");
                }
                if (params.get(i) == 0) {
                    typeNameStr.append("*");
                } else {
                    typeNameStr.append(constants.getMultiname(params.get(i)).getName(constants, fullyQualifiedNames, raw));
                }
            }
            typeNameStr.append(">");
        }
        return typeNameStr.toString();
    }

    public String getName(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames, boolean raw) {
        if (kind == TYPENAME) {
            return typeNameToStr(constants, fullyQualifiedNames, raw);
        }
        if (name_index == -1) {
            return "";
        }
        if (name_index == 0) {
            return isAttribute() ? "@*" : "*";
        } else {
            String name = constants.getString(name_index);
            if (fullyQualifiedNames != null && fullyQualifiedNames.contains(DottedChain.parse(name))) {
                DottedChain dc = getNameWithNamespace(constants);
                return raw ? dc.toRawString() : dc.toPrintableString(true);
            }
            return (isAttribute() ? "@" : "") + (raw ? name : IdentifiersDeobfuscation.printIdentifier(true, name));
        }
    }

    public DottedChain getNameWithNamespace(AVM2ConstantPool constants) {
        Namespace ns = getNamespace(constants);
        String name = getName(constants, null, true);
        if (ns != null) {
            return ns.getName(constants).add(name);
        }
        return new DottedChain(name);
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
        if (!Objects.equals(params, other.params)) {
            return false;
        }
        return true;
    }
}
