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
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.Helper;
import java.util.List;
import java.util.Stack;

public class TraitMethodGetterSetter extends Trait {

    public int disp_id; //compiler assigned value that helps overriding
    public int method_info;

    @Override
    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        return "0x" + Helper.formatAddress(fileOffset) + " " + Helper.byteArrToString(bytes) + " MethodGetterSetter " + abc.constants.constant_multiname[name_index].toString(abc.constants, fullyQualifiedNames) + " disp_id=" + disp_id + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
    }

    @Override
    public String convertHeader(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int scriptIndex, int classIndex, boolean highlight, List<String> fullyQualifiedNames, boolean parallel) {
        String modifier = getModifiers(abcTags, abc, isStatic) + " ";
        if (modifier.equals(" ")) {
            modifier = "";
        }
        String addKind = "";
        if (kindType == TRAIT_GETTER) {
            addKind = "get ";
        }
        if (kindType == TRAIT_SETTER) {
            addKind = "set ";
        }
        MethodBody body = abc.findBody(method_info);

        if (((classIndex == -1) || (!abc.instance_info[classIndex].isInterface())) && (body == null)) {
            modifier = "native " + modifier;
        }

        return modifier + Highlighting.hilighSpecial(highlight, "function " + addKind, "traittype") + Highlighting.hilighSpecial(highlight, getName(abc).getName(abc.constants, fullyQualifiedNames), "traitname") + "(" + abc.method_info[method_info].getParamStr(highlight, abc.constants, body, abc, fullyQualifiedNames) + ") : " + abc.method_info[method_info].getReturnTypeStr(highlight, abc.constants, fullyQualifiedNames);

    }

    @Override
    public String convert(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int scriptIndex, int classIndex, boolean highlight, List<String> fullyQualifiedNames, boolean parallel) {
        String header = convertHeader(parent, path, abcTags, abc, isStatic, pcode, scriptIndex, classIndex, highlight, fullyQualifiedNames, parallel);
        String bodyStr = "";
        int bodyIndex = abc.findBodyIndex(method_info);
        if (bodyIndex != -1) {
            bodyStr = ABC.addTabs(abc.bodies[bodyIndex].toString(path + "." + getName(abc).getName(abc.constants, fullyQualifiedNames), pcode, isStatic, scriptIndex, classIndex, abc, this, abc.constants, abc.method_info, new Stack<GraphTargetItem>(), false, highlight, true, fullyQualifiedNames, null), 3);
        }
        return HilightedTextWriter.INDENT_STRING + HilightedTextWriter.INDENT_STRING + header + ((classIndex != -1 && abc.instance_info[classIndex].isInterface() || bodyIndex == -1) ? ";" : " {\r\n" + bodyStr + "\r\n" + HilightedTextWriter.INDENT_STRING + HilightedTextWriter.INDENT_STRING + "}");
    }

    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) {
        int bodyIndex = abc.findBodyIndex(method_info);
        if (bodyIndex != -1) {
            return abc.bodies[bodyIndex].removeTraps(abc.constants, abc, this, scriptIndex, classIndex, isStatic, path);
        }
        return 0;
    }
}
