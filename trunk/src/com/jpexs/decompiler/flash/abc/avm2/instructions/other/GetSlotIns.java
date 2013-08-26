/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.instructions.other;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
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
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class GetSlotIns extends InstructionDefinition {

    public GetSlotIns() {
        super(0x6c, "getslot", new int[]{AVM2Code.DAT_SLOT_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        int slotIndex = ins.operands[0];
        GraphTargetItem obj = (GraphTargetItem) stack.pop(); //scope
        obj = obj.getThroughRegister();
        Multiname slotname = null;
        if (obj instanceof ExceptionAVM2Item) {
            slotname = constants.constant_multiname[((ExceptionAVM2Item) obj).exception.name_index];
        } else if (obj instanceof ClassAVM2Item) {
            slotname = ((ClassAVM2Item) obj).className;
        } else if (obj instanceof ThisAVM2Item) {
            slotname = ((ThisAVM2Item) obj).className;
        } else if (obj instanceof ScriptAVM2Item) {
            for (int t = 0; t < abc.script_info[((ScriptAVM2Item) obj).scriptIndex].traits.traits.length; t++) {
                Trait tr = abc.script_info[((ScriptAVM2Item) obj).scriptIndex].traits.traits[t];
                if (tr instanceof TraitWithSlot) {
                    if (((TraitWithSlot) tr).getSlotIndex() == slotIndex) {
                        slotname = tr.getName(abc);
                    }
                }
            }
        } else if (obj instanceof NewActivationAVM2Item) {

            for (int t = 0; t < body.traits.traits.length; t++) {
                if (body.traits.traits[t] instanceof TraitWithSlot) {
                    if (((TraitWithSlot) body.traits.traits[t]).getSlotIndex() == slotIndex) {
                        slotname = body.traits.traits[t].getName(abc);
                    }
                }

            }
        }
        stack.push(new GetSlotAVM2Item(ins, obj, slotname));
    }

    @Override
    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        return -1 + 1;
    }
}
