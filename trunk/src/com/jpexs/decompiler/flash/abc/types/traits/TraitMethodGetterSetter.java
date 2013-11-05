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
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.ExportMode;
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
    public void convertHeader(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
    }

    @Override
    public GraphTextWriter toStringHeader(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
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

        writer.appendNoHilight(modifier);
        writer.hilightSpecial("function " + addKind, "traittype");
        writer.hilightSpecial(getName(abc).getName(abc.constants, fullyQualifiedNames), "traitname");
        writer.appendNoHilight("(");
        abc.method_info[method_info].getParamStr(writer, abc.constants, body, abc, fullyQualifiedNames);
        writer.appendNoHilight(") : ");
        abc.method_info[method_info].getReturnTypeStr(writer, abc.constants, fullyQualifiedNames);
        return writer;
    }

    @Override
    public void convert(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
        path = path + "." + getName(abc).getName(abc.constants, fullyQualifiedNames);
        convertHeader(parent, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        int bodyIndex = abc.findBodyIndex(method_info);
        if (!(classIndex != -1 && abc.instance_info[classIndex].isInterface() || bodyIndex == -1)) {
            if (bodyIndex != -1) {
                abc.bodies[bodyIndex].convert(path, exportMode, isStatic, scriptIndex, classIndex, abc, this, abc.constants, abc.method_info, new Stack<GraphTargetItem>(), false, fullyQualifiedNames, null, true);
            }
        }
    }

    @Override
    public GraphTextWriter toString(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
        path = path + "." + getName(abc).getName(abc.constants, fullyQualifiedNames);
        toStringHeader(parent, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        int bodyIndex = abc.findBodyIndex(method_info);
        if (classIndex != -1 && abc.instance_info[classIndex].isInterface() || bodyIndex == -1) {
            writer.appendNoHilight(";");
        } else {
            writer.appendNoHilight(" {").newLine();
            if (bodyIndex != -1) {
                abc.bodies[bodyIndex].toString(path, exportMode, isStatic, scriptIndex, classIndex, abc, this, abc.constants, abc.method_info, new Stack<GraphTargetItem>(), false, writer, fullyQualifiedNames, null);
            }
            writer.appendNoHilight("}");
        }
        writer.newLine();
        return writer;
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
