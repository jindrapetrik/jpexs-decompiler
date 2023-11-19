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
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TraitMethodGetterSetter extends Trait {

    public int disp_id; //compiler assigned value that helps overriding

    public int method_info;

    @Override
    public void delete(ABC abc, boolean d) {
        super.delete(abc, d);

        abc.constants.getMultiname(name_index).deleted = d;
        abc.method_info.get(method_info).delete(abc, d);
    }   

    @Override
    public void convertHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) {
    }

    @Override
    public void getDependencies(AbcIndexing abcIndex, int scriptIndex, int classIndex, boolean isStatic, String customNs, ABC abc, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<String> uses) throws InterruptedException {
        if (ignorePackage == null) {
            ignorePackage = getPackage(abc);
        }
        super.getDependencies(abcIndex, scriptIndex, classIndex, isStatic, customNs, abc, dependencies, ignorePackage, fullyQualifiedNames, uses);

        if (customNs == null) {
            Multiname m = getName(abc);
            int nskind = m.getSimpleNamespaceKind(abc.constants);
            if (nskind == Namespace.KIND_NAMESPACE) {
                customNs = m.getSimpleNamespaceName(abc.constants).toRawString();
            }
        }
        DependencyParser.parseDependenciesFromMethodInfo(abcIndex, this, scriptIndex, classIndex, isStatic, customNs, abc, method_info, dependencies, ignorePackage, fullyQualifiedNames, new ArrayList<>(), uses);
    }

    @Override
    public GraphTextWriter toStringHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) {
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
    public void convert(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {
        if (classIndex < 0) {
            writeImports(abcIndex, scriptIndex, classIndex, isStatic, abc, writer, getPackage(abc), fullyQualifiedNames);
        }
        writer.startMethod(method_info, getName(abc).getName(abc.constants, new ArrayList<>(), true, false));
        path = path + "." + getName(abc).getName(abc.constants, fullyQualifiedNames, false, true);
        convertHeader(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        int bodyIndex = abc.findBodyIndex(method_info);
        if (exportMode != ScriptExportMode.AS_METHOD_STUBS) {
            if (!(classIndex != -1 && abc.instance_info.get(classIndex).isInterface() || bodyIndex == -1)) {
                if (bodyIndex != -1) {
                    List<MethodBody> callStack = new ArrayList<>();
                    callStack.add(abc.bodies.get(bodyIndex));
                    abc.bodies.get(bodyIndex).convert(callStack, abcIndex, convertData, path, exportMode, isStatic, method_info, scriptIndex, classIndex, abc, this, scopeStack, 0, writer, fullyQualifiedNames, null, true, new HashSet<>());
                }
            }
        }
        writer.endMethod();
    }

    @Override
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return "0x" + Helper.formatAddress(fileOffset) + " " + Helper.byteArrToString(bytes) + " MethodGetterSetter " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " disp_id=" + disp_id + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
    }
    
    @Override
    public GraphTextWriter toString(AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {

        if (classIndex < 0) {
            writeImports(abcIndex, scriptIndex, classIndex, isStatic, abc, writer, getPackage(abc), fullyQualifiedNames);
        }
        getMetaData(parent, convertData, abc, writer);
        writer.startMethod(method_info, getName(abc).getName(abc.constants, new ArrayList<>(), true, false));
        path = path + "." + getName(abc).getName(abc.constants, fullyQualifiedNames, false, true);
        toStringHeader(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, insideInterface);
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
                    abc.bodies.get(bodyIndex).toString(callStack, abcIndex, path, exportMode, abc, this, writer, fullyQualifiedNames, new HashSet<>());
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

    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException {
        int bodyIndex = abc.findBodyIndex(method_info);
        if (bodyIndex != -1) {
            return abc.bodies.get(bodyIndex).removeTraps(abc, this, scriptIndex, classIndex, isStatic, path);
        }
        return 0;
    }

    @Override
    public TraitMethodGetterSetter clone() {
        TraitMethodGetterSetter ret = (TraitMethodGetterSetter) super.clone();
        return ret;
    }

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

    @Override
    public void getMethodInfos(ABC abc, int traitId, int classIndex, List<MethodId> methodInfos) {
        methodInfos.add(new MethodId(traitId, classIndex, method_info));
    }
}
