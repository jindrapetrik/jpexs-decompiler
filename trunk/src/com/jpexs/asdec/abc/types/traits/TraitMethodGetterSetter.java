/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types.traits;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.helpers.Helper;


public class TraitMethodGetterSetter extends Trait {

    public int disp_id; //compiler assigned value that helps overriding
    public int method_info;

    @Override
    public String toString(ConstantPool constants) {
        return "0x" + Helper.formatAddress(fileOffset) + " " + Helper.byteArrToString(bytes) + " MethodGetterSetter " + constants.constant_multiname[name_index].toString(constants) + " disp_id=" + disp_id + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
    }

    @Override
    public String convert(ConstantPool constants, MethodInfo[] methodInfo, boolean isStatic) {
        String modifier = getModifiers(constants, isStatic) + " ";
        if (modifier.equals(" ")) modifier = "";
        String addKind = "";
        if (kindType == TRAIT_GETTER) addKind = "get ";
        if (kindType == TRAIT_SETTER) addKind = "set ";
        return modifier + "function " + addKind + getMethodName(constants) + "(" + methodInfo[method_info].getParamStr(constants) + ") : " + methodInfo[method_info].getReturnTypeStr(constants);
    }


    public String getMethodName(ConstantPool constants) {
        return constants.constant_multiname[name_index].getName(constants);
    }


}
