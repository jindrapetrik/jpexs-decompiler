/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.Dependency;
import com.jpexs.decompiler.flash.exporters.script.DependencyParser;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.search.MethodId;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TraitFunction extends Trait implements TraitWithSlot {

    public int slot_id;

    public int method_info;

    @Override
    public void delete(ABC abc, boolean d) {
        super.delete(abc, d);

        abc.constants.getMultiname(name_index).deleted = d;
        abc.method_info.get(method_info).delete(abc, d);
    }

    @Override
    public int getSlotIndex() {
        return slot_id;
    }    

    @Override
    public GraphTextWriter toStringHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) {
        MethodBody body = abc.findBody(method_info);
        if (body == null) {
            writer.appendNoHilight("native ");
        }
        getModifiers(abc, isStatic, insideInterface, writer, classIndex);
        writer.hilightSpecial("function ", HighlightSpecialType.TRAIT_TYPE);
        writer.hilightSpecial(abc.constants.getMultiname(name_index).getName(abc.constants, fullyQualifiedNames, false, true), HighlightSpecialType.TRAIT_NAME);
        writer.appendNoHilight("(");
        abc.method_info.get(method_info).getParamStr(writer, abc.constants, body, abc, fullyQualifiedNames);
        writer.appendNoHilight(") : ");
        abc.method_info.get(method_info).getReturnTypeStr(writer, abc.constants, fullyQualifiedNames);
        return writer;
    }

    @Override
    public void convertHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) {
    }

    @Override
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return "Function " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
    }

    @Override
    public GraphTextWriter toString(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {
        writeImports(abcIndex, scriptIndex, classIndex, false, abc, writer, getPackage(abc), fullyQualifiedNames);
        getMetaData(parent, convertData, abc, writer);
        writer.startMethod(method_info, getName(abc).getName(abc.constants, new ArrayList<>(), true, false));
        toStringHeader(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, insideInterface);

        writer.startBlock();
        int bodyIndex = abc.findBodyIndex(method_info);
        if (bodyIndex != -1) {
            //writeUses(scriptIndex, classIndex, isStatic, abc, writer);               
            List<MethodBody> callStack = new ArrayList<>();
            callStack.add(abc.bodies.get(bodyIndex));
            abc.bodies.get(bodyIndex).toString(callStack, abcIndex, path + "." + abc.constants.getMultiname(name_index).getName(abc.constants, fullyQualifiedNames, false, true), exportMode, abc, this, writer, fullyQualifiedNames, new HashSet<>());
        }
        writer.endBlock();

        writer.newLine();
        writer.endMethod();
        return writer;
    }

    @Override
    public void convert(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {
        fullyQualifiedNames = new ArrayList<>();
        writeImports(abcIndex, scriptIndex, classIndex, false, abc, writer, getPackage(abc), fullyQualifiedNames);
        writer.startMethod(method_info, getName(abc).getName(abc.constants, new ArrayList<>(), true, false));
        convertHeader(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        int bodyIndex = abc.findBodyIndex(method_info);
        if (bodyIndex != -1) {
            List<MethodBody> callStack = new ArrayList<>();
            callStack.add(abc.bodies.get(bodyIndex));
            abc.bodies.get(bodyIndex).convert(callStack, abcIndex, convertData, path + "." + abc.constants.getMultiname(name_index).getName(abc.constants, fullyQualifiedNames, false, true), exportMode, isStatic, method_info, scriptIndex, classIndex, abc, this, scopeStack, 0, writer, fullyQualifiedNames, null, true, new HashSet<>());
        }
        writer.endMethod();
    }

    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException {
        int bodyIndex = abc.findBodyIndex(method_info);
        if (bodyIndex != -1) {
            return abc.bodies.get(bodyIndex).removeTraps(abc, this, scriptIndex, classIndex, isStatic, path);
        }
        return 0;
    }

    @Override
    public TraitFunction clone() {
        TraitFunction ret = (TraitFunction) super.clone();
        return ret;
    }

    @Override
    public void getDependencies(AbcIndexing abcIndex, int scriptIndex, int classIndex, boolean isStatic, String customNs, ABC abc, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<String> uses) throws InterruptedException {
        if (ignorePackage == null) {
            ignorePackage = getPackage(abc);
        }
        super.getDependencies(abcIndex, scriptIndex, classIndex, false, customNs, abc, dependencies, ignorePackage, fullyQualifiedNames, uses);
        DependencyParser.parseDependenciesFromMethodInfo(abcIndex, this, scriptIndex, classIndex, false, customNs, abc, method_info, dependencies, ignorePackage, fullyQualifiedNames, new ArrayList<>(), uses);
    }

    @Override
    public GraphTextWriter convertTraitHeader(ABC abc, GraphTextWriter writer) {
        convertCommonHeaderFlags("function", abc, writer);
        writer.newLine();
        writer.appendNoHilight("slotid ");
        writer.hilightSpecial(Integer.toString(slot_id), HighlightSpecialType.SLOT_ID);
        writer.newLine();
        /*writer.appendNoHilight("method_info ");//Not in RAbcDasm
        writer.appendNoHilight("" + method_info);
        writer.newLine();*/
        return writer;
    }

    @Override
    public void getMethodInfos(ABC abc, int traitId, int classIndex, List<MethodId> methodInfos) {
        methodInfos.add(new MethodId(traitId, classIndex, method_info));
    }
}
