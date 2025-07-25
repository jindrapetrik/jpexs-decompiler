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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.ecma.ArrayType;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.ObjectType;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * getproperty instruction - get property of object.
 *
 * @author JPEXS
 */
public class GetPropertyIns extends InstructionDefinition {

    /**
     * Constructor
     */
    public GetPropertyIns() {
        super(0x66, "getproperty", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2ExecutionException {
        if (constants.getMultiname(ins.operands[0]).kind == Multiname.MULTINAMEL) {
            String name = EcmaScript.toString(lda.operandStack.pop());
            Object obj = lda.operandStack.pop();

            if (obj == ArrayType.EMPTY_ARRAY) {
                if ("length".equals(name)) {
                    lda.operandStack.push(0L);
                } else {
                    lda.operandStack.push(Undefined.INSTANCE);
                }
                return true;
            }
            if (obj == ObjectType.EMPTY_OBJECT) {
                lda.operandStack.push(Undefined.INSTANCE);
                return true;
            }
            return true;
        }
        return false;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];
        FullMultinameAVM2Item multiname = resolveMultiname(localData, true, stack, localData.getConstants(), multinameIndex, ins);
        GraphTargetItem obj = stack.pop();
        //remove dups
        if (obj instanceof FindPropertyAVM2Item) {
            FindPropertyAVM2Item findProp = (FindPropertyAVM2Item) obj;
            if (findProp.propertyName instanceof FullMultinameAVM2Item) {
                FullMultinameAVM2Item findPropName = (FullMultinameAVM2Item) findProp.propertyName;
                if ((findPropName.name instanceof LocalRegAVM2Item) && (multiname.name instanceof LocalRegAVM2Item)) {
                    LocalRegAVM2Item getLocal1 = (LocalRegAVM2Item) findPropName.name;
                    LocalRegAVM2Item getLocal2 = (LocalRegAVM2Item) multiname.name;
                    if (!output.isEmpty() && (output.get(output.size() - 1) instanceof SetLocalAVM2Item)) {
                        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(output.size() - 1);
                        if (setLocal.regIndex == getLocal1.regIndex && setLocal.regIndex == getLocal2.regIndex) {
                            Set<Integer> usage = localData.getSetLocalUsages(localData.code.adr2pos(setLocal.getSrc().getAddress()));
                            if (usage.size() == 2) {
                                findPropName.name = setLocal.value;
                                output.remove(output.size() - 1);
                            }
                        }
                    }
                }
                if (findPropName.name instanceof DuplicateItem) {
                    if (findPropName.name.value == multiname.name) {
                        findPropName.name = findPropName.name.value;
                    }
                }
                if (findPropName.namespace instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) findPropName.namespace;
                    if (multiname.namespace instanceof LocalRegAVM2Item) {
                        LocalRegAVM2Item getLocal = (LocalRegAVM2Item) multiname.namespace;
                        if (setLocal.regIndex == getLocal.regIndex) {
                            Set<Integer> usage = localData.getSetLocalUsages(localData.code.adr2pos(setLocal.getSrc().getAddress()));
                            if (usage.size() == 1) {
                                findPropName.namespace = findPropName.namespace.value;
                            }
                        }
                    }
                }
                if (findPropName.namespace instanceof DuplicateItem) {
                    if (findPropName.namespace.value == multiname.namespace) {
                        findPropName.namespace = findPropName.namespace.value;
                    }
                }
            }
        }
        Reference<Boolean> isStatic = new Reference<>(false);
        Reference<GraphTargetItem> type = new Reference<>(null);
        Reference<GraphTargetItem> callType = new Reference<>(null);
        resolvePropertyType(localData, obj, multiname, isStatic, type, callType);

        stack.push(new GetPropertyAVM2Item(ins, localData.lineStartInstruction, obj, multiname, type.getVal(), callType.getVal(), isStatic.getVal()));
    }

    public static void resolvePropertyType(
            AVM2LocalData localData,
            GraphTargetItem obj,
            FullMultinameAVM2Item multiname,
            Reference<Boolean> isStatic, Reference<GraphTargetItem> type, Reference<GraphTargetItem> callType) {
        type.setVal(TypeItem.UNKNOWN);
        callType.setVal(TypeItem.UNKNOWN);
        String multinameStr = localData.abc.constants.getMultiname(multiname.multinameIndex).getName(localData.usedDeobfuscations, localData.abc, localData.abc.constants, new ArrayList<>(), true, true);
        if (obj instanceof FindPropertyAVM2Item) {
            FindPropertyAVM2Item fprop = (FindPropertyAVM2Item) obj;
            if (fprop.propertyName.equals(multiname)) {
                for (int b = localData.callStack.size() - 1; b >= 0; b--) {
                    MethodBody body = localData.callStack.get(b);
                    for (Trait t : body.traits.traits) {
                        if (t instanceof TraitSlotConst) {
                            TraitSlotConst tsc = (TraitSlotConst) t;
                            if (Objects.equals(
                                    tsc.getName(localData.abc).getName(localData.usedDeobfuscations, localData.abc, localData.abc.constants, new ArrayList<>(), true, true),
                                    multinameStr
                            )) {
                                GraphTargetItem ty = AbcIndexing.multinameToType(localData.usedDeobfuscations, tsc.type_index, localData.abc, localData.abc.constants);
                                type.setVal(ty);
                                callType.setVal(ty);
                                return;
                            }
                        }
                    }
                }

                if (type.getVal().equals(TypeItem.UNKNOWN)) {
                    if (localData.abcIndex != null) {
                        String currentClassName = localData.classIndex == -1 ? null : localData.abc.instance_info.get(localData.classIndex).getName(localData.abc.constants).getNameWithNamespace(localData.usedDeobfuscations, localData.abc, localData.abc.constants, true).toRawString();
                        if (currentClassName != null) {
                            Reference<Boolean> foundStatic = new Reference<>(null);                
                            localData.abcIndex.findPropertyTypeOrCallType(localData.abc, new TypeItem(currentClassName), multinameStr, localData.abc.constants.getMultiname(multiname.multinameIndex).namespace_index, true, true, true, type, callType, foundStatic);
                        }
                        if (type.getVal().equals(TypeItem.UNKNOWN)) {
                            GraphTargetItem ti = AbcIndexing.multinameToType(localData.usedDeobfuscations, multiname.multinameIndex, localData.abc, localData.abc.constants);
                            if (localData.abcIndex.findClass(ti, localData.abc, localData.scriptIndex) != null) {
                                type.setVal(ti);
                                callType.setVal(ti); //coercion  i = int(xx);
                                isStatic.setVal(true);
                                return;
                            }

                            Namespace ns = localData.abc.constants.getMultiname(multiname.multinameIndex).getNamespace(localData.abc.constants);
                            if (ns != null) {
                                String rawNs = ns.getRawName(localData.abc.constants);
                                AbcIndexing.TraitIndex traitIndex = localData.abcIndex.findScriptProperty(multinameStr, DottedChain.parseWithSuffix(rawNs));
                                if (traitIndex != null) {
                                    type.setVal(traitIndex.returnType);
                                    callType.setVal(traitIndex.callReturnType);
                                    isStatic.setVal(true); //?
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (localData.abcIndex != null) {
                GraphTargetItem receiverType = obj.returnType();
                if (!receiverType.equals(TypeItem.UNBOUNDED) && !receiverType.equals(TypeItem.UNKNOWN)) {

                    boolean parentStatic = false;
                    if (obj instanceof GetLexAVM2Item) {
                        if (((GetLexAVM2Item) obj).isStatic) {
                            parentStatic = true;
                        }
                    }
                    if (obj instanceof GetPropertyAVM2Item) {
                        if (((GetPropertyAVM2Item) obj).isStatic) {
                            parentStatic = true;
                        }
                    }
                    if (receiverType instanceof ApplyTypeAVM2Item) {
                        ApplyTypeAVM2Item ati = (ApplyTypeAVM2Item) receiverType;
                        if (localData.abc.constants.getMultiname(multiname.multinameIndex).needsName()) {
                            callType.setVal(TypeItem.UNBOUNDED);
                            /*?*/
                            type.setVal(ati.params.get(0));
                            return;
                        } else {
                            receiverType = ati.object;

                            if (receiverType.equals(new TypeItem("__AS3__.vec.Vector"))) {
                                String paramStr = ati.params.get(0).toString();
                                switch (paramStr) {
                                    case "double":
                                    case "int":
                                    case "uint":
                                        receiverType = new TypeItem("__AS3__.vec.Vector$" + paramStr);
                                        break;
                                    default:
                                        receiverType = new TypeItem("__AS3__.vec.Vector$object");
                                }
                            }

                            //TODO: handle method calls to return proper param type results
                        }
                    }
                    if (localData.abc.constants.getMultiname(multiname.multinameIndex).isAttribute()) {
                        type.setVal(new TypeItem("XMLList"));
                        return;
                    }
                    if (receiverType.equals(new TypeItem("XMLList"))) {
                        if (multiname.name != null && multiname.name.returnType().equals(TypeItem.INT)) {
                            type.setVal(new TypeItem("XML"));
                            return;
                        }
                    }
                    Reference<Boolean> foundStatic = new Reference<>(null);                
                    localData.abcIndex.findPropertyTypeOrCallType(localData.abc, receiverType, multiname.resolvedMultinameName, localData.abc.constants.getMultiname(multiname.multinameIndex).namespace_index, parentStatic, !parentStatic, false, type, callType, foundStatic);
                    if (receiverType.equals(new TypeItem("XML")) && !type.getVal().equals(new TypeItem("Function"))) {
                        type.setVal(new TypeItem("XMLList"));
                    }
                }
            }
        }
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        int multinameIndex = ins.operands[0];
        return 1 + getMultinameRequiredStackSize(abc.constants, multinameIndex);
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
