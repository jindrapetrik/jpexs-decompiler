/*
 *  Copyright (C) 2010-2016 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TraitClass extends Trait implements TraitWithSlot {

    public int slot_id;

    public int class_info;

    private boolean classInitializerIsEmpty;

    @Override
    public void delete(ABC abc, boolean d) {
        ClassInfo classInfo = abc.class_info.get(class_info);
        classInfo.deleted = d;
        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        instanceInfo.deleted = d;

        classInfo.static_traits.delete(abc, d);
        abc.method_info.get(classInfo.cinit_index).delete(abc, d);

        instanceInfo.instance_traits.delete(abc, d);
        abc.method_info.get(instanceInfo.iinit_index).delete(abc, d);

        int protectedNS = instanceInfo.protectedNS;
        if (protectedNS != 0) {
            abc.constants.getNamespace(protectedNS).deleted = d;
        }

        abc.constants.getMultiname(name_index).deleted = d;
    }

    @Override
    public int getSlotIndex() {
        return slot_id;
    }

    @Override
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return "Class " + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " class_info=" + class_info + " metadata=" + Helper.intArrToString(metadata);
    }

    @Override
    public void getImportsUsages(String customNs, ABC abc, List<DottedChain> imports, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) {
        super.getImportsUsages(customNs, abc, imports, uses, ignorePackage == null ? getPackage(abc) : ignorePackage, fullyQualifiedNames);
        ClassInfo classInfo = abc.class_info.get(class_info);
        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        DottedChain packageName = instanceInfo.getName(abc.constants).getNamespace(abc.constants).getName(abc.constants); //assume not null name

        parseImportsUsagesFromMultiname(customNs, abc, imports, uses, abc.constants.getMultiname(instanceInfo.name_index), packageName, fullyQualifiedNames);

        if (instanceInfo.super_index > 0) {
            parseImportsUsagesFromMultiname(customNs, abc, imports, uses, abc.constants.getMultiname(instanceInfo.super_index), packageName, fullyQualifiedNames);
        }
        for (int i : instanceInfo.interfaces) {
            parseImportsUsagesFromMultiname(customNs, abc, imports, uses, abc.constants.getMultiname(i), packageName, fullyQualifiedNames);
        }

        //static
        classInfo.static_traits.getImportsUsages(customNs, abc, imports, uses, packageName, fullyQualifiedNames);

        //static initializer
        parseImportsUsagesFromMethodInfo(customNs, abc, classInfo.cinit_index, imports, uses, packageName, fullyQualifiedNames, new ArrayList<>());

        //instance
        instanceInfo.instance_traits.getImportsUsages(customNs, abc, imports, uses, packageName, fullyQualifiedNames);

        //instance initializer
        parseImportsUsagesFromMethodInfo(customNs, abc, instanceInfo.iinit_index, imports, uses, packageName, fullyQualifiedNames, new ArrayList<>());
    }

    @Override
    public GraphTextWriter toStringHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) {
        abc.instance_info.get(class_info).getClassHeaderStr(writer, abc, fullyQualifiedNames, false);
        return writer;
    }

    @Override
    public void convertHeader(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) {
    }

    @Override
    public GraphTextWriter toString(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {

        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        Multiname instanceInfoMultiname = instanceInfo.getName(abc.constants);
        DottedChain packageName = instanceInfoMultiname.getNamespace(abc.constants).getName(abc.constants); //assume not null name

        fullyQualifiedNames = new ArrayList<>();
        writeImportsUsages(abc, writer, packageName, fullyQualifiedNames);

        String instanceInfoName = instanceInfoMultiname.getName(abc.constants, fullyQualifiedNames, false);

        writer.startClass(class_info);

        getMetaData(abc, writer);
        //class header
        instanceInfo.getClassHeaderStr(writer, abc, fullyQualifiedNames, false);
        writer.startBlock();

        //static variables & constants
        ClassInfo classInfo = abc.class_info.get(class_info);
        classInfo.static_traits.toString(new Class[]{TraitSlotConst.class}, this, convertData, path +/*packageName +*/ "/" + instanceInfoName, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        //static initializer
        int bodyIndex = abc.findBodyIndex(classInfo.cinit_index);
        if (bodyIndex != -1) {
            writer.startTrait(GraphTextWriter.TRAIT_CLASS_INITIALIZER);
            writer.startMethod(classInfo.cinit_index);
            if (!classInitializerIsEmpty) {
                writer.startBlock();
                abc.bodies.get(bodyIndex).toString(path +/*packageName +*/ "/" + instanceInfoName + ".staticinitializer", exportMode, abc, this, writer, fullyQualifiedNames);
                writer.endBlock();
            } else {
                //Note: There must be trait/method highlight even if the initializer is empty to TraitList in GUI to work correctly
                //TODO: handle this better in GUI(?)
                writer.append(" ");
            }
            writer.endMethod();
            writer.endTrait();
            if (!classInitializerIsEmpty) {
                writer.newLine();
            }
        } else {
            //"/*classInitializer*/";
        }

        //instance variables
        instanceInfo.instance_traits.toString(new Class[]{TraitSlotConst.class}, this, convertData, path +/*packageName +*/ "/" + instanceInfoName, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        //instance initializer - constructor
        if (!instanceInfo.isInterface()) {
            String modifier = "";
            Multiname m = abc.constants.getMultiname(instanceInfo.name_index);
            Namespace ns = m.getNamespace(abc.constants);
            if (ns != null) {
                modifier = ns.getPrefix(abc) + " ";
                if (modifier.equals(" ")) {
                    modifier = "";
                }
                if (modifier.startsWith("private")) { //cannot have private constuctor
                    modifier = "";
                }
            }

            writer.newLine();
            writer.startTrait(GraphTextWriter.TRAIT_INSTANCE_INITIALIZER);
            writer.startMethod(instanceInfo.iinit_index);
            writer.appendNoHilight(modifier);
            writer.appendNoHilight("function ");
            writer.appendNoHilight(m.getName(abc.constants, null/*do not want full names here*/, false));
            writer.appendNoHilight("(");
            bodyIndex = abc.findBodyIndex(instanceInfo.iinit_index);
            MethodBody body = bodyIndex == -1 ? null : abc.bodies.get(bodyIndex);
            abc.method_info.get(instanceInfo.iinit_index).getParamStr(writer, abc.constants, body, abc, fullyQualifiedNames);
            writer.appendNoHilight(")").startBlock();
            if (body != null) {
                body.toString(path +/*packageName +*/ "/" + instanceInfoName + ".initializer", exportMode, abc, this, writer, fullyQualifiedNames);
            }

            writer.endBlock().newLine();
            writer.endMethod();
            writer.endTrait();
        }

        //static methods
        classInfo.static_traits.toString(new Class[]{TraitClass.class, TraitFunction.class, TraitMethodGetterSetter.class}, this, convertData, path +/*packageName +*/ "/" + instanceInfoName, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        //instance methods
        instanceInfo.instance_traits.toString(new Class[]{TraitClass.class, TraitFunction.class, TraitMethodGetterSetter.class}, this, convertData, path +/*packageName +*/ "/" + instanceInfoName, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        writer.endBlock(); // class
        writer.endClass();
        writer.newLine();
        return writer;
    }

    @Override
    public void convert(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {

        fullyQualifiedNames = new ArrayList<>();

        InstanceInfo instanceInfo = abc.instance_info.get(class_info);
        String instanceInfoName = instanceInfo.getName(abc.constants).getName(abc.constants, fullyQualifiedNames, false);
        ClassInfo classInfo = abc.class_info.get(class_info);

        //class initializer
        int bodyIndex = abc.findBodyIndex(classInfo.cinit_index);
        if (bodyIndex != -1) {
            writer.mark();
            List<Traits> ts = new ArrayList<>();
            ts.add(classInfo.static_traits);
            abc.bodies.get(bodyIndex).convert(convertData, path +/*packageName +*/ "/" + instanceInfoName + ".staticinitializer", exportMode, true, classInfo.cinit_index, scriptIndex, class_info, abc, this, new ScopeStack(), GraphTextWriter.TRAIT_CLASS_INITIALIZER, writer, fullyQualifiedNames, ts, true);
            classInitializerIsEmpty = !writer.getMark();
        }

        //constructor - instance initializer
        if (!instanceInfo.isInterface()) {
            bodyIndex = abc.findBodyIndex(instanceInfo.iinit_index);
            if (bodyIndex != -1) {
                List<Traits> ts = new ArrayList<>();
                ts.add(instanceInfo.instance_traits);
                abc.bodies.get(bodyIndex).convert(convertData, path +/*packageName +*/ "/" + instanceInfoName + ".initializer", exportMode, false, instanceInfo.iinit_index, scriptIndex, class_info, abc, this, new ScopeStack(), GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, writer, fullyQualifiedNames, ts, true);
            }
        }

        //static variables,constants & methods
        classInfo.static_traits.convert(this, convertData, path +/*packageName +*/ "/" + instanceInfoName, abc, true, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);

        instanceInfo.instance_traits.convert(this, convertData, path +/*packageName +*/ "/" + instanceInfoName, abc, false, exportMode, false, scriptIndex, class_info, writer, fullyQualifiedNames, parallel);
    }

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

    @Override
    public TraitClass clone() {
        TraitClass ret = (TraitClass) super.clone();
        return ret;
    }
}
