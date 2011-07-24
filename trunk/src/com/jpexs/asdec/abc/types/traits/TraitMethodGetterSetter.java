/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.abc.types.traits;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.helpers.Helper;


public class TraitMethodGetterSetter extends Trait {

    public int disp_id; //compiler assigned value that helps overriding
    public int method_info;

    @Override
    public String toString(ABC abc) {
        return "0x" + Helper.formatAddress(fileOffset) + " " + Helper.byteArrToString(bytes) + " MethodGetterSetter " + abc.constants.constant_multiname[name_index].toString(abc.constants) + " disp_id=" + disp_id + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
    }

    @Override
    public String convert(ConstantPool constants, MethodInfo[] methodInfo,ABC abc, boolean isStatic) {
        String modifier = getModifiers(constants, isStatic) + " ";
        if (modifier.equals(" ")) modifier = "";
        String addKind = "";
        if (kindType == TRAIT_GETTER) addKind = "get ";
        if (kindType == TRAIT_SETTER) addKind = "set ";
        MethodBody body=abc.findBody(method_info);
        return modifier + "function " + addKind + getMethodName(constants) + "(" + methodInfo[method_info].getParamStr(constants,body,abc) + ") : " + methodInfo[method_info].getReturnTypeStr(constants);
    }


    public String getMethodName(ConstantPool constants) {
        return constants.constant_multiname[name_index].getName(constants);
    }


}
