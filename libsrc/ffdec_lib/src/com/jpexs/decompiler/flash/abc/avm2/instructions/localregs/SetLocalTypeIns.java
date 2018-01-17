/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.PopItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public abstract class SetLocalTypeIns extends InstructionDefinition implements SetTypeIns {

    public SetLocalTypeIns(int instructionCode, String instructionName, int[] operands, boolean canThrow) {
        super(instructionCode, instructionName, operands, canThrow);
    }

    @Override
    public boolean execute(com.jpexs.decompiler.flash.abc.avm2.LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        lda.localRegisters.put(getRegisterId(ins), lda.operandStack.pop());
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int regId = getRegisterId(ins);
        GraphTargetItem value = stack.pop();
        /*if (localRegs.containsKey(regId)) {
         localRegs.put(regId, new NotCompileTimeAVM2Item(ins, localData.lineStartInstruction, value));
         } else {
         localRegs.put(regId, value);
         }*/
        if (!(value instanceof PopItem)) {
            localData.localRegs.put(regId, value);
        }
        if (!localData.localRegAssignmentIps.containsKey(regId)) {
            localData.localRegAssignmentIps.put(regId, 0);
        }
        localData.localRegAssignmentIps.put(regId, localData.localRegAssignmentIps.get(regId) + 1);
        //localRegsAssignmentIps.put(regId, ip);
        if (value.getThroughDuplicate() instanceof NewActivationAVM2Item) {
            return;
        }
        if (value instanceof FindPropertyAVM2Item) {
            return;
        }
        if (value.getNotCoerced() instanceof IncrementAVM2Item) {
            GraphTargetItem inside = ((IncrementAVM2Item) value.getNotCoerced()).value.getNotCoerced().getThroughDuplicate();
            if (inside instanceof LocalRegAVM2Item) {
                if (((LocalRegAVM2Item) inside).regIndex == regId) {
                    if (stack.size() > 0) {
                        GraphTargetItem top = stack.peek().getNotCoerced().getThroughDuplicate();
                        if (top == inside) {
                            stack.pop();
                            stack.push(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        } else if ((top instanceof IncrementAVM2Item) && (((IncrementAVM2Item) top).value == inside)) {
                            stack.pop();
                            stack.push(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        } else {
                            output.add(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        }
                    } else {
                        output.add(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, inside));
                    }
                    return;
                }
            }
        }

        if (value.getNotCoerced() instanceof DecrementAVM2Item) {
            GraphTargetItem inside = ((DecrementAVM2Item) value.getNotCoerced()).value.getNotCoerced().getThroughDuplicate();
            if (inside instanceof LocalRegAVM2Item) {
                if (((LocalRegAVM2Item) inside).regIndex == regId) {
                    if (stack.size() > 0) {
                        GraphTargetItem top = stack.peek().getNotCoerced().getThroughDuplicate();
                        if (top == inside) {
                            stack.pop();
                            stack.push(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        } else if ((top instanceof DecrementAVM2Item) && (((DecrementAVM2Item) top).value == inside)) {
                            stack.pop();
                            stack.push(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        } else {
                            output.add(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        }
                    } else {
                        output.add(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, inside));
                    }
                    return;
                }
            }
        }

        //if(val.startsWith("catchscope ")) return;
        //if(val.startsWith("newactivation()")) return;
        output.add(new SetLocalAVM2Item(ins, localData.lineStartInstruction, regId, value));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    @Override
    public String getObject(Stack<AVM2Item> stack, ABC abc, AVM2Instruction ins, List<AVM2Item> output, MethodBody body, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames) {
        return AVM2Item.localRegName(localRegNames, getRegisterId(ins));
    }

    public abstract int getRegisterId(AVM2Instruction ins);
}
