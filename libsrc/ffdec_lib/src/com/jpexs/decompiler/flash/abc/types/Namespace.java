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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;

public class Namespace {

    public static final int KIND_NAMESPACE = 8;

    public static final int KIND_PRIVATE = 5;

    public static final int KIND_PACKAGE = 22;

    public static final int KIND_PACKAGE_INTERNAL = 23;

    public static final int KIND_PROTECTED = 24;

    public static final int KIND_EXPLICIT = 25;

    public static final int KIND_STATIC_PROTECTED = 26;

    public static final int[] nameSpaceKinds = new int[]{KIND_NAMESPACE, KIND_PRIVATE, KIND_PACKAGE, KIND_PACKAGE_INTERNAL, KIND_PROTECTED, KIND_EXPLICIT, KIND_STATIC_PROTECTED};

    public static final String[] nameSpaceKindNames = new String[]{"Namespace", "PrivateNamespace", "PackageNamespace", "PackageInternalNs", "ProtectedNamespace", "ExplicitNamespace", "StaticProtectedNs"};

    public static final String[] namePrefixes = new String[]{"", "private", "public", "", "protected", "explicit", "protected"};

    public int kind;

    public int name_index;

    @Internal
    public boolean deleted;

    public static String kindToStr(int kind) {
        for (int i = 0; i < nameSpaceKinds.length; i++) {
            if (nameSpaceKinds[i] == kind) {
                return nameSpaceKindNames[i];
            }
        }
        return null;
    }

    public static String kindToPrefix(int kind) {
        for (int i = 0; i < nameSpaceKinds.length; i++) {
            if (nameSpaceKinds[i] == kind) {
                return namePrefixes[i];
            }
        }
        return null;
    }

    public Namespace() {
    }

    public Namespace(int kind, int name_index) {
        this.kind = kind;
        this.name_index = name_index;
    }

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

    @Override
    public String toString() {

        return "Namespace: kind=" + getKindStr() + " name_index=" + name_index;
    }

    public String toString(AVM2ConstantPool constants) {
        return getName(constants).toPrintableString(true);
    }

    public String getNameWithKind(AVM2ConstantPool constants) {
        String kindStr = getKindStr();
        String nameStr = constants.getString(name_index);
        return kindStr + (nameStr.isEmpty() ? "" : " " + nameStr);
    }

    public String getPrefix(ABC abc) {
        String kindStr = "?";
        for (int k = 0; k < nameSpaceKinds.length; k++) {
            if (nameSpaceKinds[k] == kind) {
                kindStr = namePrefixes[k];
                break;
            }
        }
        return kindStr;
    }

    public DottedChain getName(AVM2ConstantPool constants) {
        if (name_index == 0) {
            return DottedChain.EMPTY;
        }

        return constants.getDottedChain(name_index);
    }

    public boolean hasName(String name, AVM2ConstantPool constants) {
        if (name == null && name_index == 0) {
            return true;
        }
        if (name == null) {
            return false;
        }
        if (name_index == 0) {
            return false;
        }
        return constants.getString(name_index).equals(name);
    }
}
