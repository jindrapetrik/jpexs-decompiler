/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.model.NewFunctionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.AssignedValue;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
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
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Slot or const trait in ABC file.
 *
 * @author JPEXS
 */
public class TraitSlotConst extends Trait implements TraitWithSlot {

    /**
     * Slot index.
     */
    public int slot_id;

    /**
     * Type index.
     */
    public int type_index;

    /**
     * Value index.
     */
    public int value_index;

    /**
     * Value kind.
     */
    public int value_kind;

    /**
     * Deletes this trait.
     *
     * @param abc ABC
     * @param d Deleted flag
     */
    @Override
    public void delete(ABC abc, boolean d) {
        super.delete(abc, d);

        abc.constants.getMultiname(name_index).deleted = d;
    }

    /**
     * Gets slot index.
     *
     * @return Slot index
     */
    @Override
    public int getSlotIndex() {
        return slot_id;
    }

    /**
     * Gets type as string.
     *
     * @param usedDeobfuscations Used deobfuscations
     * @param abc ABC
     * @param constants Constant pool
     * @param fullyQualifiedNames Fully qualified names
     * @return Type as string
     */
    public String getType(Set<String> usedDeobfuscations, ABC abc, AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        String typeStr = "*";
        if (type_index > 0) {
            typeStr = constants.getMultiname(type_index).getName(usedDeobfuscations, abc, constants, fullyQualifiedNames, false, true);
        }
        return typeStr;
    }

    /**
     * Gets name as string.
     *
     * @param writer Writer
     * @param abc ABC
     * @param fullyQualifiedNames Fully qualified names
     * @param usedDeobfuscations Used deobfuscations
     * @return Writer
     */
    public GraphTextWriter getNameStr(GraphTextWriter writer, ABC abc, List<DottedChain> fullyQualifiedNames, Set<String> usedDeobfuscations) {
        String typeStr = getType(usedDeobfuscations, abc, abc.constants, fullyQualifiedNames);
        ValueKind val = null;
        if (value_kind != 0) {
            val = new ValueKind(value_index, value_kind);
        }

        typeStr = ":" + typeStr;

        String slotconst = "var";
        if (kindType == TRAIT_CONST) {
            slotconst = "const";
        }
        if (val != null && type_index == 0 && val.isNamespace()) {
            slotconst = "namespace";
            typeStr = "";
        }
        writer.hilightSpecial(slotconst + " ", HighlightSpecialType.TRAIT_TYPE);
        writer.hilightSpecial(getName(abc).getName(usedDeobfuscations, abc, abc.constants, new ArrayList<>(), false, true), HighlightSpecialType.TRAIT_NAME);
        writer.hilightSpecial(typeStr, HighlightSpecialType.TRAIT_TYPE_NAME);
        return writer;
    }

    /**
     * Checks if value is present. (Can be assigned in initializer)
     *
     * @param abc ABC
     * @param convertData Convert data
     * @return True if value is present
     */
    private boolean hasValueStr(ABC abc, ConvertData convertData) {
        if (convertData.assignedValues.containsKey(this)) {
            return true;
        }
        if (value_kind == ValueKind.CONSTANT_Namespace) {
            if (abc.constants.getNamespace(value_index).kind == Namespace.KIND_PACKAGE_INTERNAL) {
                return false;
            }
        }
        return value_kind != 0;
    }

    /**
     * Gets value as string.
     *
     * @param usedDeobfuscations Used deobfuscations
     * @param swfVersion SWF version
     * @param abcIndex ABC indexing
     * @param exportMode Export mode
     * @param convertData Convert data
     * @param writer Writer
     * @param abc ABC
     * @param fullyQualifiedNames Fully qualified names
     * @throws InterruptedException On interrupt
     */
    public void getValueStr(Set<String> usedDeobfuscations, int swfVersion, AbcIndexing abcIndex, ScriptExportMode exportMode, ConvertData convertData, GraphTextWriter writer, ABC abc, List<DottedChain> fullyQualifiedNames) throws InterruptedException {
        if (convertData.assignedValues.containsKey(this)) {

            AssignedValue assignment = convertData.assignedValues.get(this);
            writer.startTrait(assignment.initializer);
            writer.startMethod(assignment.method, null);
            if (Configuration.showMethodBodyId.get()) {
                writer.appendNoHilight("// method body index: ");
                writer.appendNoHilight(abc.findBodyIndex(assignment.method));
                writer.appendNoHilight(" method index: ");
                writer.appendNoHilight(assignment.method);
                writer.newLine();
            }
            if (exportMode != ScriptExportMode.AS_METHOD_STUBS) {
                List<MethodBody> callStack = new ArrayList<>();
                callStack.add(abc.bodies.get(abc.findBodyIndex(assignment.method)));
                assignment.value.toString(writer, LocalData.create(callStack, abcIndex, abc, new HashMap<>(), fullyQualifiedNames, new HashSet<>(), exportMode, swfVersion, usedDeobfuscations));
            }
            writer.endMethod();
            writer.endTrait();
            return;
        }

        if (value_kind != 0) {
            ValueKind val = new ValueKind(value_index, value_kind);
            writer.hilightSpecial(val.toString(abc), HighlightSpecialType.TRAIT_VALUE);
        }
    }

    /**
     * Checks if the value is namespace.
     *
     * @return True if the value is namespace
     */
    public boolean isNamespace() {
        if (value_kind != 0) {
            ValueKind val = new ValueKind(value_index, value_kind);
            return val.isNamespace();
        }
        return false;
    }

    /**
     * To string.
     *
     * @param abc ABC
     * @param fullyQualifiedNames Fully qualified names
     * @return String
     */
    @Override
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        String typeStr = "*";
        if (type_index > 0) {
            typeStr = abc.constants.getMultiname(type_index).toString(abc.constants, fullyQualifiedNames);
        }
        return "0x" + Helper.formatAddress(fileOffset) + " " + Helper.byteArrToString(bytes) + " SlotConst " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " type=" + typeStr + " value=" + (new ValueKind(value_index, value_kind)).toString(abc) + " metadata=" + Helper.intArrToString(metadata);
    }

    @Override
    public GraphTextWriter toString(Set<String> usedDeobfuscations, int swfVersion, AbcIndexing abcIndex, DottedChain packageName, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {
        getMetaData(usedDeobfuscations, this, convertData, abc, writer);
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
            getModifiers(usedDeobfuscations, abc, isStatic, insideInterface, writer, classIndex);
        }
        if (convertData.assignedValues.containsKey(this)) {
            GraphTargetItem val = convertData.assignedValues.get(this).value;
            if (val instanceof NewFunctionAVM2Item) {
                List<MethodBody> callStack = new ArrayList<>();
                AssignedValue assignment = convertData.assignedValues.get(this);
                callStack.add(abc.bodies.get(abc.findBodyIndex(assignment.method)));
                return val.toString(writer, LocalData.create(callStack, abcIndex, abc, new HashMap<>(), fullyQualifiedNames, new HashSet<>(), exportMode, swfVersion, usedDeobfuscations));
            }
        }
        getNameStr(writer, abc, fullyQualifiedNames, usedDeobfuscations);
        if (hasValueStr(abc, convertData)) {
            writer.appendNoHilight(" = ");
            getValueStr(usedDeobfuscations, swfVersion, abcIndex, exportMode, convertData, writer, abc, fullyQualifiedNames);
        }
        return writer.appendNoHilight(";").newLine();
    }

    @Override
    public void convert(Set<String> usedDeobfuscations, int swfVersion, AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {
        getNameStr(writer, abc, fullyQualifiedNames, usedDeobfuscations);
        if (hasValueStr(abc, convertData)) {
            getValueStr(usedDeobfuscations, swfVersion, abcIndex, exportMode, convertData, writer, abc, fullyQualifiedNames);
        }
    }

    /**
     * Checks if the trait is const.
     *
     * @return True if the trait is const
     */
    public boolean isConst() {
        return kindType == TRAIT_CONST;
    }

    /**
     * Checks if the trait is var (= slot).
     *
     * @return True if the trait is var
     */
    public boolean isVar() {
        return kindType == TRAIT_SLOT;
    }

    /**
     * Removes traps - deobfuscation.
     *
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static
     * @param abc ABC
     * @param path Path
     * @return Number of removed traps
     */
    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) {
        //do nothing
        return 0;
    }

    /**
     * Clones the trait.
     *
     * @return Cloned trait
     */
    @Override
    public TraitSlotConst clone() {
        TraitSlotConst ret = (TraitSlotConst) super.clone();
        return ret;
    }
 
    @Override
    public void getDependencies(Set<String> usedDeobfuscations, AbcIndexing abcIndex, int scriptIndex, int classIndex, boolean isStatic, String customNamespace, ABC abc, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<String> uses, Reference<Integer> numberContextRef) throws InterruptedException {
        if (ignorePackage == null) {
            ignorePackage = getPackage(abc);
        }
        super.getDependencies(usedDeobfuscations, abcIndex, scriptIndex, classIndex, isStatic, customNamespace, abc, dependencies, ignorePackage, fullyQualifiedNames, uses, numberContextRef);
        DependencyParser.parseDependenciesFromMultiname(usedDeobfuscations, abcIndex, customNamespace, abc, dependencies, abc.constants.getMultiname(type_index), getPackage(abc), fullyQualifiedNames, DependencyType.SIGNATURE, uses);
    }

    /**
     * Checks if trait is visible.
     *
     * @param isStatic Is static
     * @param abc ABC
     * @return True if trait is visible
     */
    @Override
    public boolean isVisible(boolean isStatic, ABC abc) {

        if (Configuration.handleSkinPartsAutomatically.get()) {
            /*
             Hide: private static var _skinParts
             (part of [SkinPart] compilations)
             */
            if (isStatic && "_skinParts".equals(getName(abc).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), true, true))) {
                if (kindType == Trait.TRAIT_SLOT) {
                    if ("_skinParts".equals(getName(abc).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), true, true))) {
                        if (getName(abc).getNamespace(abc.constants).kind == Namespace.KIND_PRIVATE) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Converts trait header.
     *
     * @param abc ABC
     * @param writer Writer
     * @return Writer
     */
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
        if (value_kind != ValueKind.CONSTANT_Undefined) {
            writer.appendNoHilight("value ");
            writer.hilightSpecial((new ValueKind(value_index, value_kind).toASMString(abc)), HighlightSpecialType.TRAIT_VALUE);
            writer.newLine();
        }
        return writer;
    }

    /**
     * Gets method infos.
     *
     * @param abc ABC
     * @param traitId Trait ID
     * @param classIndex Class index
     * @param methodInfos Method infos
     */
    @Override
    public void getMethodInfos(ABC abc, int traitId, int classIndex, List<MethodId> methodInfos) {
    }
}
