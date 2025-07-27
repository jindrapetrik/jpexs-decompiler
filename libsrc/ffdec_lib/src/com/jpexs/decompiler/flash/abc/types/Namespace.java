/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.Set;

/**
 * Namespace in ABC file.
 *
 * @author JPEXS
 */
public class Namespace {

    /**
     * Minimum API mark value
     */
    public static final int MIN_API_MARK = 0xE000;

    /**
     * Maximum API mark value
     */
    public static final int MAX_API_MARK = 0xF8FF;

    /**
     * Namespace kind: Namespace
     */
    public static final int KIND_NAMESPACE = 8;

    /**
     * Namespace kind: Private namespace
     */
    public static final int KIND_PRIVATE = 5;

    /**
     * Namespace kind: Package namespace
     */
    public static final int KIND_PACKAGE = 22;

    /**
     * Namespace kind: Package internal namespace
     */
    public static final int KIND_PACKAGE_INTERNAL = 23;

    /**
     * Namespace kind: Protected namespace
     */
    public static final int KIND_PROTECTED = 24;

    /**
     * Namespace kind: Explicit namespace
     */
    public static final int KIND_EXPLICIT = 25;

    /**
     * Namespace kind: Static protected namespace
     */
    public static final int KIND_STATIC_PROTECTED = 26;

    /**
     * Namespace kinds
     */
    public static final int[] nameSpaceKinds = new int[]{KIND_NAMESPACE, KIND_PRIVATE, KIND_PACKAGE, KIND_PACKAGE_INTERNAL, KIND_PROTECTED, KIND_EXPLICIT, KIND_STATIC_PROTECTED};

    /**
     * Namespace kind names
     */
    public static final String[] nameSpaceKindNames = new String[]{"Namespace", "PrivateNamespace", "PackageNamespace", "PackageInternalNs", "ProtectedNamespace", "ExplicitNamespace", "StaticProtectedNs"};

    /**
     * Namespace prefixes
     */
    public static final String[] namePrefixes = new String[]{"", "private", "public", "internal", "protected", "explicit", "protected"};

    /**
     * Kind
     */
    public int kind;

    /**
     * Name index - index to string in constant pool
     */
    public int name_index;

    /**
     * Deleted flag
     */
    @Internal
    public boolean deleted;

    /**
     * Converts kind to string.
     *
     * @param kind Kind
     * @return Kind as string
     */
    public static String kindToStr(int kind) {
        for (int i = 0; i < nameSpaceKinds.length; i++) {
            if (nameSpaceKinds[i] == kind) {
                return nameSpaceKindNames[i];
            }
        }
        return null;
    }

    /**
     * Converts kind to prefix.
     *
     * @param kind Kind
     * @return Prefix
     */
    public static String kindToPrefix(int kind) {
        for (int i = 0; i < nameSpaceKinds.length; i++) {
            if (nameSpaceKinds[i] == kind) {
                return namePrefixes[i];
            }
        }
        return null;
    }

    /**
     * Constructs a new instance of Namespace.
     */
    public Namespace() {
    }

    /**
     * Constructs a new instance of Namespace.
     *
     * @param kind Kind
     * @param name_index Name index
     */
    public Namespace(int kind, int name_index) {
        this.kind = kind;
        this.name_index = name_index;
    }

    /**
     * Gets kind as string.
     *
     * @return Kind as string
     */
    public String getKindStr() {
        String kindStr = "?";
        for (int k = 0; k < nameSpaceKinds.length; k++) {
            if (nameSpaceKinds[k] == kind) {
                kindStr = nameSpaceKindNames[k];
                break;
            }
        }
        return kindStr;
    }

    /**
     * To string.
     *
     * @return String representation
     */
    @Override
    public String toString() {

        return "Namespace: kind=" + getKindStr() + " name_index=" + name_index;
    }

    /**
     * To string.
     *
     * @param usedDeobfuscations Used deobuscations
     * @param abc ABC
     * @param constants Constant pool
     * @return String representation
     */
    public String toString(Set<String> usedDeobfuscations, ABC abc, AVM2ConstantPool constants) {
        return getName(constants).toPrintableString(usedDeobfuscations, abc.getSwf(), true);
    }

    /**
     * Gets name with kind.
     *
     * @param constants Constant pool
     * @return Name with kind
     */
    public String getNameWithKind(AVM2ConstantPool constants) {
        String kindStr = getKindStr();
        String nameStr = constants.getString(name_index);
        return kindStr + (nameStr == null || nameStr.isEmpty() ? "" : " " + nameStr);
    }

    /**
     * Gets prefix.
     *
     * @return Prefix
     */
    public String getPrefix() {
        String kindStr = "?";
        for (int k = 0; k < nameSpaceKinds.length; k++) {
            if (nameSpaceKinds[k] == kind) {
                kindStr = namePrefixes[k];
                break;
            }
        }
        return kindStr;
    }

    /**
     * Gets prefix of a kind
     *
     * @param kind Kind
     * @return Prefix
     */
    public static String getPrefix(int kind) {
        String kindStr = "?";
        for (int k = 0; k < nameSpaceKinds.length; k++) {
            if (nameSpaceKinds[k] == kind) {
                kindStr = namePrefixes[k];
                break;
            }
        }
        return kindStr;
    }

    /**
     * Gets name.
     *
     * @param constants Constant pool
     * @return Name
     */
    public DottedChain getName(AVM2ConstantPool constants) {
        if (name_index == 0 || name_index == -1) {
            return DottedChain.EMPTY;
        }

        return constants.getDottedChain(name_index);
    }

    /**
     * Gets raw name.
     *
     * @param constants Constant pool
     * @return Raw name
     */
    public String getRawName(AVM2ConstantPool constants) {
        if (name_index == 0 || name_index == -1) {
            return ""; //??
        }

        return constants.getString(name_index);
    }

    /**
     * Checks if namespace has specific name.
     *
     * @param name Name to check
     * @param constants Constant pool
     * @return True if namespace has the name
     */
    public boolean hasName(String name, AVM2ConstantPool constants) {
        if (name == null && name_index == 0) {
            return true;
        }
        if (name == null) {
            return false;
        }
        if (name.isEmpty() && name_index == 0) {
            return true;
        }
        if (name_index == 0) {
            return false;
        }
        return constants.getString(name_index).equals(name);
    }
}
