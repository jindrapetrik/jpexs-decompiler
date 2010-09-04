/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.types.traits.Traits;
import com.jpexs.asdec.helpers.Helper;


public class InstanceInfo {

    public int name_index;
    public int super_index;
    public int flags; // 1 = sealed, 0 = dynamic, 2 = final, 4 = interface, 8 = ProtectedNs
    public int protectedNS; //if flags & 8
    public int interfaces[];
    public int iinit_index; // MethodInfo - constructor
    public Traits instance_traits;

    public static final int CLASS_SEALED = 1; //not dynamic
    public static final int CLASS_FINAL = 2;
    public static final int CLASS_INTERFACE = 4;
    public static final int CLASS_PROTECTEDNS = 8;

    @Override
    public String toString() {
        return "name_index=" + name_index + " super_index=" + super_index + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString();
    }


    public String toString(ConstantPool constants) {
        String supIndexStr = "[nothing]";
        if (super_index > 0)
            constants.constant_multiname[super_index].toString(constants);
        return "name_index=" + constants.constant_multiname[name_index].toString(constants) + " super_index=" + supIndexStr + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString(constants);
    }

    public String getClassHeaderStr(ConstantPool constants) {
        String supIndexStr = "";
        if (super_index > 0)
            supIndexStr = " extends " + constants.constant_multiname[super_index].getName(constants);////+" flags="+flags+" protectedNS="+protectedNS+" interfaces="+Helper.intArrToString(interfaces)+" method_index="+iinit_index
        String modifiers = "";
        Namespace ns = constants.constant_multiname[name_index].getNamespace(constants);
        modifiers = ns.getPrefix(constants);
        if (!modifiers.equals("")) modifiers += " ";

        if ((flags & CLASS_FINAL) == CLASS_FINAL) {
            modifiers = "final ";
        }
        if ((flags & CLASS_SEALED) == 0) {
            modifiers = modifiers + "dynamic ";
        }
        String objType = "class ";
        if ((flags & CLASS_INTERFACE) == CLASS_INTERFACE) {
            objType = "interface ";
        }
        return modifiers + objType + constants.constant_multiname[name_index].getName(constants) + supIndexStr;
    }

    public String getInstanceVarsStr(ConstantPool constants) {
        return instance_traits.convert(constants, "\t");
    }

    public Multiname getName(ConstantPool constants) {
        return constants.constant_multiname[name_index];
    }

}
