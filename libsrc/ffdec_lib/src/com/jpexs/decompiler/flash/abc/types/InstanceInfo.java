/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class InstanceInfo {

    public int name_index;

    public int super_index;

    public int flags; // 1 = sealed, 0 = dynamic, 2 = final, 4 = interface, 8 = ProtectedNs, 16 = non nullable

    public int protectedNS; //if flags & 8

    public int[] interfaces;

    public int iinit_index; // MethodInfo - constructor

    public Traits instance_traits;

    public static final int CLASS_SEALED = 1; //not dynamic

    public static final int CLASS_FINAL = 2;

    public static final int CLASS_INTERFACE = 4;

    public static final int CLASS_PROTECTEDNS = 8;

    public static final int CLASS_NON_NULLABLE = 16; //This is somehow used in Flex, propably through annotations or something with Vector datatype (?)

    @Internal
    public boolean deleted;

    public InstanceInfo() {
        instance_traits = new Traits();
    }

    public InstanceInfo(Traits traits) {
        instance_traits = traits;
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " super_index=" + super_index + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString();
    }

    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        String supIndexStr = "[nothing]";
        if (super_index > 0) {
            supIndexStr = abc.constants.getMultiname(super_index).toString(abc.constants, fullyQualifiedNames);
        }
        return "name_index=" + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " super_index=" + supIndexStr + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString(abc, fullyQualifiedNames);
    }

    public GraphTextWriter getClassHeaderStr(GraphTextWriter writer, ABC abc, List<DottedChain> fullyQualifiedNames, boolean allowPrivate) {
        String modifiers;
        Namespace ns = abc.constants.getMultiname(name_index).getNamespace(abc.constants);
        modifiers = ns.getPrefix(abc);
        if (!allowPrivate && modifiers.equals("private")) {
            modifiers = "";
        }
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

        writer.appendNoHilight(modifiers + objType);
        writer.hilightSpecial(abc.constants.getMultiname(name_index).getName(abc.constants, null/* No full names here*/, false, true), HighlightSpecialType.CLASS_NAME);

        if (super_index > 0) {
            String typeName = abc.constants.getMultiname(super_index).getNameWithNamespace(abc.constants, true).toRawString();
            String parentName = abc.constants.getMultiname(super_index).getName(abc.constants, fullyQualifiedNames, false, true);
            if (!parentName.equals("Object")) {
                writer.appendNoHilight(" extends ");
                writer.hilightSpecial(parentName, HighlightSpecialType.TYPE_NAME, typeName);
            }
        }
        if (interfaces.length > 0) {
            if (isInterface()) {
                writer.appendNoHilight(" extends ");
            } else {
                writer.appendNoHilight(" implements ");
            }
            for (int i = 0; i < interfaces.length; i++) {
                if (i > 0) {
                    writer.append(", ");
                }
                String typeName = abc.constants.getMultiname(interfaces[i]).getNameWithNamespace(abc.constants, true).toRawString();
                writer.hilightSpecial(abc.constants.getMultiname(interfaces[i]).getName(abc.constants, fullyQualifiedNames, false, true), HighlightSpecialType.TYPE_NAME, typeName);
            }
        }

        return writer;
    }

    public Multiname getName(AVM2ConstantPool constants) {
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

    public boolean isNullable() {
        return (flags & CLASS_NON_NULLABLE) != CLASS_NON_NULLABLE;
    }
}
