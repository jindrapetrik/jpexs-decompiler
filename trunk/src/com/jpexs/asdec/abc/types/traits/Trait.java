/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types.traits;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.Namespace;
import com.jpexs.asdec.helpers.Helper;


public class Trait {

    public int name_index;
    public int kindType;
    public int kindFlags;
    public int metadata[] = new int[0];
    public long fileOffset;
    public byte bytes[];
    public static final int ATTR_Final = 0x1;
    public static final int ATTR_Override = 0x2;
    public static final int ATTR_Metadata = 0x4;

    public static final int TRAIT_SLOT = 0;
    public static final int TRAIT_METHOD = 1;
    public static final int TRAIT_GETTER = 2;
    public static final int TRAIT_SETTER = 3;
    public static final int TRAIT_CLASS = 4;
    public static final int TRAIT_FUNCTION = 5;
    public static final int TRAIT_CONST = 6;


    public String getModifiers(ConstantPool constants, boolean isStatic) {
        String ret = "";
        if ((kindFlags & ATTR_Override) > 0) {
            ret += "override";
        }
        Multiname m = getMultiName(constants);
        if (m != null) {
            Namespace ns = m.getNamespace(constants);
            if (ns != null) {
                ret += " " + ns.getPrefix(constants);
            }
        }
        if (isStatic)
            ret += " static";
        if ((kindFlags & ATTR_Final) > 0) {
            if (!isStatic) {
                ret += " final";
            }
        }
        return ret.trim();
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public String toString(ConstantPool constants) {
        return constants.constant_multiname[name_index].toString(constants) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public String convert(ConstantPool constants, MethodInfo[] methodInfo) {
        return convert(constants, methodInfo, false);
    }

    public String convert(ConstantPool constants, MethodInfo[] methodInfo, boolean isStatic) {
        return constants.constant_multiname[name_index].toString(constants) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public Multiname getMultiName(ConstantPool constants) {
        if (name_index == 0) {
            return null;
        } else {
            return constants.constant_multiname[name_index];
        }
    }
}
