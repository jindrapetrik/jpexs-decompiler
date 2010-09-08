/*
 *  Copyright (C) 2010 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
