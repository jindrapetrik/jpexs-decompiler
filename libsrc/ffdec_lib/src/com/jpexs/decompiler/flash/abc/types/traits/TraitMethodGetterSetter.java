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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.Dependency;
import com.jpexs.decompiler.flash.exporters.script.DependencyParser;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.search.MethodId;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Method, getter or setter trait in ABC file.
 *
 * @author JPEXS
 */
public class TraitMethodGetterSetter extends Trait {

    /**
     * Dispatch id. Compiler assigned value that helps overriding.
     */
    public int disp_id;

    /**
     * Method info index.
     */
    public int method_info;

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
        abc.method_info.get(method_info).delete(abc, d);
    }
  
    @Override
    public void convertHeader(int swfVersion, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) {
    }

    /**
     * Gets dependencies.
     *
     * @param abcIndex ABC indexing
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static
     * @param customNamespace Custom namespace
     * @param abc ABC
     * @param dependencies Dependencies
     * @param ignorePackage Ignore package
     * @param fullyQualifiedNames Fully qualified names
     * @param uses Uses
     * @throws InterruptedException On interrupt
     */
    @Override
    public void getDependencies(AbcIndexing abcIndex, int scriptIndex, int classIndex, boolean isStatic, String customNamespace, ABC abc, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<String> uses, Reference<Integer> numberContextRef) throws InterruptedException {
        if (ignorePackage == null) {
            ignorePackage = getPackage(abc);
        }
        super.getDependencies(abcIndex, scriptIndex, classIndex, isStatic, customNamespace, abc, dependencies, ignorePackage, fullyQualifiedNames, uses, numberContextRef);

        if (customNamespace == null) {
            Multiname m = getName(abc);
            int nskind = m.getSimpleNamespaceKind(abc.constants);
            if (nskind == Namespace.KIND_NAMESPACE) {
                customNamespace = m.getSimpleNamespaceName(abc.constants).toRawString();
            }
        }
        DependencyParser.parseDependenciesFromMethodInfo(abcIndex, this, scriptIndex, classIndex, isStatic, customNamespace, abc, method_info, dependencies, ignorePackage, fullyQualifiedNames, new ArrayList<>(), uses, numberContextRef);
    }
    
    @Override
    public GraphTextWriter toStringHeader(int swfVersion, Trait parent, DottedChain packageName, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) {
        String addKind = "";
        if (kindType == TRAIT_GETTER) {
            addKind = "get ";
        }
        if (kindType == TRAIT_SETTER) {
            addKind = "set ";
        }
        MethodBody body = abc.findBody(method_info);

        getModifiers(abc, isStatic, insideInterface, writer, classIndex);

        if (abc.method_info.get(method_info).flagNative()) {
            writer.appendNoHilight("native ");
        }

        writer.hilightSpecial("function " + addKind, HighlightSpecialType.TRAIT_TYPE);
        writer.hilightSpecial(getName(abc).getName(abc.constants, new ArrayList<>(), false, true), HighlightSpecialType.TRAIT_NAME);
        writer.appendNoHilight("(");
        abc.method_info.get(method_info).getParamStr(writer, abc.constants, body, abc, fullyQualifiedNames);
        writer.appendNoHilight(") : ");
        abc.method_info.get(method_info).getReturnTypeStr(writer, abc.constants, fullyQualifiedNames);
        return writer;
    }
   
    @Override
    public void convert(int swfVersion, AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {
        int nsKind = getName(abc).getSimpleNamespaceKind(abc.constants);
        writer.startMethod(method_info, getName(abc).getName(abc.constants, new ArrayList<>(), true, false));
        path = path + "." + getName(abc).getName(abc.constants, fullyQualifiedNames, false, true);
        convertHeader(swfVersion, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        int bodyIndex = abc.findBodyIndex(method_info);
        if (exportMode != ScriptExportMode.AS_METHOD_STUBS) {
            if (!(classIndex != -1 && abc.instance_info.get(classIndex).isInterface() || bodyIndex == -1)) {
                if (bodyIndex != -1) {
                    List<MethodBody> callStack = new ArrayList<>();
                    callStack.add(abc.bodies.get(bodyIndex));
                    abc.bodies.get(bodyIndex).convert(swfVersion, callStack, abcIndex, convertData, path, exportMode, isStatic, method_info, scriptIndex, classIndex, abc, this, scopeStack, 0, writer, fullyQualifiedNames, null, true, new HashSet<>());                    
                }
            }
        }
        writer.endMethod();
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
        return "0x" + Helper.formatAddress(fileOffset) + " " + Helper.byteArrToString(bytes) + " MethodGetterSetter " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " disp_id=" + disp_id + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
    }

    @Override
    public GraphTextWriter toString(int swfVersion, AbcIndexing abcIndex, DottedChain packageName, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {
        getMetaData(this, convertData, abc, writer);
        writer.startMethod(method_info, getName(abc).getName(abc.constants, new ArrayList<>(), true, false));
        path = path + "." + getName(abc).getName(abc.constants, fullyQualifiedNames, false, true);
        toStringHeader(swfVersion, parent, packageName, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, insideInterface);
        int bodyIndex = abc.findBodyIndex(method_info);
        if (classIndex != -1 && abc.instance_info.get(classIndex).isInterface() || bodyIndex == -1) {
            writer.appendNoHilight(";");
        } else {
            writer.startBlock();
            if (exportMode != ScriptExportMode.AS_METHOD_STUBS) {
                if (exportMode != ScriptExportMode.AS) {
                    convertTraitHeader(abc, writer);
                }
                if (bodyIndex != -1) {
                    //writeUses(scriptIndex, classIndex, isStatic, abc, writer);
                    List<MethodBody> callStack = new ArrayList<>();
                    callStack.add(abc.bodies.get(bodyIndex));
                    abc.bodies.get(bodyIndex).toString(swfVersion, callStack, abcIndex, path, exportMode, abc, this, writer, fullyQualifiedNames, new HashSet<>());
                }
            } else {
                String retTypeRaw = abc.method_info.get(method_info).getReturnTypeRaw(abc.constants, fullyQualifiedNames);
                switch (retTypeRaw) {
                    case "void":
                        break;
                    case "int":
                    case "uint":
                        writer.append("return 0; //autogenerated").newLine();
                        break;
                    case "double":
                    case "float":
                        writer.append("return 0.0; //autogenerated").newLine();
                        break;
                    case "String":
                        writer.append("return \"\"; //autogenerated").newLine();
                        break;
                    default:
                        writer.append("return null; //autogenerated").newLine();
                        break;
                }
            }
            writer.endBlock();
        }
        writer.newLine();
        writer.endMethod();
        return writer;
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
     * @throws InterruptedException On interrupt
     */
    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException {
        int bodyIndex = abc.findBodyIndex(method_info);
        if (bodyIndex != -1) {
            return abc.bodies.get(bodyIndex).removeTraps(abc, this, scriptIndex, classIndex, isStatic, path);
        }
        return 0;
    }

    /**
     * Clones trait.
     *
     * @return Cloned trait
     */
    @Override
    public TraitMethodGetterSetter clone() {
        TraitMethodGetterSetter ret = (TraitMethodGetterSetter) super.clone();
        return ret;
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
            if ("skinParts".equals(getName(abc).getName(abc.constants, new ArrayList<>(), true, true))) {
                if (kindType == TRAIT_GETTER) {
                    MethodInfo mi = abc.method_info.get(method_info);
                    if (mi.param_types.length == 0 && "Object".equals(abc.constants.getMultiname(mi.ret_type).getNameWithNamespace(abc.constants, true).toRawString())) {
                        if (abc.constants.getNamespace(abc.constants.getMultiname(name_index).namespace_index).kind == Namespace.KIND_PROTECTED) {
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

        switch (kindType) {
            case Trait.TRAIT_METHOD:
                convertCommonHeaderFlags("method", abc, writer);
                break;
            case Trait.TRAIT_GETTER:
                convertCommonHeaderFlags("getter", abc, writer);
                break;
            case Trait.TRAIT_SETTER:
                convertCommonHeaderFlags("setter", abc, writer);
                break;
        }
        writer.newLine();
        writer.appendNoHilight("dispid ");
        writer.hilightSpecial("" + disp_id, HighlightSpecialType.DISP_ID);
        writer.newLine();
        /*writer.appendNoHilight("method_info ");//Not in RAbcDasm
        writer.appendNoHilight("" + method_info);
        writer.newLine();*/
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
        methodInfos.add(new MethodId(traitId, classIndex, method_info));
    }
}
