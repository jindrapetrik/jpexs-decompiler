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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.PropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class GetLexIns extends InstructionDefinition {

    public GetLexIns() {
        super(0x60, "getlex", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];
        Multiname multiname = localData.getConstants().getMultiname(multinameIndex);
        String multinameStr = multiname.getName(localData.abc.constants, new ArrayList<>(), true, true);
        GraphTargetItem slotType = TypeItem.UNBOUNDED;
        for (Trait t : localData.methodBody.traits.traits) {
            if (t instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) t;
                if (Objects.equals(
                        tsc.getName(localData.abc).getName(localData.abc.constants, new ArrayList<>(), true, true),
                        multinameStr
                )) {
                    slotType = PropertyAVM2Item.multinameToType(tsc.type_index, localData.abc.constants);
                    break;
                }
            }
        }
        stack.push(new GetLexAVM2Item(ins, localData.lineStartInstruction, multiname, localData.getConstants(), slotType));
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
