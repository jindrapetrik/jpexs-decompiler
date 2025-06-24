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
package com.jpexs.decompiler.flash.abc.usages;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.abc.usages.multinames.ClassNameMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.ConstVarNameMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.ConstVarTypeMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.MethodBodyMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.MethodNameMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.MethodParamsMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.MethodReturnTypeMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.SuperClassMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.SuperInterfaceMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.TraitMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.TypeNameMultinameUsage;
import java.util.ArrayList;
import java.util.List;

/**
 * Multiname usage detector.
 *
 * @author JPEXS
 */
public class MultinameUsageDetector implements UsageDetector {

    @Override
    public List<Usage> findUsages(ABC abc, int index) {
        return findMultinameUsage(abc, index, true);
    }

    public List<Usage> findMultinameUsage(ABC abc, int multinameIndex, boolean exactMatch) {
        List<Usage> ret = new ArrayList<>();
        if (multinameIndex == 0) {
            return ret;
        }
        for (int s = 0; s < abc.script_info.size(); s++) {
            checkMultinameUsedInMethod(abc, multinameIndex, exactMatch, abc.script_info.get(s).init_index, ret, s, -1, 0, TraitMultinameUsage.TRAITS_TYPE_SCRIPT, true, null, -1);
            findMultinameUsageInTraits(abc, abc.script_info.get(s).traits, multinameIndex, exactMatch, TraitMultinameUsage.TRAITS_TYPE_SCRIPT, s, -1, ret, -1);
        }
        loopm:
        for (int t = 1; t < abc.constants.getMultinameCount(); t++) {
            Multiname multiname = abc.constants.getMultiname(t);
            if (multiname.kind == Multiname.TYPENAME) {
                if (multiname.qname_index == multinameIndex) {
                    ret.add(new TypeNameMultinameUsage(abc, multinameIndex, t, -1));
                    continue;
                }
                for (int mp : multiname.params) {
                    if (mp == multinameIndex) {
                        ret.add(new TypeNameMultinameUsage(abc, multinameIndex, t, -1));
                        continue loopm;
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public List<List<Usage>> findAllUsage(ABC abc) {
        List<List<Usage>> ret = new ArrayList<>();
        for (int i = 0; i < abc.constants.getMultinameCount(); i++) {
            ret.add(new ArrayList<>());
        }

        for (int s = 0; s < abc.script_info.size(); s++) {
            checkAllMultinameUsedInMethod(abc, abc.script_info.get(s).init_index, ret, s, -1, 0, TraitMultinameUsage.TRAITS_TYPE_SCRIPT, true, null, -1);
            findAllMultinameUsageInTraits(abc, abc.script_info.get(s).traits, TraitMultinameUsage.TRAITS_TYPE_SCRIPT, s, -1, ret, -1);
        }

        boolean[] foundMultinames = new boolean[abc.constants.getMultinameCount()];
        for (int t = 1; t < abc.constants.getMultinameCount(); t++) {
            Multiname multiname = abc.constants.getMultiname(t);
            if (multiname.kind == Multiname.TYPENAME) {
                if (!foundMultinames[multiname.qname_index]) {
                    ret.get(multiname.qname_index).add(new TypeNameMultinameUsage(abc, multiname.qname_index, t, -1));
                    foundMultinames[multiname.qname_index] = true;
                }
                for (int mp : multiname.params) {
                    if (!foundMultinames[mp]) {
                        ret.get(mp).add(new TypeNameMultinameUsage(abc, mp, t, -1));
                        foundMultinames[mp] = true;
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public String getKind() {
        return "string";
    }

    private void findAllMultinameUsageInTraits(ABC abc, Traits traits, int traitsType, int scriptIndex, int classIndex, List<List<Usage>> ret, int parentTraitIndex) {
        for (int t = 0; t < traits.traits.size(); t++) {
            if (traits.traits.get(t) instanceof TraitClass) {
                TraitClass tc = (TraitClass) traits.traits.get(t);
                ret.get(tc.name_index).add(new ClassNameMultinameUsage(abc, tc.name_index, tc.class_info, scriptIndex));

                int c = tc.class_info;

                int classNameMultinameIndex = abc.instance_info.get(c).name_index;
                ret.get(classNameMultinameIndex).add(new ClassNameMultinameUsage(abc, classNameMultinameIndex, c, scriptIndex));
                int extendsMultinameIndex = abc.instance_info.get(c).super_index;
                ret.get(extendsMultinameIndex).add(new SuperClassMultinameUsage(abc, extendsMultinameIndex, c, scriptIndex));
                for (int i = 0; i < abc.instance_info.get(c).interfaces.length; i++) {
                    int implementsMultinameIndex = abc.instance_info.get(c).interfaces[i];
                    ret.get(implementsMultinameIndex).add(new SuperInterfaceMultinameUsage(abc, implementsMultinameIndex, c, scriptIndex));
                }
                checkAllMultinameUsedInMethod(abc, abc.instance_info.get(c).iinit_index, ret, -1/*FIXME*/, c, 0, TraitMultinameUsage.TRAITS_TYPE_INSTANCE, true, null, -1);
                checkAllMultinameUsedInMethod(abc, abc.class_info.get(c).cinit_index, ret, -1/*FIXME*/, c, 0, TraitMultinameUsage.TRAITS_TYPE_CLASS, true, null, -1);
                findAllMultinameUsageInTraits(abc, abc.instance_info.get(c).instance_traits, TraitMultinameUsage.TRAITS_TYPE_INSTANCE, -1/*FIXME*/, c, ret, -1);
                findAllMultinameUsageInTraits(abc, abc.class_info.get(c).static_traits, TraitMultinameUsage.TRAITS_TYPE_CLASS, -1/*FIXME*/, c, ret, -1);
            }
            if (traits.traits.get(t) instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) traits.traits.get(t);
                ret.get(tsc.name_index).add(new ConstVarNameMultinameUsage(abc, tsc.name_index, scriptIndex, classIndex, t, traitsType, traits, parentTraitIndex));
                ret.get(tsc.type_index).add(new ConstVarTypeMultinameUsage(abc, tsc.type_index, scriptIndex, classIndex, t, traitsType, traits, parentTraitIndex));
            }
            if (traits.traits.get(t) instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) traits.traits.get(t);
                ret.get(tmgs.name_index).add(new MethodNameMultinameUsage(abc, tmgs.name_index, scriptIndex, classIndex, t, traitsType, false, traits, parentTraitIndex));
                checkAllMultinameUsedInMethod(abc, tmgs.method_info, ret, scriptIndex, classIndex, t, traitsType, false, traits, parentTraitIndex);
            }
        }
    }

    private void checkAllMultinameUsedInMethod(ABC abc, int methodInfo, List<List<Usage>> ret, int scriptIndex, int classIndex, int traitIndex, int traitsType, boolean isInitializer, Traits traits, int parentTraitIndex) {
        boolean[] foundMultinames = new boolean[abc.constants.getMultinameCount()];
        for (int p = 0; p < abc.method_info.get(methodInfo).param_types.length; p++) {
            int methodParamsMultinameIndex = abc.method_info.get(methodInfo).param_types[p];
            if (!foundMultinames[methodParamsMultinameIndex]) {
                ret.get(methodParamsMultinameIndex).add(new MethodParamsMultinameUsage(abc, methodParamsMultinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                foundMultinames[methodParamsMultinameIndex] = true;
            }
        }
        int methodReturnTypeMultinameIndex = abc.method_info.get(methodInfo).ret_type;
        ret.get(methodReturnTypeMultinameIndex).add(new MethodReturnTypeMultinameUsage(abc, methodReturnTypeMultinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));

        MethodBody body = abc.findBody(methodInfo);
        if (body != null) {
            findAllMultinameUsageInTraits(abc, body.traits, traitsType, scriptIndex, classIndex, ret, traitIndex);
            foundMultinames = new boolean[abc.constants.getMultinameCount()];
            for (ABCException e : body.exceptions) {
                if (!foundMultinames[e.name_index]) {
                    ret.get(e.name_index).add(new MethodBodyMultinameUsage(abc, e.name_index, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                    foundMultinames[e.name_index] = true;
                }

                if (!foundMultinames[e.type_index]) {
                    ret.get(e.type_index).add(new MethodBodyMultinameUsage(abc, e.type_index, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                    foundMultinames[e.type_index] = true;
                }
            }
            for (AVM2Instruction ins : body.getCode().code) {
                for (int o = 0; o < ins.definition.operands.length; o++) {
                    if (ins.definition.operands[o] == AVM2Code.DAT_MULTINAME_INDEX) {
                        int mi = ins.operands[o];
                        if (mi < foundMultinames.length && !foundMultinames[mi]) {
                            ret.get(mi).add(new MethodBodyMultinameUsage(abc, mi, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                            foundMultinames[mi] = true;
                        }
                    }
                }
            }
        }
    }

    private boolean isSameName(ABC abc, int expectedQNameIndex, int checkedNameIndex, boolean exactMatch) {
        if (expectedQNameIndex == checkedNameIndex) {
            return true;
        }
        if (exactMatch) {
            return false;
        }
        Multiname expectedQName = abc.constants.getMultiname(expectedQNameIndex);
        Multiname checkedName = abc.constants.getMultiname(checkedNameIndex);
        if (checkedName == null) {
            return false;
        }

        if (expectedQName.name_index != checkedName.name_index) {
            return false;
        }
        if (checkedName.kind == Multiname.QNAME) {
            return expectedQName.namespace_index == checkedName.namespace_index;
        }
        if (checkedName.kind != Multiname.MULTINAME) {
            return false;
        }
        for (int ns : abc.constants.getNamespaceSet(checkedName.namespace_set_index).namespaces) {
            if (ns == expectedQName.namespace_index) {
                return true;
            }
        }
        return false;
    }

    private void findMultinameUsageInTraits(ABC abc, Traits traits, int multinameIndex, boolean exactMatch, int traitsType, int scriptIndex, int classIndex, List<Usage> ret, int parentTraitIndex) {
        for (int t = 0; t < traits.traits.size(); t++) {
            if (traits.traits.get(t) instanceof TraitClass) {
                TraitClass tc = (TraitClass) traits.traits.get(t);
                if (isSameName(abc, multinameIndex, tc.name_index, exactMatch)) {
                    ret.add(new ClassNameMultinameUsage(abc, multinameIndex, tc.class_info, scriptIndex));
                }
                int c = tc.class_info;
                if (isSameName(abc, multinameIndex, abc.instance_info.get(c).super_index, exactMatch)) {
                    ret.add(new SuperClassMultinameUsage(abc, multinameIndex, c, scriptIndex));
                }
                for (int i = 0; i < abc.instance_info.get(c).interfaces.length; i++) {
                    if (isSameName(abc, multinameIndex, abc.instance_info.get(c).interfaces[i], exactMatch)) {
                        ret.add(new SuperInterfaceMultinameUsage(abc, multinameIndex, c, scriptIndex));
                    }
                }
                checkMultinameUsedInMethod(abc, multinameIndex, exactMatch, abc.instance_info.get(c).iinit_index, ret, -1/*FIXME*/, c, 0, TraitMultinameUsage.TRAITS_TYPE_INSTANCE, true, null, -1);
                checkMultinameUsedInMethod(abc, multinameIndex, exactMatch, abc.class_info.get(c).cinit_index, ret, -1/*FIXME*/, c, 0, TraitMultinameUsage.TRAITS_TYPE_CLASS, true, null, -1);
                findMultinameUsageInTraits(abc, abc.instance_info.get(c).instance_traits, multinameIndex, exactMatch, TraitMultinameUsage.TRAITS_TYPE_INSTANCE, -1/*FIXME*/, c, ret, -1);
                findMultinameUsageInTraits(abc, abc.class_info.get(c).static_traits, multinameIndex, exactMatch, TraitMultinameUsage.TRAITS_TYPE_CLASS, -1/*FIXME*/, c, ret, -1);
            }
            if (traits.traits.get(t) instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) traits.traits.get(t);
                if (isSameName(abc, multinameIndex, tsc.name_index, exactMatch)) {
                    ret.add(new ConstVarNameMultinameUsage(abc, multinameIndex, scriptIndex, classIndex, t, traitsType, traits, parentTraitIndex));
                }
                if (isSameName(abc, multinameIndex, tsc.type_index, exactMatch)) {
                    ret.add(new ConstVarTypeMultinameUsage(abc, multinameIndex, scriptIndex, classIndex, t, traitsType, traits, parentTraitIndex));
                }
            }
            if (traits.traits.get(t) instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) traits.traits.get(t);
                if (isSameName(abc, multinameIndex, tmgs.name_index, exactMatch)) {
                    ret.add(new MethodNameMultinameUsage(abc, multinameIndex, scriptIndex, classIndex, t, traitsType, false, traits, parentTraitIndex));
                }
                checkMultinameUsedInMethod(abc, multinameIndex, exactMatch, tmgs.method_info, ret, scriptIndex, classIndex, t, traitsType, false, traits, parentTraitIndex);
            }
        }
    }

    private void checkMultinameUsedInMethod(ABC abc, int multinameIndex, boolean exactMatch, int methodInfo, List<Usage> ret, int scriptIndex, int classIndex, int traitIndex, int traitsType, boolean isInitializer, Traits traits, int parentTraitIndex) {
        for (int p = 0; p < abc.method_info.get(methodInfo).param_types.length; p++) {
            if (isSameName(abc, multinameIndex, abc.method_info.get(methodInfo).param_types[p], exactMatch)) {
                ret.add(new MethodParamsMultinameUsage(abc, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                break;
            }
        }
        if (isSameName(abc, multinameIndex, abc.method_info.get(methodInfo).ret_type, exactMatch)) {
            ret.add(new MethodReturnTypeMultinameUsage(abc, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
        }
        MethodBody body = abc.findBody(methodInfo);
        if (body != null) {
            findMultinameUsageInTraits(abc, body.traits, multinameIndex, exactMatch, traitsType, scriptIndex, classIndex, ret, traitIndex);
            for (ABCException e : body.exceptions) {
                if ((isSameName(abc, multinameIndex, e.name_index, exactMatch)) || (isSameName(abc, multinameIndex, e.type_index, exactMatch))) {
                    ret.add(new MethodBodyMultinameUsage(abc, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                    return;
                }
            }
            for (AVM2Instruction ins : body.getCode().code) {
                for (int o = 0; o < ins.definition.operands.length; o++) {
                    if (ins.definition.operands[o] == AVM2Code.DAT_MULTINAME_INDEX && ins.operands[o] < abc.constants.getMultinameCount()) {
                        if (isSameName(abc, multinameIndex, ins.operands[o], exactMatch)) {
                            ret.add(new MethodBodyMultinameUsage(abc, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, isInitializer, traits, parentTraitIndex));
                            return;
                        }
                    }
                }
            }
        }
    }
}
