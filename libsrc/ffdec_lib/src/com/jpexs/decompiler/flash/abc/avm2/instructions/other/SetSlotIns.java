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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.DecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.CompoundableBinaryOp;
import com.jpexs.helpers.Reference;
import java.util.List;
import java.util.Objects;

/**
 * setslot instruction - set slot value.
 *
 * @author JPEXS
 */
public class SetSlotIns extends InstructionDefinition implements SetTypeIns {

    /**
     * Constructor
     */
    public SetSlotIns() {
        super(0x6d, "setslot", new int[]{AVM2Code.DAT_SLOT_INDEX}, true);
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int slotIndex = ins.operands[0];
        stack.allowSwap(output);
        GraphTargetItem value = stack.pop();
        GraphTargetItem obj = stack.pop(); //scopeId
        if (obj.getThroughRegister() instanceof NewActivationAVM2Item) {
            ((NewActivationAVM2Item) obj.getThroughRegister()).slots.put(slotIndex, value);
        }
        handleSetSlot(localData, stack, ins, output, slotIndex, obj, value);
    }

    public static void handleSetSlot(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, int slotIndex, GraphTargetItem obj, GraphTargetItem value) {

        GraphTargetItem objnoreg = obj;
        obj = obj.getThroughRegister();

        Reference<GraphTargetItem> realObjRef = new Reference<>(null);
        Multiname slotname = InstructionDefinition.searchSlotName(slotIndex, localData, obj, realObjRef);

        GraphTargetItem realObj = realObjRef.getVal();
        if (realObj != null) {
            obj = realObj;
        }

        if (slotname != null) {
            if (value instanceof LocalRegAVM2Item) {
                LocalRegAVM2Item lr = (LocalRegAVM2Item) value;
                String slotNameStr = slotname.getName(localData.usedDeobfuscations, localData.abc, localData.getConstants(), localData.fullyQualifiedNames, true, true);
                if (localData.localRegNames.containsKey(lr.regIndex)) {
                    if (localData.localRegNames.get(lr.regIndex).equals(slotNameStr)) {
                        return; //Register with same name to slot
                    }
                }
            }
        }

        if (value.getNotCoerced().getThroughDuplicate() instanceof IncrementAVM2Item) {
            GraphTargetItem inside = ((IncrementAVM2Item) value.getNotCoerced()).value.getThroughRegister().getNotCoerced().getThroughDuplicate();
            if (inside instanceof GetSlotAVM2Item) {
                GetSlotAVM2Item slotItem = (GetSlotAVM2Item) inside;
                if ((slotItem.scope.getThroughRegister() == obj.getThroughRegister())
                        && (slotItem.slotName == slotname)) {
                    stack.moveToStack(output);
                    if (!stack.isEmpty()) {
                        GraphTargetItem top = stack.peek().getNotCoerced().getThroughDuplicate();
                        if (top == inside) {
                            //GraphTargetItem.checkDup(stack, output, inside, top);
                            GraphTargetItem.checkDup(stack, output, stack.pop(), value.getNotCoercedNoDup().value);
                            //stack.pop();
                            //TestIncDec12 with result
                            stack.push(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        } else if ((top instanceof IncrementAVM2Item) && (((IncrementAVM2Item) top).value == inside)) {
                            GraphTargetItem.checkDup(stack, output, stack.pop(), value.getNotCoercedNoDup());
                            //stack.pop();
                            //TestIncDec11 with result
                            stack.push(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        } else {
                            stack.addToOutput(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        }
                    } else {
                        stack.addToOutput(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, inside));
                    }
                    return;
                }
            }
        }

        if (value.getNotCoerced().getThroughDuplicate() instanceof DecrementAVM2Item) {
            GraphTargetItem inside = ((DecrementAVM2Item) value.getNotCoerced()).value.getThroughRegister().getNotCoerced().getThroughDuplicate();
            if (inside instanceof GetSlotAVM2Item) {
                GetSlotAVM2Item slotItem = (GetSlotAVM2Item) inside;
                if ((slotItem.scope.getThroughRegister() == obj.getThroughRegister())
                        && (slotItem.slotName == slotname)) {
                    stack.moveToStack(output);                    
                    if (!stack.isEmpty()) {
                        GraphTargetItem top = stack.peek().getNotCoerced().getThroughDuplicate();
                        if (top == inside) {
                            GraphTargetItem.checkDup(stack, output, stack.pop(), value.getNotCoercedNoDup().value);
                            //stack.pop();
                            //TestIncDec12 with result
                            stack.push(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        } else if ((top instanceof DecrementAVM2Item) && (((DecrementAVM2Item) top).value == inside)) {
                            GraphTargetItem.checkDup(stack, output, stack.pop(), value.getNotCoercedNoDup());
                            //stack.pop();
                            //TestIncDec11 with result
                            stack.push(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        } else {
                            stack.addToOutput(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, inside));
                        }
                    } else {
                        stack.addToOutput(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, inside));
                    }
                    return;
                }
            }
        }

        GraphTargetItem slotType = TypeItem.UNBOUNDED;
        if (obj instanceof NewActivationAVM2Item) {
            for (Trait t : localData.methodBody.traits.traits) {
                if (t instanceof TraitSlotConst) {
                    TraitSlotConst tsc = (TraitSlotConst) t;
                    if (tsc.slot_id == slotIndex) {
                        slotType = AbcIndexing.multinameToType(localData.usedDeobfuscations, tsc.type_index, localData.abc, localData.abc.constants);
                        break;
                    }
                }
            }
        }

        SetSlotAVM2Item result = new SetSlotAVM2Item(ins, localData.lineStartInstruction, obj, objnoreg, slotIndex, slotname, value, slotType);

        if (value.getNotCoerced() instanceof CompoundableBinaryOp) {
            if (!obj.hasSideEffect()) {
                CompoundableBinaryOp binaryOp = (CompoundableBinaryOp) value.getNotCoerced();
                if (binaryOp.getLeftSide().getNotCoerced() instanceof GetSlotAVM2Item) {
                    GetSlotAVM2Item getSlot = (GetSlotAVM2Item) binaryOp.getLeftSide().getNotCoerced();
                    if (Objects.equals(obj, getSlot.scope.getThroughDuplicate()) && slotIndex == getSlot.slotIndex) {
                        result.compoundValue = binaryOp.getRightSide();
                        result.compoundOperator = binaryOp.getOperator();
                    }
                }
            }
        }

        SetTypeIns.handleResult(value, stack, output, localData, result, -1, slotType);
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 2;
    }
}
