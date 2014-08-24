/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;

public class InstanceInfo {

    public int name_index;
    public int super_index;
    public int flags; // 1 = sealed, 0 = dynamic, 2 = final, 4 = interface, 8 = ProtectedNs
    public int protectedNS; //if flags & 8
    public int[] interfaces;
    public int iinit_index; // MethodInfo - constructor
    public Traits instance_traits = new Traits();
    public static final int CLASS_SEALED = 1; //not dynamic
    public static final int CLASS_FINAL = 2;
    public static final int CLASS_INTERFACE = 4;
    public static final int CLASS_PROTECTEDNS = 8;

    public boolean deleted;

    @Override
    public String toString() {
        return "name_index=" + name_index + " super_index=" + super_index + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString();
    }

    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        String supIndexStr = "[nothing]";
        if (super_index > 0) {
            supIndexStr = abc.constants.getMultiname(super_index).toString(abc.constants, fullyQualifiedNames);
        }
        return "name_index=" + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " super_index=" + supIndexStr + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString(abc, fullyQualifiedNames);
    }

    public String getClassHeaderStr(ABC abc, List<String> fullyQualifiedNames) {
        String supIndexStr = "";
        if (super_index > 0) {
            supIndexStr = " extends " + abc.constants.getMultiname(super_index).getName(abc.constants, fullyQualifiedNames,false);////+" flags="+flags+" protectedNS="+protectedNS+" interfaces="+Helper.intArrToString(interfaces)+" method_index="+iinit_index
        }
        String implStr = "";
        if (interfaces.length > 0) {
            if (isInterface()) {
                implStr = " extends ";
            } else {
                implStr = " implements ";
            }
            for (int i = 0; i < interfaces.length; i++) {
                if (i > 0) {
                    implStr += ", ";
                }
                implStr += abc.constants.getMultiname(interfaces[i]).getName(abc.constants, fullyQualifiedNames,false);
            }
        }
        String modifiers;
        Namespace ns = abc.constants.getMultiname(name_index).getNamespace(abc.constants);
        modifiers = ns.getPrefix(abc);
        if (!modifiers.isEmpty()) {
            modifiers += " ";
        }

        if (isFinal()) {
            modifiers += "final ";
        }
        if (!isInterface() && isDynamic()) {
            modifiers += "dynamic ";
        }
        String objType = "class ";
        if (isInterface()) {
            objType = "interface ";
        }
        return modifiers + objType + abc.constants.getMultiname(name_index).getName(abc.constants, new ArrayList<String>()/* No full names here*/,false) + supIndexStr + implStr;
    }

    public Multiname getName(ConstantPool constants) {
        return constants.getMultiname(name_index);
    }

    public boolean isInterface() {
        return ((flags & CLASS_INTERFACE) == CLASS_INTERFACE);
    }

    public boolean isDynamic() {
        return (flags & CLASS_SEALED) == 0;
    }

    public boolean isFinal() {
        return (flags & CLASS_FINAL) == CLASS_FINAL;
    }
}
