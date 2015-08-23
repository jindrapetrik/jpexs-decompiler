/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ClassAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ScriptAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.TraitWithSlot;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class SetSlotIns extends InstructionDefinition implements SetTypeIns {

    public SetSlotIns() {
        super(0x6d, "setslot", new int[]{AVM2Code.DAT_SLOT_INDEX}, true);
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, AVM2ConstantPool constants, AVM2Instruction ins, List<MethodInfo> method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        int slotIndex = ins.operands[0];
        GraphTargetItem value = stack.pop();
        GraphTargetItem obj = stack.pop(); //scopeId
        GraphTargetItem objnoreg = obj;
        obj = obj.getThroughRegister();
        Multiname slotname = null;
        if (obj instanceof NewActivationAVM2Item) {
            ((NewActivationAVM2Item) obj).slots.put(slotIndex, value);
        }

        if (obj instanceof ExceptionAVM2Item) {
            slotname = constants.getMultiname(((ExceptionAVM2Item) obj).exception.name_index);
        } else if (obj instanceof ClassAVM2Item) {
            slotname = ((ClassAVM2Item) obj).className;
        } else if (obj instanceof ThisAVM2Item) {
            slotname = ((ThisAVM2Item) obj).className;
        } else if (obj instanceof ScriptAVM2Item) {
            for (int t = 0; t < abc.script_info.get(((ScriptAVM2Item) obj).scriptIndex).traits.traits.size(); t++) {
                Trait tr = abc.script_info.get(((ScriptAVM2Item) obj).scriptIndex).traits.traits.get(t);
                if (tr instanceof TraitWithSlot) {
                    if (((TraitWithSlot) tr).getSlotIndex() == slotIndex) {
                        slotname = tr.getName(abc);
                    }
                }
            }
        } else if (obj instanceof NewActivationAVM2Item) {

            for (int t = 0; t < body.traits.traits.size(); t++) {
                if (body.traits.traits.get(t) instanceof TraitWithSlot) {
                    if (((TraitWithSlot) body.traits.traits.get(t)).getSlotIndex() == slotIndex) {
                        slotname = body.traits.traits.get(t).getName(abc);
                    }
                }

            }
        }

        if (slotname != null) {
            if (value instanceof LocalRegAVM2Item) {
                LocalRegAVM2Item lr = (LocalRegAVM2Item) value;
                String slotNameStr = slotname.getName(constants, fullyQualifiedNames, true);
                if (localRegNames.containsKey(lr.regIndex)) {
                    if (localRegNames.get(lr.regIndex).equals(slotNameStr)) {
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
                    if (stack.size() > 0) {
                        GraphTargetItem top = stack.peek().getNotCoerced().getThroughDuplicate();
                        if (top == inside) {
                            stack.pop();
                            stack.push(new PostIncrementAVM2Item(ins, inside));
                        } else if ((top instanceof IncrementAVM2Item) && (((IncrementAVM2Item) top).value == inside)) {
                            stack.pop();
                            stack.push(new PreIncrementAVM2Item(ins, inside));
                        } else {
                            output.add(new PostIncrementAVM2Item(ins, inside));
                        }
                    } else {
                        output.add(new PostIncrementAVM2Item(ins, inside));
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
                    if (stack.size() > 0) {
                        GraphTargetItem top = stack.peek().getNotCoerced().getThroughDuplicate();
                        if (top == inside) {
                            stack.pop();
                            stack.push(new PostDecrementAVM2Item(ins, inside));
                        } else if ((top instanceof DecrementAVM2Item) && (((DecrementAVM2Item) top).value == inside)) {
                            stack.pop();
                            stack.push(new PreDecrementAVM2Item(ins, inside));
                        } else {
                            output.add(new PostDecrementAVM2Item(ins, inside));
                        }
                    } else {
                        output.add(new PostDecrementAVM2Item(ins, inside));
                    }
                    return;
                }
            }
        }

        output.add(new SetSlotAVM2Item(ins, obj, slotname, value));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 2;
    }

    @Override
    public String getObject(Stack<AVM2Item> stack, ABC abc, AVM2Instruction ins, List<AVM2Item> output, MethodBody body, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames) {
        int slotIndex = ins.operands[0];
        ////String obj = stack.get(1);
        String slotname = "";
        for (int t = 0; t < body.traits.traits.size(); t++) {
            if (body.traits.traits.get(t) instanceof TraitSlotConst) {
                if (((TraitSlotConst) body.traits.traits.get(t)).slot_id == slotIndex) {
                    slotname = body.traits.traits.get(t).getName(abc).getName(abc.constants, fullyQualifiedNames, true);
                }
            }

        }
        return slotname;
    }
}
