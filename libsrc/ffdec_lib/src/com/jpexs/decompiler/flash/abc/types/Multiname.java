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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Multiname in ABC file.
 *
 * @author JPEXS
 */
public class Multiname {

    /**
     * Qualified name kind
     */
    public static final int QNAME = 7;

    /**
     * Qualified name of attribute kind
     */
    public static final int QNAMEA = 0x0d;

    /**
     * Runtime qualified name kind
     */
    public static final int RTQNAME = 0x0f;

    /**
     * Runtime qualified name of attribute kind
     */
    public static final int RTQNAMEA = 0x10;

    /**
     * Runtime qualified name with late resolution kind
     */
    public static final int RTQNAMEL = 0x11;

    /**
     * Runtime qualified name of attribute with late resolution kind
     */
    public static final int RTQNAMELA = 0x12;

    /**
     * Multiname kind
     */
    public static final int MULTINAME = 0x09;

    /**
     * Multiname of attribute kind
     */
    public static final int MULTINAMEA = 0x0e;

    /**
     * Multiname with late resolution kind
     */
    public static final int MULTINAMEL = 0x1b;

    /**
     * Multiname of attribute with late resolution kind
     */
    public static final int MULTINAMELA = 0x1c;

    /**
     * Type name kind
     */
    public static final int TYPENAME = 0x1d;

    /**
     * Multiname kinds
     */
    private static final int[] multinameKinds = new int[]{QNAME, QNAMEA, MULTINAME, MULTINAMEA, RTQNAME, RTQNAMEA, MULTINAMEL, RTQNAMEL, RTQNAMELA, MULTINAMELA, TYPENAME};

    /**
     * Multiname kind names
     */
    private static final String[] multinameKindNames = new String[]{"QName", "QNameA", "Multiname", "MultinameA", "RTQName", "RTQNameA", "MultinameL", "RTQNameL", "RTQNameLA", "MultinameLA", "TypeName"};

    /**
     * Kind
     */
    public int kind;

    /**
     * Name index - index to string constant pool
     */
    public int name_index;

    /**
     * Namespace index - index to namespace constant pool
     */
    public int namespace_index;

    /**
     * Namespace set index - index to namespace set constant pool
     */
    public int namespace_set_index;

    /**
     * Qname index - index to multiname constant pool
     */
    public int qname_index; //for TypeName

    /**
     * Parameters - indexes to multiname constant pool
     */
    public int[] params; //for TypeName

    /**
     * Deleted flag
     */
    @Internal
    public boolean deleted;

    /**
     * Display namespace flag
     */
    @Internal
    private boolean displayNamespace = false;

    /**
     * Whether this typename is cyclic
     */
    @Internal
    private boolean cyclic = false;

    /**
     * Gets namespace suffix.
     *
     * @return Namespace suffix
     */
    public String getNamespaceSuffix() {
        if (displayNamespace) {
            return "#" + namespace_index;
        }
        return "";
    }

    /**
     * Sets display namespace flag.
     *
     * @param displayNamespace Display namespace flag
     */
    public void setDisplayNamespace(boolean displayNamespace) {
        this.displayNamespace = displayNamespace;
    }

    /**
     * Checks if the multiname kind is valid.
     *
     * @return True if the multiname kind is valid
     */
    private boolean validType() {
        boolean cnt = false;
        for (int i = 0; i < multinameKinds.length; i++) {
            if (multinameKinds[i] == kind) {
                cnt = true;
            }
        }
        return cnt;
    }

    /**
     * Constructs a new multiname.
     */
    public Multiname() {
        kind = -1;
        namespace_index = 0;
        namespace_set_index = 0;
        qname_index = 0;
        params = null;
    }

    /**
     * Constructs a new multiname.
     *
     * @param kind Kind
     * @param name_index Name index
     * @param namespace_index Namespace index
     * @param namespace_set_index Namespace set index
     * @param qname_index Qname index
     * @param params Parameters
     */
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

    /**
     * Checks if the multiname has its own name.
     *
     * @return True if the multiname has its own name
     */
    public boolean hasOwnName() {
        return kind == QNAME || kind == QNAMEA || kind == RTQNAME || kind == RTQNAMEA || kind == MULTINAME || kind == MULTINAMEA;
    }

    /**
     * Checks if the multiname has its own namespace.
     *
     * @return True if the multiname has its own namespace
     */
    public boolean hasOwnNamespace() {
        return kind == QNAME || kind == QNAMEA;
    }

    /**
     * Checks if the multiname has its own namespace set.
     *
     * @return True if the multiname has its own namespace set
     */
    public boolean hasOwnNamespaceSet() {
        return kind == MULTINAME || kind == MULTINAMEA || kind == MULTINAMEL || kind == MULTINAMELA;
    }

    /**
     * Creates a new QName.
     *
     * @param attribute Attribute flag
     * @param name_index Name index
     * @param namespace_index Namespace index
     * @return New QName
     */
    public static Multiname createQName(boolean attribute, int name_index, int namespace_index) {
        return new Multiname(attribute ? QNAMEA : QNAME, name_index, namespace_index, 0, 0, null);
    }

    /**
     * Creates a new RTQName.
     *
     * @param attribute Attribute flag
     * @param name_index Name index
     * @return New RTQName
     */
    public static Multiname createRTQName(boolean attribute, int name_index) {
        return new Multiname(attribute ? RTQNAMEA : RTQNAME, name_index, 0, 0, 0, null);
    }

    /**
     * Creates a new RTQNameL.
     *
     * @param attribute Attribute flag
     * @return New RTQNameL
     */
    public static Multiname createRTQNameL(boolean attribute) {
        return new Multiname(attribute ? RTQNAMELA : RTQNAMEL, 0, 0, 0, 0, null);
    }

    /**
     * Creates a new Multiname.
     *
     * @param attribute Attribute flag
     * @param name_index Name index
     * @param namespace_set_index Namespace set index
     * @return New Multiname
     */
    public static Multiname createMultiname(boolean attribute, int name_index, int namespace_set_index) {
        return new Multiname(attribute ? MULTINAMEA : MULTINAME, name_index, 0, namespace_set_index, 0, null);
    }

    /**
     * Creates a new MultinameL.
     *
     * @param attribute Attribute flag
     * @param namespace_set_index Namespace set index
     * @return New MultinameL
     */
    public static Multiname createMultinameL(boolean attribute, int namespace_set_index) {
        return new Multiname(attribute ? MULTINAMELA : MULTINAMEL, 0, 0, namespace_set_index, 0, null);
    }

    /**
     * Creates a new TypeName.
     *
     * @param qname_index Qname index
     * @param params Parameters
     * @return New TypeName
     */
    public static Multiname createTypeName(int qname_index, int[] params) {
        return new Multiname(TYPENAME, 0, 0, 0, qname_index, params);
    }

    /**
     * Checks if the typename is cyclic.
     *
     * @param constants Constant pool
     * @param name_index Type name index
     */
    public static void checkTypeNameCyclic(AVM2ConstantPool constants, int name_index) {
        Set<Integer> visited = new HashSet<>();
        if (name_index >= constants.getMultinameCount()) {
            return;
        }
        Multiname m = constants.getMultiname(name_index);
        if (m != null) {

            boolean cyclic = checkCyclicTypeNameSub(constants, name_index, visited);
            if (!m.cyclic && cyclic) {
                Logger.getLogger(AbcIndexing.class.getName()).log(Level.WARNING, "Recursive typename detected");
            }
            m.cyclic = cyclic;
        }
    }

    /**
     * Checks if the typename is cyclic.
     *
     * @param constants Constant pool
     * @param name_index Type name index
     * @param visited Visited set
     * @return True if the typename is cyclic
     */
    private static boolean checkCyclicTypeNameSub(AVM2ConstantPool constants, int name_index, Set<Integer> visited) {
        if (name_index >= constants.getMultinameCount()) {
            return false;
        }
        Multiname m = constants.getMultiname(name_index);
        if (m != null && m.kind == Multiname.TYPENAME) {
            if (visited.contains(name_index)) {
                return true;
            }
            visited.add(name_index);
            if (checkCyclicTypeNameSub(constants, m.qname_index, visited)) {
                return true;
            }
            for (int p : m.params) {
                if (checkCyclicTypeNameSub(constants, p, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the multiname is attribute.
     *
     * @return True if the multiname is attribute
     */
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

    /**
     * Checks if the multiname is runtime
     *
     * @return True if the multiname is runtime
     */
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

    /**
     * Checks if the multiname requires name on stack.
     *
     * @return True if the multiname requires name on stack
     */
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

    /**
     * Checks if the multiname requires namespace on stack.
     *
     * @return True if the multiname requires namespace on stack
     */
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

    /**
     * Gets the kind string
     *
     * @return Kind string
     */
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

    /**
     * Converts the multiname to string.
     *
     * @param constants Constant pool
     * @param index Index
     * @return Multiname as string
     */
    public static String namespaceToString(AVM2ConstantPool constants, int index) {
        if (index == 0) {
            return "null";
        }
        if (index >= constants.getNamespaceCount()) {
            return "Unknown(" + index + ")";
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
        String versionAdd = "";
        if (name != null && name.length() > 0) {
            char lastChar = name.charAt(name.length() - 1);

            if (lastChar >= Namespace.MIN_API_MARK && lastChar <= Namespace.MAX_API_MARK) {
                name = name.substring(0, name.length() - 1);
                versionAdd = String.format("\\u%04x", (int) lastChar);
            }
        }

        return constants.getNamespace(index).getKindStr() + "(" + (name == null ? "null" : "\""
                + Helper.escapePCodeString(name) + versionAdd
                + "\"") + (sub > 0 ? ",\"" + sub + "\"" : "") + ")";
    }

    /**
     * Converts the namespace set to string.
     *
     * @param constants Constant pool
     * @param index Index
     * @return Namespace set as string
     */
    public static String namespaceSetToString(AVM2ConstantPool constants, int index) {
        if (index == 0) {
            return "null";
        }
        if (index >= constants.getNamespaceSetCount()) {
            return "Unknown(" + index + ")";
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

    /**
     * Converts the multiname to string.
     *
     * @param constants Constant pool
     * @param index Index
     * @param fullyQualifiedNames Fully qualified names
     * @return Multiname as string
     */
    private static String multinameToString(AVM2ConstantPool constants, int index, List<DottedChain> fullyQualifiedNames) {
        if (index == 0) {
            return "null";
        }
        return constants.getMultiname(index).toString(constants, fullyQualifiedNames);
    }

    /**
     * Converts the multiname to string.
     *
     * @return Multiname as string
     */
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

    /**
     * Converts the multiname to string.
     *
     * @param constants Constant pool
     * @param fullyQualifiedNames Fully qualified names
     * @return Multiname as string
     */
    public String toString(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {

        switch (kind) {
            case QNAME:
            case QNAMEA:
                return getKindStr() + "(" + namespaceToString(constants, namespace_index) + "," + (name_index == 0 ? "null" : "\"" + Helper.escapePCodeString(constants.getString(name_index)) + "\"") + ")";
            case RTQNAME:
            case RTQNAMEA:
                return getKindStr() + "(" + (name_index == 0 ? "null" : "\"" + Helper.escapePCodeString(constants.getString(name_index))) + "\"" + ")";
            case RTQNAMEL:
            case RTQNAMELA:
                return getKindStr() + "()";
            case MULTINAME:
            case MULTINAMEA:
                return getKindStr() + "(" + (name_index == 0 ? "null" : "\"" + Helper.escapePCodeString(constants.getString(name_index)) + "\"") + "," + namespaceSetToString(constants, namespace_set_index) + ")";
            case MULTINAMEL:
            case MULTINAMELA:
                return getKindStr() + "(" + namespaceSetToString(constants, namespace_set_index) + ")";
            case TYPENAME:
                if (cyclic) {
                    return "CyclicTypeName";
                }
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

    /**
     * Converts the typename to string.
     *
     * @param constants Constant pool
     * @param fullyQualifiedNames Fully qualified names
     * @param dontDeobfuscate Don't deobfuscate flag
     * @param withSuffix With suffix flag
     * @return Typename as string
     */
    private String typeNameToStr(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames, boolean dontDeobfuscate, boolean withSuffix) {
        if (cyclic) {
            return "§§cyclic_typename()";
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

    /**
     * Gets the name with custom namespace.
     *
     * @param abc ABC
     * @param fullyQualifiedNames Fully qualified names
     * @param dontDeobfuscate Don't deobfuscate flag
     * @param withSuffix With suffix flag
     * @return Name with custom namespace
     */
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

            int nskind = getSimpleNamespaceKind(abc.constants);
            if (nskind == Namespace.KIND_NAMESPACE || nskind == Namespace.KIND_PACKAGE_INTERNAL) {
                DottedChain dc = abc.findCustomNsOfMultiname(this);
                String nsname = dc != null ? dc.getLast() : null;

                if (nsname != null && !"AS3".equals(nsname)) {
                    String identifier = dontDeobfuscate ? nsname : IdentifiersDeobfuscation.printIdentifier(true, nsname);
                    if (identifier != null && !identifier.isEmpty()) {
                        return nsname + "::" + name;
                    }
                }
            }

            if (nskind == Namespace.KIND_PACKAGE && fullyQualifiedNames != null && !fullyQualifiedNames.isEmpty() && fullyQualifiedNames.contains(DottedChain.parseWithSuffix(name))) {
                DottedChain dc = getNameWithNamespace(abc.constants, withSuffix);
                return dontDeobfuscate ? dc.toRawString() : dc.toPrintableString(true);
            }
            return (isAttribute() ? "@" : "") + (dontDeobfuscate ? name : IdentifiersDeobfuscation.printIdentifier(true, name)) + (withSuffix ? getNamespaceSuffix() : "");
        }
    }

    /**
     * Gets the name.
     *
     * @param constants Constant pool
     * @param fullyQualifiedNames Fully qualified names
     * @param dontDeobfuscate Don't deobfuscate flag
     * @param withSuffix With suffix flag
     * @return Name
     */
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
            Namespace ns = getNamespace(constants);
            boolean isPublic = false;
            if (ns == null) {
                NamespaceSet nss = getNamespaceSet(constants);
                if (nss != null) {
                    if (nss.namespaces.length == 1) {
                        ns = constants.getNamespace(nss.namespaces[0]);                        
                    }
                }
            }            
            if (ns != null && (ns.kind == Namespace.KIND_PACKAGE || ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                isPublic = true;
            } 
            if (isPublic && fullyQualifiedNames != null && !fullyQualifiedNames.isEmpty() && fullyQualifiedNames.contains(DottedChain.parseWithSuffix(name))) {
                DottedChain dc = getNameWithNamespace(constants, withSuffix);
                return dontDeobfuscate ? dc.toRawString() : dc.toPrintableString(true);
            }
            return (isAttribute() ? "@" : "") + (dontDeobfuscate ? name : IdentifiersDeobfuscation.printIdentifier(true, name)) + (withSuffix ? getNamespaceSuffix() : "");
        }
    }

    /**
     * Gets the name with namespace.
     *
     * @param constants Constant pool
     * @param withSuffix With suffix flag
     * @return Name with namespace
     */
    public DottedChain getNameWithNamespace(AVM2ConstantPool constants, boolean withSuffix) {
        DottedChain cached = constants.getCachedMultinameWithNamespace(this);
        if (cached != null) {
            return cached;
        }

        DottedChain nsName = getSimpleNamespaceName(constants);
        if (nsName == null) {
            Namespace ns = getNamespace(constants);
            if (ns == null) {
                NamespaceSet nss = getNamespaceSet(constants);
                if (nss != null) {
                    if (nss.namespaces.length == 1) {
                        ns = constants.getNamespace(nss.namespaces[0]);
                    }
                }
            }
            if (ns != null) {
                nsName = ns.getName(constants);
            }
        }
        String name = getName(constants, null, true, false);
        DottedChain ret;
        if (nsName != null) {
            ret = nsName.add(name, withSuffix ? getNamespaceSuffix() : "");
        } else {
            ret = new DottedChain(new String[]{name}, new String[]{withSuffix ? getNamespaceSuffix() : ""});
        }
        constants.cacheMultinameWithNamespace(this, ret);
        return ret;
    }

    /**
     * Gets the namespace.
     *
     * @param constants Constant pool
     * @return Namespace
     */
    public Namespace getNamespace(AVM2ConstantPool constants) {
        if ((namespace_index == 0) || (namespace_index == -1)) {
            return null;
        } else {
            return constants.getNamespace(namespace_index);
        }
    }

    /**
     * Gets simple namespace name as dottedchain. Ignores swf api versioning.
     *
     * @param constants Constant pool
     * @return Simple namespace name as dottedchain
     */
    public DottedChain getSimpleNamespaceName(AVM2ConstantPool constants) {
        if (hasOwnNamespace()) {
            if (namespace_index == 0 || namespace_index == -1) {
                return DottedChain.EMPTY;
            }
            return getNamespace(constants).getName(constants);
        }
        if (hasOwnNamespaceSet()) {
            NamespaceSet nss = getNamespaceSet(constants);
            if (nss == null) {
                return null;
            }
            return nss.getNonversionedName(constants);
        }
        return null;
    }

    /**
     * Gets simple namespace kind. Ignores swf api versioning.
     *
     * @param constants Constant pool
     * @return Simple namespace kind
     */
    public int getSimpleNamespaceKind(AVM2ConstantPool constants) {
        if (hasOwnNamespace()) {
            if (namespace_index == 0 || namespace_index == -1) {
                return 0;
            }
            return getNamespace(constants).kind;
        }
        if (hasOwnNamespaceSet()) {
            NamespaceSet nss = getNamespaceSet(constants);
            if (nss == null) {
                return 0;
            }
            return nss.getNonversionedKind(constants);
        }
        return 0;
    }

    /**
     * Gets the versions of this multiname for API versioned ABCs.
     *
     * @param constants Constant pool
     * @return Versions of this multiname.
     */
    public List<Integer> getApiVersions(AVM2ConstantPool constants) {
        if (hasOwnNamespace()) {
            return new ArrayList<>();
        }
        if (hasOwnNamespaceSet()) {
            return getNamespaceSet(constants).getApiVersions(constants);
        }
        return new ArrayList<>();
    }

    /**
     * Checks if this multiname is API versioned.
     *
     * @param constants Constant pool
     * @return True if this multiname is API versioned
     */
    public boolean isApiVersioned(AVM2ConstantPool constants) {
        if (hasOwnNamespace()) {
            return false;
        }
        if (hasOwnNamespaceSet()) {
            return getNamespaceSet(constants).isApiVersioned(constants);
        }
        return false;
    }

    /**
     * Gets the namespace set.
     *
     * @param constants Constant pool
     * @return Namespace set
     */
    public NamespaceSet getNamespaceSet(AVM2ConstantPool constants) {
        if (namespace_set_index == 0) {
            return null;
        } else if (namespace_set_index == -1) {
            return null;
        } else {
            return constants.getNamespaceSet(namespace_set_index);
        }
    }

    /**
     * Hash code.
     *
     * @return Hash code
     */
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

    /**
     * Equals.
     *
     * @param obj Object
     * @return True if the objects are equal
     */
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
     * used for interfaces.
     *
     * @param pool Constant pool
     * @return True if this MULTINAME kind with only one namespace
     */
    public boolean isMULTINAMEwithOneNs(AVM2ConstantPool pool) {
        return kind == MULTINAME && pool.getNamespaceSet(namespace_set_index).namespaces.length == 1;
    }

    /**
     * Gets single namespace index. It can be the namespace index of QNAME or
     * namespace index in namespace set of MULTINAME, if it has only one
     * namespace in the set.
     *
     * @param pool Constant pool
     * @return Single namespace index
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
     * @param pool Constant pool
     * @return Single namespace
     */
    public Namespace getSingleNamespace(AVM2ConstantPool pool) {
        int index = getSingleNamespaceIndex(pool);
        if (index < 0) {
            return null;
        }
        return pool.getNamespace(index);
    }

    /**
     * Checks if this multiname is effectively a QName. Effectively means that it
     * is a QName or QNameA or MULTINAME with only one namespace.
     *
     * @param thisCpool This constant pool
     * @return True if it's effectively a QName
     */
    private boolean isEffectivelyQname(AVM2ConstantPool thisCpool) {
        return kind == QNAME || kind == QNAMEA || isMULTINAMEwithOneNs(thisCpool);
    }

    /**
     * Checks if this qname effectively equals to other qname.
     *
     * @param thisCpool This constant pool
     * @param other Other qname
     * @param otherCpool Other constant pool
     * @return True if this qname effectively equals to other qname
     */
    public boolean qnameEquals(AVM2ConstantPool thisCpool, Multiname other, AVM2ConstantPool otherCpool) {
        if (!isEffectivelyQname(thisCpool) || !other.isEffectivelyQname(otherCpool)) {
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

    /**
     * Gets the cyclic flag.
     *
     * @return Cyclic flag
     */
    public boolean isCyclic() {
        return cyclic;
    }
}
