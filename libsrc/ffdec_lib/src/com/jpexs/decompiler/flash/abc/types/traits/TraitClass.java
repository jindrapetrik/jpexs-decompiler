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
import com.jpexs.decompiler.flash.abc.avm2.model.ClassAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructPropAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.Dependency;
import com.jpexs.decompiler.flash.exporters.script.DependencyParser;
import com.jpexs.decompiler.flash.exporters.script.DependencyType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.search.MethodId;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Class trait in ABC file
 *
 * @author JPEXS
 */
public class TraitClass extends Trait implements TraitWithSlot {

    /**
     * Slot id
     */
    public int slot_id;

    /**
     * Class info index
     */
    public int class_info;

    /**
     * Is class initializer empty
     */
    private boolean classInitializerIsEmpty;

    /**
     * Frame trait names
     */
    private final List<String> frameTraitNames = new ArrayList<>();

    /**
     * Deletes this trait.
     *
     * @param abc ABC
     * @param d Deleted flag
     */
    @Override
    public void delete(ABC abc, boolean d) {
        super.delete(abc, d);
        abc.deleteClass(class_info, d);
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
        super.getDependencies(abcIndex, scriptIndex, -1, false, customNamespace, abc, dependencies, ignorePackage == null ? getPackage(abc) : ignorePackage, fullyQualifiedNames, uses, numberContextRef);
        ClassInfo classInfo = abc.class_info.get(class_info);
        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        DottedChain packageName = instanceInfo.getName(abc.constants).getNamespace(abc.constants).getName(abc.constants); //assume not null name

        //DependencyParser.parseDependenciesFromMultiname(customNs, abc, dependencies, uses, abc.constants.getMultiname(instanceInfo.name_index), packageName, fullyQualifiedNames);
        if (instanceInfo.super_index > 0) {
            DependencyParser.parseDependenciesFromMultiname(abcIndex, customNamespace, abc, dependencies, abc.constants.getMultiname(instanceInfo.super_index), packageName, fullyQualifiedNames, DependencyType.INHERITANCE, uses);
        }
        for (int i : instanceInfo.interfaces) {
            DependencyParser.parseDependenciesFromMultiname(abcIndex, customNamespace, abc, dependencies, abc.constants.getMultiname(i), packageName, fullyQualifiedNames, DependencyType.INHERITANCE, uses);
        }

        //static
        classInfo.static_traits.getDependencies(abcIndex, scriptIndex, class_info, true, customNamespace, abc, dependencies, packageName, fullyQualifiedNames, uses, numberContextRef);

        //static initializer
        DependencyParser.parseDependenciesFromMethodInfo(abcIndex, null, scriptIndex, class_info, true, customNamespace, abc, classInfo.cinit_index, dependencies, packageName, fullyQualifiedNames, new ArrayList<>(), uses, numberContextRef);

        //instance
        instanceInfo.instance_traits.getDependencies(abcIndex, scriptIndex, class_info, false, customNamespace, abc, dependencies, packageName, fullyQualifiedNames, uses, numberContextRef);

        //instance initializer
        DependencyParser.parseDependenciesFromMethodInfo(abcIndex, null, scriptIndex, class_info, false, customNamespace, abc, instanceInfo.iinit_index, dependencies, packageName, fullyQualifiedNames, new ArrayList<>(), uses, numberContextRef);
    }

    @Override
    public GraphTextWriter toStringHeader(int swfVersion, Trait parent, DottedChain packageName, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) {
        abc.instance_info.get(class_info).getClassHeaderStr(convertData.assetsDir, writer, abc, fullyQualifiedNames, false, false /*??*/);
        return writer;
    }

    @Override
    public void convertHeader(int swfVersion, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) {
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
        return "Class " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " class_info=" + class_info + " metadata=" + Helper.intArrToString(metadata);
    }

    /**
     * To string.
     *
     * @param abcIndex ABC indexing
     * @param parent Parent trait
     * @param convertData Convert data
     * @param path Path
     * @param abc ABC
     * @param isStatic Is static
     * @param exportMode Export mode
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param parallel Parallel
     * @param insideInterface Inside interface
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    @Override
    public GraphTextWriter toString(int swfVersion, AbcIndexing abcIndex, DottedChain packageName, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, boolean insideInterface) throws InterruptedException {

        InstanceInfo instanceInfo = abc.instance_info.get(class_info);

        boolean isInterface = instanceInfo.isInterface();

        Multiname instanceInfoMultiname = instanceInfo.getName(abc.constants);
        //DottedChain packageName = instanceInfoMultiname.getNamespace(abc.constants).getName(abc.constants); //assume not null name

        Reference<Boolean> first = new Reference<>(true);

        String instanceInfoName = instanceInfoMultiname.getName(abc.constants, fullyQualifiedNames, false, true);

        getMetaData(this, convertData, abc, writer);

        boolean allowEmbed = true;

        if (convertData.exportEmbedFlaMode) {
            allowEmbed = false;
            if (abc.getSwf() != null) {
                CharacterTag ct = abc.getSwf().getCharacterByClass(instanceInfoMultiname.getNameWithNamespace(abc.constants, false).toRawString());
                if (ct == null) {
                    allowEmbed = false;
                } else {
                    if (ct instanceof DefineBinaryDataTag) {
                        allowEmbed = true;
                    }

                    if (ct instanceof ImageTag) {
                        allowEmbed = true;
                        if (abcIndex.isInstanceOf(abc, class_info, DottedChain.parseNoSuffix("flash.display.BitmapData"))) {
                            allowEmbed = false;
                        }
                    }

                    if (ct instanceof DefineFont4Tag) {
                        allowEmbed = true;
                    }

                    if (ct.getClassNames().size() > 1) {
                        allowEmbed = true;
                    }
                }
            }
        }
                
        //class header
        instanceInfo.getClassHeaderStr(convertData.assetsDir, writer, abc, fullyQualifiedNames, false, allowEmbed);
        writer.endTrait();
        writer.startClass(class_info);
        writer.startBlock();
        
        first.setVal(true);
                       
        ClassInfo classInfo = abc.class_info.get(class_info);
        
        //static initializer        
        int bodyIndex = abc.findBodyIndex(classInfo.cinit_index);
                
        if (bodyIndex != -1) {
            writer.startTrait(GraphTextWriter.TRAIT_CLASS_INITIALIZER);
            writer.startMethod(classInfo.cinit_index, "cinit");
            writer.append(""); 
            writer.newLine();
            writer.endMethod();
            writer.endTrait();
        } else {
            writer.newLine();
        }
        
        //static variables & constants
        classInfo.static_traits.toString(swfVersion, packageName, first, abcIndex, new Class[]{TraitSlotConst.class}, this, convertData, path + "/" + instanceInfoName, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel, new ArrayList<>(), isInterface);

        //static initializer continue        
        if (bodyIndex != -1) {
            writer.startTrait(GraphTextWriter.TRAIT_CLASS_INITIALIZER);
            writer.startMethod(classInfo.cinit_index, "cinit");
            if (exportMode != ScriptExportMode.AS_METHOD_STUBS) {
                if (!classInitializerIsEmpty) {
                    //writer.startBlock();
                    if (!first.getVal()) {
                        writer.newLine();
                    }
                    first.setVal(false);
                    List<MethodBody> callStack = new ArrayList<>();
                    callStack.add(abc.bodies.get(bodyIndex));
                    abc.bodies.get(bodyIndex).toString(swfVersion, callStack, abcIndex, path + "/" + instanceInfoName + ".staticinitializer", exportMode, abc, this, writer, fullyQualifiedNames, new HashSet<>());
                    //first.setVal(true);
                    //writer.endBlock();
                } else {
                    //Note: There must be trait/method highlight even if the initializer is empty to TraitList in GUI to work correctly
                    //TODO: handle this better in GUI(?)
                    writer.append("");                    
                }
            }
            writer.endMethod();
            writer.endTrait();
            /*if (!classInitializerIsEmpty) {
                writer.newLine();
            }*/
        } else {
            //"/*classInitializer*/";
        }

        List<String> ignoredInstanceVariableNames = new ArrayList<>();
        if (convertData.ignoreAccessibility) {
            ignoredInstanceVariableNames.add("__setAccDict");
            ignoredInstanceVariableNames.add("__setTabDict");
            ignoredInstanceVariableNames.add("__lastFrameAcc");
            ignoredInstanceVariableNames.add("__lastFrameTab");
        }

        //instance variables
        instanceInfo.instance_traits.toString(swfVersion, packageName, first, abcIndex, new Class[]{TraitSlotConst.class}, this, convertData, path + "/" + instanceInfoName, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel, ignoredInstanceVariableNames, isInterface);

        //instance initializer - constructor
        if (!instanceInfo.isInterface()) {
            String modifier = "public ";
            Multiname m = abc.constants.getMultiname(instanceInfo.name_index);

            if (!first.getVal()) {
                writer.newLine();
            }
            first.setVal(false);
            writer.startTrait(GraphTextWriter.TRAIT_INSTANCE_INITIALIZER);
            writer.startMethod(instanceInfo.iinit_index, "iinit");
            writer.appendNoHilight(modifier);
            writer.appendNoHilight("function ");
            writer.appendNoHilight(m.getName(abc.constants, null/*do not want full names here*/, false, true));
            writer.appendNoHilight("(");
            bodyIndex = abc.findBodyIndex(instanceInfo.iinit_index);
            MethodBody body = bodyIndex == -1 ? null : abc.bodies.get(bodyIndex);
            abc.method_info.get(instanceInfo.iinit_index).getParamStr(writer, abc.constants, body, abc, fullyQualifiedNames);
            writer.appendNoHilight(")").startBlock();
            if (exportMode != ScriptExportMode.AS_METHOD_STUBS) {
                if (body != null) {
                    List<MethodBody> callStack = new ArrayList<>();
                    callStack.add(body);
                    body.toString(swfVersion, callStack, abcIndex, path + "/" + instanceInfoName + ".initializer", exportMode, abc, this, writer, fullyQualifiedNames, new HashSet<>());
                }
            }

            writer.endBlock().newLine();
            writer.endMethod();
            writer.endTrait();
        }

        //static methods
        classInfo.static_traits.toString(swfVersion, packageName, first, abcIndex, new Class[]{TraitClass.class, TraitFunction.class, TraitMethodGetterSetter.class}, this, convertData, path + "/" + instanceInfoName, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel, new ArrayList<>(), isInterface);

        List<String> ignoredInstanceTraitNames = new ArrayList<>();
        if (convertData.ignoreFrameScripts) {
            ignoredInstanceTraitNames.addAll(frameTraitNames);
        }
        if (convertData.ignoreAccessibility) {
            for (Trait t : instanceInfo.instance_traits.traits) {
                String traitName = t.getName(abc).getName(abc.constants, new ArrayList<>(), true, false);;
                if (traitName.startsWith("__setAcc_")
                        || traitName.startsWith("__setTab_")) {
                    ignoredInstanceTraitNames.add(traitName);
                }
            }
        }

        //instance methods
        instanceInfo.instance_traits.toString(swfVersion, packageName, first, abcIndex, new Class[]{TraitClass.class, TraitFunction.class, TraitMethodGetterSetter.class}, this, convertData, path + "/" + instanceInfoName, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel, ignoredInstanceTraitNames, isInterface);

        if (first.getVal()) {
            writer.newLine();
        }
        writer.endClass();
        writer.endBlock(); // class
        writer.newLine();
        return writer;
    }

    @Override
    public void convert(int swfVersion, AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {

        fullyQualifiedNames = new ArrayList<>();

        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        String instanceInfoName = instanceInfo.getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false, true);
        ClassInfo classInfo = abc.class_info.get(class_info);

        AbcIndexing index = new AbcIndexing(abc.getSwf());
        //for simplification of String(this)
        int sIndex = abc.constants.getStringId("", false);
        if (sIndex > -1) {
            int nsIndex = abc.constants.getNamespaceId(Namespace.KIND_PACKAGE, DottedChain.TOPLEVEL, sIndex, false);
            if (nsIndex > -1) {
                convertData.thisHasDefaultToPrimitive = null == index.findProperty(new AbcIndexing.PropertyDef("toString", new TypeItem(instanceInfo.getName(abc.constants).getNameWithNamespace(abc.constants, true)), abc, nsIndex), false, true, false);
            } else {
                convertData.thisHasDefaultToPrimitive = true;
            }
        } else {
            convertData.thisHasDefaultToPrimitive = true;
        }
        ScopeStack newScopeStack = (ScopeStack) scopeStack.clone();
        //class initializer
        int bodyIndex = abc.findBodyIndex(classInfo.cinit_index);
        if (bodyIndex != -1) {
            writer.mark();
            List<MethodBody> callStack = new ArrayList<>();
            callStack.add(abc.bodies.get(bodyIndex));

            if (!abc.instance_info.get(class_info).isInterface()) {
                AbcIndexing.ClassIndex cls = abcIndex.findClass(AbcIndexing.multinameToType(abc.instance_info.get(class_info).name_index, abc.constants), abc, scriptIndex);
                List<AbcIndexing.ClassIndex> clsList = new ArrayList<>();
                cls = cls.parent;
                while (cls != null) {
                    clsList.add(0, cls);
                    cls = cls.parent;
                }
                for (AbcIndexing.ClassIndex cls2 : clsList) {
                    newScopeStack.push(new ClassAVM2Item(cls2.abc.instance_info.get(cls2.index).getName(cls2.abc.constants).getNameWithNamespace(cls2.abc.constants, true)));
                }
            }

            abc.bodies.get(bodyIndex).convert(swfVersion, callStack, abcIndex, convertData, path + "/" + instanceInfoName + ".staticinitializer", exportMode, true, classInfo.cinit_index, scriptIndex, class_info, abc, this, newScopeStack, GraphTextWriter.TRAIT_CLASS_INITIALIZER, writer, fullyQualifiedNames, classInfo.static_traits, true, new HashSet<>(), new ArrayList<>());

            newScopeStack.push(new ClassAVM2Item(abc.instance_info.get(class_info).getName(abc.constants)));
            classInitializerIsEmpty = !writer.getMark();
        }

        //constructor - instance initializer
        if (!instanceInfo.isInterface()) {
            bodyIndex = abc.findBodyIndex(instanceInfo.iinit_index);
            if (bodyIndex != -1) {
                MethodBody constructorBody = abc.bodies.get(bodyIndex);
                List<MethodBody> callStack = new ArrayList<>();
                callStack.add(constructorBody);
                constructorBody.convert(swfVersion, callStack, abcIndex, convertData, path + "/" + instanceInfoName + ".initializer", exportMode, false, instanceInfo.iinit_index, scriptIndex, class_info, abc, this, new ScopeStack(), GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, writer, fullyQualifiedNames, instanceInfo.instance_traits, true, new HashSet<>(), new ArrayList<>());

                if (convertData.ignoreFrameScripts) {
                    //find all addFrameScript(xx,this.method) in constructor
                    /*
                It looks like this:
                CallPropertyAVM2Item
                ->propertyName == FullMultinameAVM2Item
                        -> resolvedMultinameName (String) "addFrameScript"
                ->arguments
                        ->0 IntegerValueAVM2Item
                                ->value (Long) 0    - zero based
                        ->1 GetPropertyAVM2Item
                                ->object (ThisAVM2Item)
                                ->propertyName (FullMultinameAvm2Item)
                                        ->multinameIndex
                                        ->resolvedMultinameName (String) "frame1"
                     */
                    if (constructorBody.convertedItems != null) {
                        for (int j = 0; j < constructorBody.convertedItems.size(); j++) {
                            GraphTargetItem ti = constructorBody.convertedItems.get(j);
                            if (ti instanceof CallPropertyAVM2Item) {
                                CallPropertyAVM2Item callProp = (CallPropertyAVM2Item) ti;
                                if (callProp.propertyName instanceof FullMultinameAVM2Item) {
                                    FullMultinameAVM2Item propName = (FullMultinameAVM2Item) callProp.propertyName;
                                    if ("addFrameScript".equals(propName.resolvedMultinameName)) {
                                        for (int i = 0; i < callProp.arguments.size(); i += 2) {
                                            if (callProp.arguments.get(i) instanceof IntegerValueAVM2Item) {
                                                if (callProp.arguments.get(i + 1) instanceof GetLexAVM2Item) {
                                                    GetLexAVM2Item lex = (GetLexAVM2Item) callProp.arguments.get(i + 1);
                                                    frameTraitNames.add(lex.propertyName.getName(abc.constants, new ArrayList<>(), false, true));
                                                } else if (callProp.arguments.get(i + 1) instanceof GetPropertyAVM2Item) {
                                                    GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) callProp.arguments.get(i + 1);
                                                    if (getProp.object instanceof ThisAVM2Item) {
                                                        if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                                            FullMultinameAVM2Item framePropName = (FullMultinameAVM2Item) getProp.propertyName;
                                                            int multinameIndex = framePropName.multinameIndex;
                                                            frameTraitNames.add(abc.constants.getMultiname(multinameIndex).getName(abc.constants, new ArrayList<>(), false, true));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        constructorBody.convertedItems.remove(j);
                                        j--;
                                    } else if (propName.resolvedMultinameName != null
                                            && (propName.resolvedMultinameName.startsWith("__setAcc_")
                                            || propName.resolvedMultinameName.startsWith("__setTab_"))
                                            && callProp.arguments.isEmpty()) {
                                        //accessibilityTraitNames.add(propName.resolvedMultinameName);
                                        constructorBody.convertedItems.remove(j);
                                        j--;
                                    }
                                }
                            }
                        }
                    }
                }

                if (convertData.ignoreAccessibility) {
                    if (constructorBody.convertedItems != null) {
                        for (int j = 0; j < constructorBody.convertedItems.size(); j++) {
                            GraphTargetItem ti = constructorBody.convertedItems.get(j);
                            if (ti instanceof CallPropertyAVM2Item) {
                                CallPropertyAVM2Item callProp = (CallPropertyAVM2Item) ti;

                                if (callProp.propertyName instanceof FullMultinameAVM2Item) {
                                    FullMultinameAVM2Item propName = (FullMultinameAVM2Item) callProp.propertyName;
                                    if ("addEventListener".equals(propName.resolvedMultinameName)) {
                                        //addEventListener(Event.FRAME_CONSTRUCTED,this.__setAcc_handler,false,0,true);
                                        if (callProp.arguments.size() != 5) {
                                            continue;
                                        }
                                        if (!(callProp.arguments.get(0) instanceof GetPropertyAVM2Item)) {
                                            continue;
                                        }
                                        GetPropertyAVM2Item gp = (GetPropertyAVM2Item) callProp.arguments.get(0);
                                        if (!(gp.propertyName instanceof FullMultinameAVM2Item)) {
                                            continue;
                                        }
                                        FullMultinameAVM2Item fm = (FullMultinameAVM2Item) gp.propertyName;
                                        if (!"FRAME_CONSTRUCTED".equals(fm.resolvedMultinameName)) {
                                            continue;
                                        }
                                        if (!(callProp.arguments.get(1) instanceof GetPropertyAVM2Item)) {
                                            continue;
                                        }
                                        gp = (GetPropertyAVM2Item) callProp.arguments.get(1);
                                        if (!(gp.propertyName instanceof FullMultinameAVM2Item)) {
                                            continue;
                                        }
                                        fm = (FullMultinameAVM2Item) gp.propertyName;
                                        if (!("__setAcc_handler".equals(fm.resolvedMultinameName)
                                                || "__setTab_handler".equals(fm.resolvedMultinameName))) {
                                            continue;
                                        }
                                        constructorBody.convertedItems.remove(j);
                                        j--;
                                    }

                                    if (propName.resolvedMultinameName != null
                                            && (propName.resolvedMultinameName.startsWith("__setAcc_")
                                            || propName.resolvedMultinameName.startsWith("__setTab_"))
                                            && callProp.arguments.isEmpty()) {
                                        //accessibilityTraitNames.add(propName.resolvedMultinameName);
                                        constructorBody.convertedItems.remove(j);
                                        j--;
                                    }
                                }
                            }
                            if (ti instanceof SetPropertyAVM2Item) {
                                if (ti.value instanceof ConstructPropAVM2Item) {
                                    ConstructPropAVM2Item cons = (ConstructPropAVM2Item) ti.value;
                                    if (cons.propertyName instanceof FullMultinameAVM2Item) {
                                        FullMultinameAVM2Item fm = (FullMultinameAVM2Item) cons.propertyName;
                                        if ("AccessibilityProperties".equals(fm.resolvedMultinameName)) {
                                            constructorBody.convertedItems.remove(j);
                                            j--;
                                            continue;
                                        }
                                    }
                                }
                                SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) ti;
                                if (setProp.object instanceof GetPropertyAVM2Item) {
                                    GetPropertyAVM2Item parentGetProp = (GetPropertyAVM2Item) setProp.object;
                                    if (parentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                        FullMultinameAVM2Item parentProp = (FullMultinameAVM2Item) parentGetProp.propertyName;
                                        if ("accessibilityProperties".equals(parentProp.resolvedMultinameName)) {
                                            if (parentGetProp.object instanceof GetPropertyAVM2Item) {
                                                GetPropertyAVM2Item parentParentGetProp = (GetPropertyAVM2Item) parentGetProp.object;
                                                if (parentParentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                    FullMultinameAVM2Item parentParentProp = (FullMultinameAVM2Item) parentParentGetProp.propertyName;
                                                    if ("root".equals(parentParentProp.resolvedMultinameName)) {
                                                        if (parentParentGetProp.object instanceof ThisAVM2Item) {
                                                            constructorBody.convertedItems.remove(j);
                                                            j--;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //static variables,constants & methods
        classInfo.static_traits.convert(swfVersion, abcIndex, this, convertData, path + "/" + instanceInfoName, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel, newScopeStack);

        instanceInfo.instance_traits.convert(swfVersion, abcIndex, this, convertData, path + "/" + instanceInfoName, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel, newScopeStack);
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
        ClassInfo classInfo = abc.class_info.get(class_info);
        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        int iInitializer = abc.findBodyIndex(instanceInfo.iinit_index);
        int ret = 0;
        if (iInitializer != -1) {
            ret += abc.bodies.get(iInitializer).removeTraps(abc, this, scriptIndex, class_info, false, path);
        }
        int sInitializer = abc.findBodyIndex(classInfo.cinit_index);
        if (sInitializer != -1) {
            ret += abc.bodies.get(sInitializer).removeTraps(abc, this, scriptIndex, class_info, true, path);
        }
        ret += instanceInfo.instance_traits.removeTraps(scriptIndex, class_info, false, abc, path);
        ret += classInfo.static_traits.removeTraps(scriptIndex, class_info, true, abc, path);
        return ret;
    }

    /**
     * Clones trait.
     *
     * @return Cloned trait
     */
    @Override
    public TraitClass clone() {
        TraitClass ret = (TraitClass) super.clone();
        return ret;
    }

    /**
     * Converts trait.
     *
     * @param abc ABC
     * @param writer Writer
     * @return Writer
     */
    @Override
    public GraphTextWriter convertTraitHeader(ABC abc, GraphTextWriter writer) {
        convertCommonHeaderFlags("class", abc, writer);
        writer.newLine();
        writer.appendNoHilight("slotid ");
        writer.hilightSpecial(Integer.toString(slot_id), HighlightSpecialType.SLOT_ID);
        writer.newLine();
        writer.appendNoHilight("class").newLine();
        writer.indent();
        InstanceInfo ii = abc.instance_info.get(class_info);
        writer.appendNoHilight("instance ").hilightSpecial(abc.constants.multinameToString(ii.name_index), HighlightSpecialType.INSTANCE_NAME).newLine();
        writer.indent();
        writer.appendNoHilight("extends ").hilightSpecial(abc.constants.multinameToString(ii.super_index), HighlightSpecialType.EXTENDS).newLine();
        for (int iface : ii.interfaces) {
            writer.appendNoHilight("implements ").hilightSpecial(abc.constants.multinameToString(iface), HighlightSpecialType.IMPLEMENTS).newLine();
        }
        if ((ii.flags & InstanceInfo.CLASS_SEALED) == InstanceInfo.CLASS_SEALED) {
            writer.appendNoHilight("flag SEALED").newLine();
        }
        if ((ii.flags & InstanceInfo.CLASS_FINAL) == InstanceInfo.CLASS_FINAL) {
            writer.appendNoHilight("flag FINAL").newLine();
        }
        if ((ii.flags & InstanceInfo.CLASS_INTERFACE) == InstanceInfo.CLASS_INTERFACE) {
            writer.appendNoHilight("flag INTERFACE").newLine();
        }
        if ((ii.flags & InstanceInfo.CLASS_PROTECTEDNS) == InstanceInfo.CLASS_PROTECTEDNS) {
            writer.appendNoHilight("flag PROTECTEDNS").newLine();
        }
        if ((ii.flags & InstanceInfo.CLASS_NON_NULLABLE) == InstanceInfo.CLASS_NON_NULLABLE) {
            writer.appendNoHilight("flag NON_NULLABLE").newLine();
        }
        if ((ii.flags & InstanceInfo.CLASS_PROTECTEDNS) == InstanceInfo.CLASS_PROTECTEDNS) {
            writer.appendNoHilight("protectedns ").hilightSpecial(Multiname.namespaceToString(abc.constants, ii.protectedNS), HighlightSpecialType.PROTECTEDNS).newLine();
        }
        writer.unindent();
        writer.appendNoHilight("end ; instance").newLine();
        writer.unindent();
        writer.appendNoHilight("end ; class").newLine();
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
        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        ClassInfo classInfo = abc.class_info.get(class_info);

        //class initializer
        methodInfos.add(new MethodId(GraphTextWriter.TRAIT_CLASS_INITIALIZER, class_info, classInfo.cinit_index));

        //constructor - instance initializer
        methodInfos.add(new MethodId(GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, class_info, instanceInfo.iinit_index));

        //static variables,constants & methods
        classInfo.static_traits.getMethodInfos(abc, true, class_info, methodInfos);

        instanceInfo.instance_traits.getMethodInfos(abc, false, class_info, methodInfos);
    }
}
