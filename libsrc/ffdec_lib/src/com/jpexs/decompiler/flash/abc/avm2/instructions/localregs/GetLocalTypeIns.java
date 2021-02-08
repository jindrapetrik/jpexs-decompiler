/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.ClassAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class GetLocalTypeIns extends InstructionDefinition {

    public GetLocalTypeIns(int instructionCode, String instructionName, int[] operands, boolean canThrow) {
        super(instructionCode, instructionName, operands, canThrow);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        Object value = lda.localRegisters.get(getRegisterId(ins));
        lda.operandStack.push(value == null ? Undefined.INSTANCE : value);
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {

        int regId = getRegisterId(ins);

        if (regId == 0) {
            if ((localData.classIndex >= localData.getInstanceInfo().size()) || localData.classIndex < 0) {
                stack.push(new ThisAVM2Item(ins, localData.lineStartInstruction, null, false));
                return;
            }
            if (localData.isStatic) {
                stack.push(new ClassAVM2Item(localData.getInstanceInfo().get(localData.classIndex).getName(localData.getConstants())));
            } else {
                List<Trait> ts = localData.getInstanceInfo().get(localData.classIndex).instance_traits.traits;
                boolean isBasicObject = localData.thisHasDefaultToPrimitive;
                Multiname m = localData.getInstanceInfo().get(localData.classIndex).getName(localData.getConstants());
                stack.push(new ThisAVM2Item(ins, localData.lineStartInstruction, m.getNameWithNamespace(localData.getConstants(), true), isBasicObject));
            }
            return;
        }

        GraphTargetItem computedValue = localData.localRegs.get(regId);
        int assignCount = 0;
        if (localData.localRegAssignmentIps.containsKey(regId)) {
            assignCount = localData.localRegAssignmentIps.get(regId);
        }
        if (assignCount > 5) { //Do not allow change register more than 5 - for deobfuscation
            //computedValue = new NotCompileTimeItem(ins, localData.lineStartInstruction, computedValue);
        }

        //chained assignments and/or ASC post/pre increment
        if (!output.isEmpty()) {
            if ((output.get(output.size() - 1) instanceof SetTypeAVM2Item)) {
                GraphTargetItem setItem = output.get(output.size() - 1);
                if ((setItem instanceof SetPropertyAVM2Item)
                        && ((setItem.value.getNotCoerced() instanceof DecrementAVM2Item)
                        || (setItem.value.getNotCoerced() instanceof IncrementAVM2Item))) {
                    boolean isIncrement = (setItem.value.getNotCoerced() instanceof IncrementAVM2Item);
                    GraphTargetItem val = setItem.value.getNotCoerced();
                    if (val.value instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) val.value;
                        if (setLocal.regIndex == regId) {
                            if (setLocal.value.getNotCoerced() instanceof GetPropertyAVM2Item) {
                                SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) setItem;
                                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLocal.value.getNotCoerced();
                                if (getProp.object.getThroughDuplicate() == setProp.object) {
                                    if (((FullMultinameAVM2Item) setProp.propertyName).compareSame((FullMultinameAVM2Item) getProp.propertyName)) {
                                        if (getProp.object instanceof DuplicateItem) {
                                            getProp.object = getProp.object.value;
                                        }
                                        GraphTargetItem result;
                                        if (isIncrement) {
                                            result = new PostIncrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                        } else {
                                            result = new PostDecrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                        }
                                        output.remove(output.size() - 1);
                                        stack.add(result);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                else if (setItem.value.getNotCoerced() instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) setItem.value.getNotCoerced();
                    if (setLocal.regIndex == regId) {
                        if ((setItem.value instanceof CoerceAVM2Item) || (setItem.value instanceof ConvertAVM2Item)) {
                            setItem.value.value = setLocal.value;
                        } else {
                            setItem.value = setLocal.value;
                        }

                        output.remove(output.size() - 1);

                        if (setItem instanceof SetPropertyAVM2Item) {
                            if ((setItem.value instanceof IncrementAVM2Item) || (setItem.value instanceof DecrementAVM2Item)) {
                                boolean isIncrement = (setItem.value instanceof IncrementAVM2Item);
                                if (setItem.value.value instanceof GetPropertyAVM2Item) {
                                    SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) setItem;
                                    GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setItem.value.value;
                                    if (getProp.object.getThroughDuplicate() == setProp.object) {
                                        if (((FullMultinameAVM2Item) setProp.propertyName).compareSame((FullMultinameAVM2Item) getProp.propertyName)) {
                                            if (getProp.object instanceof DuplicateItem) {
                                                getProp.object = getProp.object.value;
                                            }
                                            if (isIncrement) {
                                                setItem = new PreIncrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                            } else {
                                                setItem = new PreDecrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        stack.add(setItem);
                        return;
                    }
                }
            }
        }

        stack.push(new LocalRegAVM2Item(ins, localData.lineStartInstruction, regId, computedValue));
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    public abstract int getRegisterId(AVM2Instruction ins);
}
