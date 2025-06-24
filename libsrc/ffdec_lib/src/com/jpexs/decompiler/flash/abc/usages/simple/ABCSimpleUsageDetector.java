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
package com.jpexs.decompiler.flash.abc.usages.simple;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MetadataInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple usage detector for ABC.
 *
 * @author JPEXS
 */
public class ABCSimpleUsageDetector {

    /**
     * Item kind
     */
    public static enum ItemKind {
        /**
         * Integer
         */
        INT(true),
        /**
         * Unsigned integer
         */
        UINT(true),
        /**
         * Double
         */
        DOUBLE(true),
        /**
         * Decimal - only for ABCs with decimal support
         */
        DECIMAL(true),
        /**
         * Float - only for ABCs with float support
         */
        FLOAT(true),
        /**
         * Float4 - only for ABCs with float support
         */
        FLOAT4(true),
        /**
         * String
         */
        STRING(true),
        /**
         * Namespace
         */
        NAMESPACE(true),
        /**
         * Namespace set
         */
        NAMESPACESET(true),
        /**
         * Multiname
         */
        MULTINAME(true),
        /**
         * Metadata info
         */
        METADATAINFO(false),
        /**
         * Method info
         */
        METHODINFO(false),
        /**
         * Method body
         */
        METHODBODY(false),
        /**
         * Class
         */
        CLASS(false);

        /**
         * True if has reserved zero index.
         */
        private final boolean reserveZeroIndex;

        /**
         * Constructs a new item kind.
         * @param reserveZeroIndex True if has reserved zero index
         */
        private ItemKind(boolean reserveZeroIndex) {
            this.reserveZeroIndex = reserveZeroIndex;
        }

        /**
         * Checks if has reserved zero index.
         * @return True if has reserved zero index
         */
        public boolean hasReservedZeroIndex() {
            return reserveZeroIndex;
        }
    }

    private final Map<ItemKind, List<List<String>>> usages = new HashMap<>();
    private final Map<ItemKind, List<Integer>> zeroUsages = new HashMap<>();

    private final ABC abc;

    /**
     * Constructs a new ABC simple usage detector.
     * @param abc ABC
     */
    public ABCSimpleUsageDetector(ABC abc) {
        this.abc = abc;
    }

    /**
     * Initializes usages.
     * @param kind Item kind
     * @param itemCount Item count
     */
    private void initUsages(ItemKind kind, int itemCount) {
        List<List<String>> list = new ArrayList<>();
        if (kind.hasReservedZeroIndex() && itemCount == 0) {
            itemCount = 1;
        }
        for (int i = 0; i < itemCount; i++) {
            list.add(new ArrayList<>());
        }
        usages.put(kind, list);
    }

    /**
     * Detects usages.
     */
    public void detect() {
        usages.clear();

        initUsages(ItemKind.INT, abc.constants.getIntCount());
        initUsages(ItemKind.UINT, abc.constants.getUIntCount());
        initUsages(ItemKind.DOUBLE, abc.constants.getDoubleCount());
        if (abc.hasFloatSupport()) {
            initUsages(ItemKind.FLOAT, abc.constants.getFloatCount());
        }
        if (abc.hasDecimalSupport()) {
            initUsages(ItemKind.DECIMAL, abc.constants.getDecimalCount());
        }
        
        initUsages(ItemKind.STRING, abc.constants.getStringCount());
        initUsages(ItemKind.NAMESPACE, abc.constants.getNamespaceCount());
        initUsages(ItemKind.NAMESPACESET, abc.constants.getNamespaceSetCount());
        initUsages(ItemKind.MULTINAME, abc.constants.getMultinameCount());
        initUsages(ItemKind.METADATAINFO, abc.metadata_info.size());
        initUsages(ItemKind.METHODINFO, abc.method_info.size());
        initUsages(ItemKind.METHODBODY, abc.bodies.size());
        initUsages(ItemKind.CLASS, abc.class_info.size());

        ABCWalker walker = new ABCWalker() {
            protected void handleUsageNamespace(int index, String usageDescription) {
                if (!handleUsage(ItemKind.NAMESPACE, index, usageDescription)) {
                    return;
                }
                Namespace ns = abc.constants.getNamespace(index);
                if (ns == null) {
                    return;
                }
                handleUsage(ItemKind.STRING, ns.name_index, "ns" + index + "/name");
            }

            protected void handleUsageNamespaceSet(int index, String usageDescription) {
                if (!handleUsage(ItemKind.NAMESPACESET, index, usageDescription)) {
                    return;
                }
                NamespaceSet nss = abc.constants.getNamespaceSet(index);
                if (nss == null) {
                    return;
                }
                for (int i = 0; i < nss.namespaces.length; i++) {
                    handleUsageNamespace(nss.namespaces[i], "nss" + index + "/ns" + nss.namespaces[i]);
                }
            }

            protected void handleUsageMultiname(int index, String usageDescription) {
                if (!handleUsage(ItemKind.MULTINAME, index, usageDescription)) {
                    return;
                }
                Multiname m = abc.constants.getMultiname(index);
                if (m == null) {
                    return;
                }
                if (m.hasOwnName()) {
                    handleUsage(ItemKind.STRING, m.name_index, "mn" + index + "/name");
                }
                if (m.hasOwnNamespace()) {
                    handleUsageNamespace(m.namespace_index, "mn" + index + "/namespace");
                }
                if (m.hasOwnNamespaceSet()) {
                    handleUsageNamespaceSet(m.namespace_set_index, "mn" + index + "/namespace_set");
                }
                if (m.kind == Multiname.TYPENAME) {
                    handleUsageMultiname(m.qname_index, "mn" + index + "/qname");
                    for (int i = 0; i < m.params.length; i++) {
                        handleUsageMultiname(m.params[i], "mn" + index + "/param" + i);
                    }
                }
            }

            protected void handleUsageMethodInfo(int index, String usageDescription) {
                if (!handleUsage(ItemKind.METHODINFO, index, usageDescription)) {
                    return;
                }

                MethodInfo m = abc.method_info.get(index);
                handleUsage(ItemKind.STRING, m.name_index, usageDescription + "/name");

                if (m.flagHas_paramnames()) {
                    for (int i = 0; i < m.paramNames.length; i++) {
                        handleUsage(ItemKind.STRING, m.paramNames[i], usageDescription + "/param_names/pn" + i);
                    }
                }
                if (m.flagHas_optional()) {
                    for (int i = 0; i < m.optional.length; i++) {
                        handleUsageValueKind(abc, m.optional[i].value_kind, m.optional[i].value_index, usageDescription + "/optional/op" + i);
                    }
                }

                for (int i = 0; i < m.param_types.length; i++) {
                    handleUsageMultiname(m.param_types[i], usageDescription + "/param_types/pt" + i);
                }

                handleUsageMultiname(m.ret_type, usageDescription + "/return_type");

                int bodyIndex = abc.findBodyIndex(index);
                if (bodyIndex > -1) {
                    handleUsageMethodBody(bodyIndex, usageDescription + "/method_body");
                }
            }

            protected void handleUsageMethodBody(int index, String usageDescription) {
                if (!handleUsage(ItemKind.METHODBODY, index, usageDescription)) {
                    return;
                }
                MethodBody body = abc.bodies.get(index);
                for (int i = 0; i < body.exceptions.length; i++) {
                    handleUsageMultiname(body.exceptions[i].name_index, usageDescription + "/exceptions/ex" + i + "/name");
                    handleUsageMultiname(body.exceptions[i].type_index, usageDescription + "/exceptions/ex" + i + "/type");
                }
                List<AVM2Instruction> code = body.getCode().code;
                for (int i = 0; i < code.size(); i++) {
                    AVM2Instruction ins = code.get(i);
                    for (int operandIndex = 0; operandIndex < ins.definition.operands.length; operandIndex++) {
                        int operand = ins.operands[operandIndex];
                        String operandDescription = usageDescription + "/code/ins" + i + "/op" + operandIndex;
                        switch (ins.definition.operands[operandIndex]) {
                            case AVM2Code.DAT_CLASS_INDEX:
                                break;
                            case AVM2Code.DAT_DOUBLE_INDEX:
                                handleUsage(ItemKind.DOUBLE, operand, operandDescription);
                                break;
                            case AVM2Code.DAT_INT_INDEX:
                                handleUsage(ItemKind.INT, operand, operandDescription);
                                break;
                            case AVM2Code.DAT_METHOD_INDEX:
                                //handleUsageMethodInfo(operand, operandDescription);
                                break;
                            case AVM2Code.DAT_MULTINAME_INDEX:
                                handleUsageMultiname(operand, operandDescription);
                                break;
                            case AVM2Code.DAT_NAMESPACE_INDEX:
                                handleUsageNamespace(operand, operandDescription);
                                break;
                            case AVM2Code.DAT_STRING_INDEX:
                                handleUsage(ItemKind.STRING, operand, operandDescription);
                                break;
                            case AVM2Code.DAT_UINT_INDEX:
                                handleUsage(ItemKind.UINT, operand, operandDescription);
                                break;
                            case AVM2Code.DAT_FLOAT_INDEX:
                                handleUsage(ItemKind.FLOAT, operand, operandDescription);
                                break;
                            case AVM2Code.DAT_DECIMAL_INDEX:
                                handleUsage(ItemKind.DECIMAL, operand, operandDescription);
                                break;
                        }
                    }
                }
            }

            @Override
            protected void handleScript(ABC abc, int index) {

            }

            @Override
            protected void handleMetadataInfo(ABC abc, int index, Trait trait, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, int traitMetadataIndex, ABCWalker.WalkType walkType) {
                String description = "si" + scriptIndex + "/traits/t" + scriptTraitIndex;
                if (walkType == WalkType.Class && traitIndex > -1) {
                    description += "/class_info/traits/t" + traitIndex;
                } else if (walkType == WalkType.Instance && traitIndex > -1) {
                    description += "/instance_info/traits/t" + traitIndex;
                }
                description += "/metadata/md" + index;

                //?? classIndex
                if (!handleUsage(ItemKind.METADATAINFO, index, description)) {
                    return;
                }
                MetadataInfo md = abc.metadata_info.get(index);
                handleUsage(ItemKind.STRING, md.name_index, description + "/name");
                for (int i = 0; i < md.keys.length; i++) {
                    handleUsage(ItemKind.STRING, md.keys[i], description + "/pairs/p" + i + "/key");
                }
                for (int i = 0; i < md.values.length; i++) {
                    handleUsage(ItemKind.STRING, md.values[i], description + "/pairs/p" + i + "/value");
                }
            }

            @Override
            protected void handleTraitClass(ABC abc, TraitClass trait, int scriptIndex, int scriptTraitIndex) {
                String description = "si" + scriptIndex + "/traits/t" + scriptTraitIndex;
                if (!handleUsage(ItemKind.CLASS, trait.class_info, description)) {
                    return;
                }

                InstanceInfo ii = abc.instance_info.get(trait.class_info);
                if ((ii.flags & InstanceInfo.CLASS_PROTECTEDNS) != 0) {
                    handleUsageNamespace(ii.protectedNS, description + "/instance_info/protected_ns");
                }
                handleUsageMultiname(ii.name_index, description + "/instance_info/name");
                handleUsageMultiname(ii.super_index, description + "/instance_info/super");
                for (int i = 0; i < ii.interfaces.length; i++) {
                    handleUsageMultiname(ii.interfaces[i], description + "/instance_info/interfaces/in" + i);
                }
                /*
                handleUsageMethodInfo(abc.class_info.get(trait.class_info).cinit_index, description + "/class_info/cinit");
                handleUsageMethodInfo(abc.instance_info.get(trait.class_info).iinit_index, description + "/instance_info/iinit");
                 */
            }

            @Override
            protected void handleTraitMethodGetterSetter(ABC abc, TraitMethodGetterSetter trait, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, ABCWalker.WalkType walkType) {
                handleTraitMethodBase(abc, trait, scriptIndex, scriptTraitIndex, classIndex, traitIndex, walkType);
            }

            protected void handleTraitMethodBase(ABC abc, Trait trait, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, ABCWalker.WalkType walkType) {
                String description = "";
                if (scriptIndex > -1) {
                    description += "si" + scriptIndex + "/traits/t" + scriptTraitIndex;
                    if (walkType == WalkType.Class && traitIndex > -1) {
                        description += "/class_info/traits/t" + traitIndex;
                    } else if (walkType == WalkType.Instance && traitIndex > -1) {
                        description += "/instance_info/traits/t" + traitIndex;
                    }
                } else {
                    if (walkType == WalkType.Class && traitIndex > -1) {
                        description += "ci" + classIndex + "/class_info/traits/t" + traitIndex;
                    } else if (walkType == WalkType.Instance && traitIndex > -1) {
                        description += "ii" + classIndex + "/instance_info/traits/t" + traitIndex;
                    }
                }

                //description += " " + trait.getKindToStr();
                handleUsageMultiname(trait.name_index, description + "/name");
                //handleUsageMethodInfo(trait.method_info, description + "/method_info");
            }

            @Override
            protected void handleTraitFunction(ABC abc, TraitFunction trait, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, ABCWalker.WalkType walkType) {
                handleTraitMethodBase(abc, trait, scriptIndex, scriptTraitIndex, classIndex, traitIndex, walkType);
            }

            protected void handleUsageValueKind(ABC abc, int value_kind, int value_index, String description) {
                switch (value_kind) {
                    case ValueKind.CONSTANT_Int:
                        handleUsage(ItemKind.INT, value_index, description);
                        break;
                    case ValueKind.CONSTANT_UInt:
                        handleUsage(ItemKind.UINT, value_index, description);
                        break;
                    case ValueKind.CONSTANT_Double:
                        handleUsage(ItemKind.DOUBLE, value_index, description);
                        break;
                    case ValueKind.CONSTANT_Utf8:
                        handleUsage(ItemKind.STRING, value_index, description);
                        break;
                    case ValueKind.CONSTANT_DecimalOrFloat:
                        if (abc.hasFloatSupport()) {
                            handleUsage(ItemKind.FLOAT, value_index, description);
                        } else {
                            handleUsage(ItemKind.DECIMAL, value_index, description);
                        }
                        break;
                    case ValueKind.CONSTANT_Namespace:
                    case ValueKind.CONSTANT_PackageNamespace:
                    case ValueKind.CONSTANT_PackageInternalNs:
                    case ValueKind.CONSTANT_ProtectedNamespace:
                    case ValueKind.CONSTANT_ExplicitNamespace:
                    case ValueKind.CONSTANT_StaticProtectedNs:
                    case ValueKind.CONSTANT_PrivateNs:
                        handleUsageNamespace(value_index, description);
                        break;
                }
            }

            @Override
            protected void handleTraitSlotConst(ABC abc, TraitSlotConst trait, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, int bodyIndex, int bodyTraitIndex, ABCWalker.WalkType walkType, Stack<Integer> callStack) {

                String description = "";
                if (callStack.size() > 1) {
                    if (bodyTraitIndex != -1) {
                        int methodInfo = callStack.peek();
                        description += "mi" + methodInfo + "/method_body/traits/t" + bodyTraitIndex;
                    }
                } else if (scriptIndex > -1) {
                    description += "si" + scriptIndex + "/traits/t" + scriptTraitIndex;

                    if (walkType == WalkType.Class && traitIndex > -1) {
                        description += "/class_info/traits/t" + traitIndex;
                    } else if (walkType == WalkType.Instance && traitIndex > -1) {
                        description += "/instance_info/traits/t" + traitIndex;
                    }

                    if (bodyTraitIndex != -1) {
                        description += "/method_info/method_body/traits/t" + bodyTraitIndex;
                    }
                } else {
                    if (walkType == WalkType.Class && traitIndex > -1) {
                        description += "ci" + classIndex + "/class_info/traits/t" + traitIndex;
                    } else if (walkType == WalkType.Instance && traitIndex > -1) {
                        description += "ii" + classIndex + "/instance_info/traits/t" + traitIndex;
                    } else if (bodyTraitIndex > -1) {
                        description += "mb" + bodyIndex + "/traits/t" + bodyTraitIndex;
                    }
                }

                //description += " " + trait.getKindToStr();
                handleUsageMultiname(trait.name_index, description + "/name");
                handleUsageMultiname(trait.type_index, description + "/type");
                handleUsageValueKind(abc, trait.value_kind, trait.value_index, description + "/value_index");
            }

            @Override
            protected void handleMethodInfo(ABC abc, int index, int scriptIndex, int scriptTraitIndex, int classIndex, int traitIndex, ABCWalker.WalkType walkType, boolean initializer, Stack<Integer> callStack) {
                if (callStack.size() > 1) { //it is an anonymous submethod
                    int prevMethod = callStack.get(callStack.size() - 2);
                    handleUsageMethodInfo(index, "mi" + prevMethod + "/method_body/code");
                    return;
                }
                if (initializer) {
                    switch (walkType) {
                        case Class:
                            if (scriptIndex != -1) {
                                handleUsageMethodInfo(index, "si" + scriptIndex + "/traits/t" + scriptTraitIndex + "/class_info/cinit");
                            } else {
                                handleUsageMethodInfo(index, "ci" + classIndex + "/class_info/cinit");
                            }
                            break;
                        case Instance:
                            if (scriptIndex != -1) {
                                handleUsageMethodInfo(index, "si" + scriptIndex + "/traits/t" + scriptTraitIndex + "/instance_info/iinit");
                            } else {
                                handleUsageMethodInfo(index, "ii" + classIndex + "/instance_info/iinit");
                            }
                            break;
                        case Script:
                            handleUsageMethodInfo(index, "si" + scriptIndex + "/init");
                            break;

                    }
                    return;
                }

                String description = "";
                if (scriptIndex > -1) {
                    description += "si" + scriptIndex + "/traits/t" + scriptTraitIndex;
                    if (walkType == WalkType.Class && traitIndex > -1) {
                        description += "/class_info/traits/t" + traitIndex;
                    } else if (walkType == WalkType.Instance && traitIndex > -1) {
                        description += "/instance_info/traits/t" + traitIndex;
                    }
                } else {
                    if (walkType == WalkType.Class && traitIndex > -1) {
                        description += "ci" + classIndex + "/class_info/traits/t" + traitIndex;
                    } else if (walkType == WalkType.Instance && traitIndex > -1) {
                        description += "ii" + classIndex + "/instance_info/traits/t" + traitIndex;
                    }
                }
                description += "/method_info";
                handleUsageMethodInfo(index, description);
            }
        };
        walker.walkABC(abc, false);

        zeroUsages.clear();
        for (ItemKind kind : usages.keySet()) {
            zeroUsages.put(kind, new ArrayList<>());
            for (int i = kind.hasReservedZeroIndex() ? 1 : 0; i < usages.get(kind).size(); i++) {
                if (usages.get(kind).get(i).isEmpty()) {
                    zeroUsages.get(kind).add(i);
                }
            }
        }
    }

    /**
     * Handles usage.
     * @param kind Item kind
     * @param index Index
     * @param usageDescription Usage description
     * @return True if it is new
     */
    private boolean handleUsage(ItemKind kind, int index, String usageDescription) {
        if (index < 0 || index > usages.get(kind).size() - 1) {
            Logger.getLogger(ABCSimpleUsageDetector.class.getName()).log(Level.WARNING, "{0} with index {1} not found for usage {2}", new Object[]{kind, index, usageDescription});
            return false;
        }
        List<String> kindList = usages.get(kind).get(index);
        kindList.add(usageDescription);

        return kindList.size() == 1;
    }

    /**
     * Gets usages.
     * @return Usages
     */
    public Map<ItemKind, List<List<String>>> getUsages() {
        return Collections.unmodifiableMap(usages);
    }

    /**
     * Gets usages of kind and index.
     * @param kind Item kind
     * @param index Index
     * @return Usages
     */
    public List<String> getUsages(ItemKind kind, int index) {
        return Collections.unmodifiableList(usages.get(kind).get(index));
    }

    /**
     * Gets usages of kind.
     * @param kind Item kind
     * @return Usages
     */
    public List<List<String>> getUsages(ItemKind kind) {
        return Collections.unmodifiableList(usages.get(kind));
    }

    /**
     * Gets zero usages.
     * @return Zero usages
     */
    public Map<ItemKind, List<Integer>> getZeroUsages() {
        return Collections.unmodifiableMap(zeroUsages);
    }

    /**
     * Gets zero usages of kind.
     * @param kind Item kind
     * @return Zero usages
     */
    public List<Integer> getZeroUsages(ItemKind kind) {
        return zeroUsages.get(kind);
    }

    /**
     * Gets zero usages count.
     * @return Zero usages count
     */
    public int getZeroUsagesCount() {
        int cnt = 0;
        for (ItemKind kind : zeroUsages.keySet()) {
            cnt += zeroUsages.get(kind).size();
        }
        return cnt;
    }

    /**
     * Gets zero usages count of kind.
     * @param kind Item kind
     * @return Zero usages count
     */
    public int getZeroUsagesCount(ItemKind kind) {
        return zeroUsages.get(kind).size();
    }
}
