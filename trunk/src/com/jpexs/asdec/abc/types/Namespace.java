/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;


public class Namespace {

    public static final int nameSpaceKinds[] = new int[]{8, 5, 22, 23, 24, 25, 26};
    public static final String nameSpaceKindNames[] = new String[]{"Namespace", "PrivateNamespace", "PackageNamespace", "PackageInternalNamespace", "ProtectedNamespace", "ExplicitNamespace", "StaticProtectedNamespace"};
    public static final String namePrefixes[] = new String[]{"", "private", "public", "", "protected", "explicit", ""};

    public int kind;
    public int name_index;

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

    public String getPrefix(ConstantPool constants) {
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
        return constants.constant_string[name_index];
    }
}
