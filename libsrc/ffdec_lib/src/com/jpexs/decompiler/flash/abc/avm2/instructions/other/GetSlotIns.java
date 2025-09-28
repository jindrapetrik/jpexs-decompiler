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
import com.jpexs.decompiler.flash.abc.avm2.model.GetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.Reference;
import java.util.List;

/**
 * getslot instruction - get slot value.
 *
 * @author JPEXS
 */
public class GetSlotIns extends InstructionDefinition {

    /**
     * Constructor
     */
    public GetSlotIns() {
        super(0x6c, "getslot", new int[]{AVM2Code.DAT_SLOT_INDEX}, true);
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int slotIndex = ins.operands[0];
        GraphTargetItem objinreg = stack.pop(); //scope
        GraphTargetItem obj = objinreg.getThroughRegister();
        Reference<GraphTargetItem> realObj = new Reference<>(null);
        Multiname slotname = InstructionDefinition.searchSlotName(slotIndex, localData, obj, realObj);
        if (realObj.getVal() != null) {
            obj = realObj.getVal();
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
        stack.push(new GetSlotAVM2Item(ins, localData.lineStartInstruction, obj, objinreg, slotIndex, slotname, slotType));
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
