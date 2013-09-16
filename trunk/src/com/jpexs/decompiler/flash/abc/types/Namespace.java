/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;

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

    public String toString(ConstantPool constants) {
        return getName(constants); //getPrefix(constants)+" "+getName(constants);
    }

    public String getNameWithKind(ConstantPool constants) {
        String kindStr = getKindStr();
        String nameStr = constants.constant_string[name_index];
        return kindStr + (nameStr.equals("") ? "" : " " + nameStr);
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

    public String getName(ConstantPool constants) {
        if (name_index == 0) {
            return "-";
        }
        return constants.constant_string[name_index];
    }
}
