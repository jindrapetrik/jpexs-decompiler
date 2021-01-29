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
 * License along with this library. */
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
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ScriptAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
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
                stack.push(new ScriptAVM2Item(localData.scriptIndex));
                return;
            }
            if (localData.isStatic) {
                stack.push(new ClassAVM2Item(localData.getInstanceInfo().get(localData.classIndex).getName(localData.getConstants())));
            } else {

                List<Trait> ts = localData.getInstanceInfo().get(localData.classIndex).instance_traits.traits;
                boolean isBasicObject = localData.thisHasDefaultToPrimitive;
                Multiname m = localData.getInstanceInfo().get(localData.classIndex).getName(localData.getConstants());
                stack.push(new ThisAVM2Item(ins, localData.lineStartInstruction, m, m.getNameWithNamespace(localData.getConstants(), true), isBasicObject));
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

        //chained assignments
        if (!output.isEmpty()) {
            if ((output.get(output.size() - 1) instanceof SetTypeAVM2Item)) {
                GraphTargetItem setItem = output.get(output.size() - 1);
                if (setItem.value.getNotCoerced() instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) setItem.value.getNotCoerced();
                    if (setLocal.regIndex == regId) {
                        if ((setItem.value instanceof CoerceAVM2Item) || (setItem.value instanceof ConvertAVM2Item)) {
                            setItem.value.value = setLocal.value;
                        } else {
                            setItem.value = setLocal.value;
                        }

                        output.remove(output.size() - 1);
                        stack.add(setItem);
                        return;
                    }
                }
            }
        }
        /*if (output.size() >= 2) {
            if ((output.get(output.size() - 1) instanceof SetTypeAVM2Item) && (output.get(output.size() - 2) instanceof SetLocalAVM2Item)) {
                SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(output.size() - 2);
                GraphTargetItem setItem = output.get(output.size() - 1);
                if (setLocal.regIndex == regId
                        && (setLocal.value instanceof DuplicateItem)
                        && (setLocal.value.value == setItem.value.getNotCoerced())) {

                    int setLocalIp = getItemIp(localData, setLocal);
                    int getLocalIp = localData.code.adr2pos(ins.getAddress());
                    Set<Integer> usages = localData.getSetLocalUsages(setLocalIp);
                    if (usages.size() == 1 && usages.iterator().next().equals(getLocalIp)) {
                        output.remove(output.size() - 1);
                        output.remove(output.size() - 1);
                        stack.push(setItem);
                        return;
                    }
                }
            }
        }*/

        stack.push(new LocalRegAVM2Item(ins, localData.lineStartInstruction, regId, computedValue));
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    public abstract int getRegisterId(AVM2Instruction ins);
}
