/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.parser.script.PropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.ecma.ArrayType;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.ObjectType;
import com.jpexs.decompiler.flash.ecma.Undefined;
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
 *
 * @author JPEXS
 */
public class GetPropertyIns extends InstructionDefinition {

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
        GraphTargetItem type = resolvePropertyType(localData, obj, multiname, isStatic, false);

        stack.push(new GetPropertyAVM2Item(ins, localData.lineStartInstruction, obj, multiname, type, isStatic.getVal()));
    }

    public static GraphTargetItem resolvePropertyType(
            AVM2LocalData localData,
            GraphTargetItem obj,
            FullMultinameAVM2Item multiname,
            Reference<Boolean> isStatic, boolean call) {
        GraphTargetItem type = null;
        String multinameStr = localData.abc.constants.getMultiname(multiname.multinameIndex).getName(localData.abc.constants, new ArrayList<>(), true, true);
        if (obj instanceof FindPropertyAVM2Item) {
            FindPropertyAVM2Item fprop = (FindPropertyAVM2Item) obj;
            if (fprop.propertyName.equals(multiname)) {
                for (Trait t : localData.methodBody.traits.traits) {
                    if (t instanceof TraitSlotConst) {
                        TraitSlotConst tsc = (TraitSlotConst) t;
                        if (Objects.equals(
                                tsc.getName(localData.abc).getName(localData.abc.constants, new ArrayList<>(), true, true),
                                multinameStr
                        )) {
                            type = PropertyAVM2Item.multinameToType(tsc.type_index, localData.abc.constants);
                            break;
                        }
                    }
                }

                if (type == null) {
                    if (localData.abcIndex != null) {
                        String currentClassName = localData.classIndex == -1 ? null : localData.abc.instance_info.get(localData.classIndex).getName(localData.abc.constants).getNameWithNamespace(localData.abc.constants, true).toRawString();
                        GraphTargetItem thisPropType = TypeItem.UNBOUNDED;
                        if (currentClassName != null) {
                            if (call) {
                                thisPropType = localData.abcIndex.findPropertyCallType(localData.abc, new TypeItem(currentClassName), multinameStr, localData.abc.constants.getMultiname(multiname.multinameIndex).namespace_index, true, true, true);
                            } else {
                                thisPropType = localData.abcIndex.findPropertyType(localData.abc, new TypeItem(currentClassName), multinameStr, localData.abc.constants.getMultiname(multiname.multinameIndex).namespace_index, true, true, true);
                            }
                        }
                        if (!thisPropType.equals(TypeItem.UNBOUNDED)) {
                            type = thisPropType;
                        }

                        if (type == null) {
                            TypeItem ti = new TypeItem(localData.abc.constants.getMultiname(multiname.multinameIndex).getNameWithNamespace(localData.abc.constants, true));
                            if (localData.abcIndex.findClass(ti) != null) {
                                type = ti;
                                isStatic.setVal(true);
                            }
                        }
                    }
                }
            }
        } else {
            if (localData.abcIndex != null) {
                GraphTargetItem receiverType = obj.returnType();
                if (!receiverType.equals(TypeItem.UNBOUNDED)) {

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
                            if (call) {
                                type = TypeItem.UNBOUNDED; /*?*/
                            } else {
                                type = ati.params.get(0);
                            }
                        } else {
                            receiverType = ati.object;
                            
                            if (receiverType.equals(new TypeItem("__AS3__.vec.Vector"))) {
                                String paramStr = ati.params.get(0).toString();
                                switch(paramStr) {
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
                    if (type == null)
                    {
                        if (call) {
                            type = localData.abcIndex.findPropertyCallType(localData.abc, receiverType, multiname.resolvedMultinameName, localData.abc.constants.getMultiname(multiname.multinameIndex).namespace_index, parentStatic, !parentStatic, false);
                        } else {
                            type = localData.abcIndex.findPropertyType(localData.abc, receiverType, multiname.resolvedMultinameName, localData.abc.constants.getMultiname(multiname.multinameIndex).namespace_index, parentStatic, !parentStatic, false);
                        }
                    }
                }
            }
        }

        if (type == null) {
            type = TypeItem.UNBOUNDED;
        }
        return type;
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
