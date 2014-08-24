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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import java.util.List;

public class TraitFunction extends Trait implements TraitWithSlot {

    public int slot_id;
    public int method_info;

    @Override
    public void delete(ABC abc, boolean d) {
        abc.constants.constant_multiname.get(name_index).deleted = d;
        abc.method_info.get(method_info).delete(abc, d);
    }

    @Override
    public int getSlotIndex() {
        return slot_id;
    }

    @Override
    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        return "Function " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
    }

    @Override
    public GraphTextWriter toStringHeader(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
        String modifier = getModifiers(abcTags, abc, isStatic) + " ";
        MethodBody body = abc.findBody(method_info);
        if (body == null) {
            modifier = "native " + modifier;
        }
        if (modifier.equals(" ")) {
            modifier = "";
        }
        writer.appendNoHilight(modifier);
        writer.hilightSpecial("function ", "traittype");
        writer.hilightSpecial(abc.constants.getMultiname(name_index).getName(abc.constants, fullyQualifiedNames, false), "traitname");
        writer.appendNoHilight("(");
        abc.method_info.get(method_info).getParamStr(writer, abc.constants, body, abc, fullyQualifiedNames);
        writer.appendNoHilight(") : ");
        abc.method_info.get(method_info).getReturnTypeStr(writer, abc.constants, fullyQualifiedNames);
        return writer;
    }

    @Override
    public void convertHeader(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
    }

    @Override
    public GraphTextWriter toString(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<String> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        toStringHeader(parent, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        if (abc.instance_info.get(classIndex).isInterface()) {
            writer.appendNoHilight(";");
        } else {
            writer.appendNoHilight(" {").newLine();
            int bodyIndex = abc.findBodyIndex(method_info);
            if (bodyIndex != -1) {
                abc.bodies.get(bodyIndex).toString(path + "." + abc.constants.getMultiname(name_index).getName(abc.constants, fullyQualifiedNames, false), exportMode, abc, this, abc.constants, abc.method_info, writer, fullyQualifiedNames);
            }
            writer.newLine();
            writer.appendNoHilight("}");
        }
        writer.newLine();
        return writer;
    }

    @Override
    public void convert(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<String> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        convertHeader(parent, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        if (!abc.instance_info.get(classIndex).isInterface()) {
            int bodyIndex = abc.findBodyIndex(method_info);
            if (bodyIndex != -1) {
                abc.bodies.get(bodyIndex).convert(path + "." + abc.constants.getMultiname(name_index).getName(abc.constants, fullyQualifiedNames, false), exportMode, isStatic, scriptIndex, classIndex, abc, this, abc.constants, abc.method_info, new ScopeStack(), false, writer, fullyQualifiedNames, null, true);
            }
        }
    }

    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException {
        int bodyIndex = abc.findBodyIndex(method_info);
        if (bodyIndex != -1) {
            return abc.bodies.get(bodyIndex).removeTraps(abc.constants, abc, this, scriptIndex, classIndex, isStatic, path);
        }
        return 0;
    }
}
