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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.model.NewFunctionAVM2Item;
import com.jpexs.decompiler.flash.abc.types.AssignedValue;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.Dependency;
import com.jpexs.decompiler.flash.exporters.script.DependencyParser;
import com.jpexs.decompiler.flash.exporters.script.DependencyType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.search.MethodId;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TraitSlotConst extends Trait implements TraitWithSlot {

    public int slot_id;

    public int type_index;

    public int value_index;

    public int value_kind;

    @Override
    public void delete(ABC abc, boolean d) {
        abc.constants.getMultiname(name_index).deleted = d;
    }

    @Override
    public int getSlotIndex() {
        return slot_id;
    }

    @Override
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        String typeStr = "*";
        if (type_index > 0) {
            typeStr = abc.constants.getMultiname(type_index).toString(abc.constants, fullyQualifiedNames);
        }
        return "0x" + Helper.formatAddress(fileOffset) + " " + Helper.byteArrToString(bytes) + " SlotConst " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " type=" + typeStr + " value=" + (new ValueKind(value_index, value_kind)).toString(abc.constants) + " metadata=" + Helper.intArrToString(metadata);
    }

    public String getType(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        String typeStr = "*";
        if (type_index > 0) {
            typeStr = constants.getMultiname(type_index).getName(constants, fullyQualifiedNames, false, true);
        }
        return typeStr;
    }

    public GraphTextWriter getNameStr(GraphTextWriter writer, ABC abc, List<DottedChain> fullyQualifiedNames) {
        String typeStr = getType(abc.constants, fullyQualifiedNames);
        if (typeStr.equals("*")) {
            typeStr = "";
        } else {
            typeStr = ":" + typeStr;
        }
        ValueKind val = null;
        if (value_kind != 0) {
            val = new ValueKind(value_index, value_kind);
        }

        String slotconst = "var";
        if (kindType == TRAIT_CONST) {
            slotconst = "const";
        }
        if (val != null && val.isNamespace()) {
            slotconst = "namespace";
        }
        writer.hilightSpecial(slotconst + " ", HighlightSpecialType.TRAIT_TYPE);
        writer.hilightSpecial(getName(abc).getName(abc.constants, fullyQualifiedNames, false, true), HighlightSpecialType.TRAIT_NAME);
        writer.hilightSpecial(typeStr, HighlightSpecialType.TRAIT_TYPE_NAME);
        return writer;
    }

    public void getValueStr(ScriptExportMode exportMode, Trait parent, ConvertData convertData, GraphTextWriter writer, ABC abc, List<DottedChain> fullyQualifiedNames) throws InterruptedException {
        if (convertData.assignedValues.containsKey(this)) {

            AssignedValue assignment = convertData.assignedValues.get(this);
            writer.startTrait(assignment.initializer);
            writer.startMethod(assignment.method);
            if (Configuration.showMethodBodyId.get()) {
                writer.appendNoHilight("// method body index: ");
                writer.appendNoHilight(abc.findBodyIndex(assignment.method));
                writer.appendNoHilight(" method index: ");
                writer.appendNoHilight(assignment.method);
                writer.newLine();
            }
            if (exportMode != ScriptExportMode.AS_METHOD_STUBS) {
                assignment.value.toString(writer, LocalData.create(abc.constants, new HashMap<>(), fullyQualifiedNames));
            }
            writer.endMethod();
            writer.endTrait();
            return;
        }

        if (value_kind != 0) {
            ValueKind val = new ValueKind(value_index, value_kind);
            writer.hilightSpecial(val.toString(abc.constants), HighlightSpecialType.TRAIT_VALUE);
        }
    }

    public boolean isNamespace() {
        if (value_kind != 0) {
            ValueKind val = new ValueKind(value_index, value_kind);
            return val.isNamespace();
        }
        return false;
    }

    @Override
    public GraphTextWriter toString(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        getMetaData(parent, convertData, abc, writer);
        Multiname n = getName(abc);
        boolean showModifier = true;
        if ((classIndex == -1) && (n != null)) {
            Namespace ns = n.getNamespace(abc.constants);
            if (ns == null) {
                showModifier = false;
            } else if ((ns.kind != Namespace.KIND_PACKAGE) && (ns.kind != Namespace.KIND_PACKAGE_INTERNAL)) {
                showModifier = false;
            }
        }
        if (showModifier) {
            getModifiers(abc, isStatic, writer);
        }
        if (convertData.assignedValues.containsKey(this)) {
            GraphTargetItem val = convertData.assignedValues.get(this).value;
            if (val instanceof NewFunctionAVM2Item) {
                return val.toString(writer, LocalData.create(abc.constants, new HashMap<>(), fullyQualifiedNames));
            }
        }
        getNameStr(writer, abc, fullyQualifiedNames);
        if (value_kind != 0 || convertData.assignedValues.containsKey(this)) {
            writer.appendNoHilight(" = ");
            getValueStr(exportMode, parent, convertData, writer, abc, fullyQualifiedNames);
        }
        return writer.appendNoHilight(";").newLine();
    }

    @Override
    public void convert(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        getNameStr(writer, abc, fullyQualifiedNames);
        if (value_kind != 0 || convertData.assignedValues.containsKey(this)) {
            getValueStr(exportMode, parent, convertData, writer, abc, fullyQualifiedNames);
        }
    }

    public boolean isConst() {
        return kindType == TRAIT_CONST;
    }

    public boolean isVar() {
        return kindType == TRAIT_SLOT;
    }

    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) {
        //do nothing
        return 0;
    }

    @Override
    public TraitSlotConst clone() {
        TraitSlotConst ret = (TraitSlotConst) super.clone();
        return ret;
    }

    @Override
    public void getDependencies(String customNs, ABC abc, List<Dependency> dependencies, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
        if (ignorePackage == null) {
            ignorePackage = getPackage(abc);
        }
        super.getDependencies(customNs, abc, dependencies, uses, ignorePackage, fullyQualifiedNames);
        DependencyParser.parseDependenciesFromMultiname(customNs, abc, dependencies, uses, abc.constants.getMultiname(type_index), getPackage(abc), fullyQualifiedNames, DependencyType.SIGNATURE);
    }

    @Override
    public boolean isVisible(boolean isStatic, ABC abc) {

        if (Configuration.handleSkinPartsAutomatically.get()) {
            /*
             Hide: private static var _skinParts
             (part of [SkinPart] compilations)
             */
            if (isStatic && "_skinParts".equals(getName(abc).getName(abc.constants, new ArrayList<>(), true, true))) {
                if (kindType == Trait.TRAIT_SLOT) {
                    if ("_skinParts".equals(getName(abc).getName(abc.constants, new ArrayList<>(), true, true))) {
                        if (getName(abc).getNamespace(abc.constants).kind == Namespace.KIND_PRIVATE) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public GraphTextWriter convertTraitHeader(ABC abc, GraphTextWriter writer) {
        convertCommonHeaderFlags(isConst() ? "const" : "slot", abc, writer);
        writer.newLine();
        writer.appendNoHilight("slotid ");
        writer.hilightSpecial(Integer.toString(slot_id), HighlightSpecialType.SLOT_ID);
        writer.newLine();
        writer.appendNoHilight("type ");
        writer.hilightSpecial(abc.constants.multinameToString(type_index), HighlightSpecialType.TRAIT_TYPE_NAME);
        writer.newLine();
        writer.appendNoHilight("value ");
        writer.hilightSpecial((new ValueKind(value_index, value_kind).toASMString(abc.constants)), HighlightSpecialType.TRAIT_VALUE);
        writer.newLine();
        return writer;
    }

    @Override
    public void getMethodInfos(ABC abc, int traitId, int classIndex, List<MethodId> methodInfos) {
    }
}
