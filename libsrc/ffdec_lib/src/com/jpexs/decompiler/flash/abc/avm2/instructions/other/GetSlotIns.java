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
import com.jpexs.decompiler.flash.abc.avm2.model.ClassAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ScriptAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitWithSlot;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.HashMap;
import java.util.List;

public class GetSlotIns extends InstructionDefinition {

    public GetSlotIns() {
        super(0x6c, "getslot", new int[]{AVM2Code.DAT_SLOT_INDEX}, true);
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, AVM2ConstantPool constants, AVM2Instruction ins, List<MethodInfo> method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        int slotIndex = ins.operands[0];
        GraphTargetItem obj = stack.pop(); //scope
        obj = obj.getThroughRegister();
        Multiname slotname = null;
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
        stack.push(new GetSlotAVM2Item(ins, obj, slotname));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
