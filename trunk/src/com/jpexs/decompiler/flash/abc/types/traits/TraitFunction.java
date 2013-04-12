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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DoABCDefineTag;
import java.util.List;
import java.util.Stack;

public class TraitFunction extends Trait {

    public int slot_index;
    public int method_info;

    @Override
    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        return "Function " + abc.constants.constant_multiname[name_index].toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_index + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
    }

    @Override
    public String convertHeader(String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int classIndex, boolean highlight, List<String> fullyQualifiedNames) {
        String modifier = getModifiers(abcTags, abc, isStatic) + " ";
        if (modifier.equals(" ")) {
            modifier = "";
        }
        MethodBody body = abc.findBody(method_info);
        return modifier + "function " + abc.constants.constant_multiname[name_index].getName(abc.constants, fullyQualifiedNames) + "(" + abc.method_info[method_info].getParamStr(abc.constants, body, abc, fullyQualifiedNames) + ") : " + abc.method_info[method_info].getReturnTypeStr(abc.constants, fullyQualifiedNames);
    }

    @Override
    public String convert(String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int classIndex, boolean highlight, List<String> fullyQualifiedNames) {
        String header = convertHeader(path, abcTags, abc, isStatic, pcode, classIndex, highlight, fullyQualifiedNames);
        String bodyStr = "";
        int bodyIndex = abc.findBodyIndex(method_info);
        if (bodyIndex != -1) {
            bodyStr = ABC.addTabs(abc.bodies[bodyIndex].toString(path + "." + abc.constants.constant_multiname[name_index].getName(abc.constants, fullyQualifiedNames), pcode, isStatic, classIndex, abc, abc.constants, abc.method_info, new Stack<GraphTargetItem>(), false, highlight, fullyQualifiedNames, null), 3);
        }
        return ABC.IDENT_STRING + ABC.IDENT_STRING + header + (abc.instance_info[classIndex].isInterface() ? ";" : " {\r\n" + bodyStr + "\r\n" + ABC.IDENT_STRING + ABC.IDENT_STRING + "}");

    }
}
