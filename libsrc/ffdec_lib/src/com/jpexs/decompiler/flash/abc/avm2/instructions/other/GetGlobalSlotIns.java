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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.GetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GetGlobalSlotIns extends InstructionDefinition {

    public GetGlobalSlotIns() {
        super(0x6e, "getglobalslot", new int[]{AVM2Code.DAT_SLOT_INDEX}, false);
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int slotIndex = ins.operands[0];
        GraphTargetItem obj = localData.scopeStack.get(0); //scope
        Multiname slotname = null;
        if (obj instanceof ExceptionAVM2Item) {
            slotname = localData.getConstants().getMultiname(((ExceptionAVM2Item) obj).exception.name_index);
        } else {
            MethodBody body = localData.methodBody;
            List<Trait> traits = body.traits.traits;
            for (int t = 0; t < traits.size(); t++) {
                Trait trait = traits.get(t);
                if (trait instanceof TraitSlotConst) {
                    if (((TraitSlotConst) trait).slot_id == slotIndex) {
                        slotname = trait.getName(localData.abc);
                    }
                }

            }
        }
        stack.push(new GetSlotAVM2Item(ins, localData.lineStartInstruction, obj, slotname));
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
