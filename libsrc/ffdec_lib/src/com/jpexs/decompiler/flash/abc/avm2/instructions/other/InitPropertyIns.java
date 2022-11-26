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
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.PropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class InitPropertyIns extends InstructionDefinition {

    public InitPropertyIns() {
        super(0x68, "initproperty", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];

        GraphTargetItem val = stack.pop();
        FullMultinameAVM2Item multiname = resolveMultiname(localData, true, stack, localData.getConstants(), multinameIndex, ins);
        GraphTargetItem obj = stack.pop();
        
        /*GraphTargetItem propertyType = TypeItem.UNBOUNDED;
        String multinameStr = localData.abc.constants.getMultiname(multiname.multinameIndex).getName(localData.abc.constants, new ArrayList<>(), true, true);
        
        for (Trait t : localData.methodBody.traits.traits) {
            if (t instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) t;
                if (Objects.equals(
                        tsc.getName(localData.abc).getName(localData.abc.constants, new ArrayList<>(), true, true),
                        multinameStr
                )) {
                    propertyType = PropertyAVM2Item.multinameToType(tsc.type_index, localData.abc.constants);
                    break;
                }
            }
        }*/
        
        Reference<Boolean> isStatic = new Reference<>(false);
        GraphTargetItem type = GetPropertyIns.resolvePropertyType(localData, obj, multiname, isStatic, false);
        
        InitPropertyAVM2Item result = new InitPropertyAVM2Item(ins, localData.lineStartInstruction, obj, multiname, val, type, isStatic.getVal());
        SetPropertyIns.handleCompound(localData, obj, multiname, val, output, result);
        output.add(result);
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        int multinameIndex = ins.operands[0];
        return 2 + getMultinameRequiredStackSize(abc.constants, multinameIndex);
    }
}
