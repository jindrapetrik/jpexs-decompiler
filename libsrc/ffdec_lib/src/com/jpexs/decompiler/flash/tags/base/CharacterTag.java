/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.ActionTreeOperation;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A character tag.
 *
 * @author JPEXS
 */
public abstract class CharacterTag extends Tag implements CharacterIdTag {

    protected LinkedHashSet<String> classNames = new LinkedHashSet<>();

    public CharacterTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public void setClassNames(LinkedHashSet<String> classNames) {
        this.classNames = new LinkedHashSet<>(classNames);
    }

    public LinkedHashSet<String> getClassNames() {
        return new LinkedHashSet<>(classNames);
    }

    public void addClassName(String className) {
        classNames.add(className);
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        int chid = getCharacterId();
        if (chid > -1) {
            ret.put("chid", "" + chid);
        }
        if (exportName != null) {
            ret.put("exp", Helper.escapePCodeString(exportName));
        }
        if (!classNames.isEmpty()) {
            ret.put("cls", Helper.joinEscapePCodeString(", ", classNames));
        }
        return ret;
    }

    @Override
    public String getExportFileName() {
        String result = super.getExportFileName();
        result += "_" + getCharacterId();
        if (exportName != null) {
            result += "_" + exportName;
        }
        if (classNames.size() == 1) {
            result += "_" + classNames.iterator().next();
        }
        return result;
    }

    public String getCharacterExportFileName() {
        String result = "" + getCharacterId();
        if (exportName != null) {
            result += "_" + exportName;
        }
        if (classNames.size() == 1) {
            result += "_" + classNames.iterator().next();
        }
        return result;
    }

    protected String exportName;

    public void setExportName(String exportName) {
        if ("".equals(exportName)) {
            exportName = null;
        }
        this.exportName = exportName;
    }

    public String getExportName() {
        return exportName;
    }

    public DefineScalingGridTag getScalingGridTag() {
        if (swf == null) { //???
            return null;
        }
        return (DefineScalingGridTag) swf.getCharacterIdTag(getCharacterId(), DefineScalingGridTag.ID);
    }

    @Override
    public String getUniqueId() {
        if (getCharacterId() == -1) {
            return null;
        }
        return "" + getCharacterId();
    }

    private String getMembersToClassName(GraphTargetItem item) {
        List<String> ret = new ArrayList<>();
        while (item instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) item;
            if (!(mem.memberName instanceof DirectValueActionItem)) {
                return null;
            }
            DirectValueActionItem dv = ((DirectValueActionItem) mem.memberName);
            if (!dv.isString()) {
                return null;
            }
            ret.add(0, dv.getAsString());
            item = mem.object;
        }
        if (!(item instanceof GetVariableActionItem)) {
            return null;
        }
        GetVariableActionItem gv = (GetVariableActionItem) item;
        if (!(gv.name instanceof DirectValueActionItem)) {
            return null;
        }
        DirectValueActionItem dv = ((DirectValueActionItem) gv.name);
        if (!dv.isString()) {
            return null;
        }
        String varName = dv.getAsString();
        ret.add(0, varName);
        return String.join(".", ret);
    }

    public String getAs2ClassName() {
        String linkageIdentifier = getExportName();
        if (linkageIdentifier == null) {
            return null;
        }
        Reference<String> classNameRef = new Reference<>(null);
        for (Tag t : getSwf().getTags()) {
            if (t instanceof DoInitActionTag) {
                DoInitActionTag as = (DoInitActionTag) t;
                if (as.spriteId == getCharacterId()) {
                    GraphTextWriter writer = new NulWriter();
                    try {
                        List<ActionTreeOperation> ops = new ArrayList<>();
                        ops.add(new ActionTreeOperation() {
                            @Override
                            public void run(List<GraphTargetItem> tree) {
                                List<Integer> listToRemove = new ArrayList<>();
                                List<String> newClassNames = new ArrayList<>();
                                for (int i = 0; i < tree.size(); i++) {
                                    GraphTargetItem item = tree.get(i);
                                    if (!(item instanceof CallMethodActionItem)) {
                                        continue;
                                    }
                                    CallMethodActionItem callMethod = (CallMethodActionItem) item;
                                    if (!(callMethod.scriptObject instanceof GetVariableActionItem)) {
                                        continue;
                                    }
                                    GetVariableActionItem methodObject = (GetVariableActionItem) callMethod.scriptObject;
                                    if (!(methodObject.name instanceof DirectValueActionItem)) {
                                        continue;
                                    }
                                    if (methodObject.name == null || !methodObject.name.toString().equals("Object")) {
                                        continue;
                                    }
                                    if (!(callMethod.methodName instanceof DirectValueActionItem)) {
                                        continue;
                                    }
                                    if (!callMethod.methodName.toString().equals("registerClass")) {
                                        continue;
                                    }
                                    if (callMethod.arguments.size() != 2) {
                                        continue;
                                    }
                                    if (!(callMethod.arguments.get(0) instanceof DirectValueActionItem)) {
                                        continue;
                                    }
                                    if (linkageIdentifier != null && !linkageIdentifier.equals(callMethod.arguments.get(0).toString())) {
                                        continue;
                                    }
                                    String className = getMembersToClassName(callMethod.arguments.get(1));
                                    if (className == null) {
                                        continue;
                                    }
                                    newClassNames.add(className);
                                    listToRemove.add(i);
                                }
                                //There's only single one
                                if (listToRemove.size() != 1) {
                                    return;
                                }
                                classNameRef.setVal(newClassNames.get(0));
                            }
                        });
                        as.getActionScriptSource(writer, null, ops);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CharacterTag.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return classNameRef.getVal();
    }
}
