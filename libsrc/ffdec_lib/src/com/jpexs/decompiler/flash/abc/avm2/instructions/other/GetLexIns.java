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
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.PropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
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
public class GetLexIns extends InstructionDefinition {

    public GetLexIns() {
        super(0x60, "getlex", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    public static GraphTargetItem resolveLexType(
            AVM2LocalData localData,
            GraphTargetItem obj,
            int multinameIndex,
            Reference<Boolean> isStatic, boolean call) {
        GraphTargetItem type = null;
        String multinameStr = localData.abc.constants.getMultiname(multinameIndex).getName(localData.abc.constants, new ArrayList<>(), true, true);
        for (Trait t : localData.methodBody.traits.traits) {
            if (t instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) t;
                if (Objects.equals(
                        tsc.getName(localData.abc).getName(localData.abc.constants, new ArrayList<>(), true, true),
                        multinameStr
                )) {
                    type = PropertyAVM2Item.multinameToType(tsc.type_index, localData.abc.constants);
                    break;
                }
            }
        }

        if (type == null) {
            if (localData.abcIndex != null) {
                String currentClassName = localData.classIndex == -1 ? null : localData.abc.instance_info.get(localData.classIndex).getName(localData.abc.constants).getNameWithNamespace(localData.abc.constants, true).toRawString();
                GraphTargetItem thisPropType = TypeItem.UNBOUNDED;
                if (currentClassName != null) {
                    if (call) {
                        thisPropType = localData.abcIndex.findPropertyCallType(localData.abc, new TypeItem(currentClassName), multinameStr, localData.abc.constants.getMultiname(multinameIndex).namespace_index, true, true);
                    } else {
                        thisPropType = localData.abcIndex.findPropertyType(localData.abc, new TypeItem(currentClassName), multinameStr, localData.abc.constants.getMultiname(multinameIndex).namespace_index, true, true);
                    }
                }
                if (!thisPropType.equals(TypeItem.UNBOUNDED)) {
                    type = thisPropType;
                }

                if (type == null) {
                    TypeItem ti = new TypeItem(localData.abc.constants.getMultiname(multinameIndex).getNameWithNamespace(localData.abc.constants, true));
                    if (localData.abcIndex.findClass(ti) != null) {
                        type = ti;
                        isStatic.setVal(true);
                    }
                }
            }
        }

        if (type == null) {
            type = TypeItem.UNBOUNDED;
        }
        return type;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];
        Multiname multiname = localData.getConstants().getMultiname(multinameIndex);
        Reference<Boolean> isStatic = new Reference<>(false);
        GraphTargetItem type = GetLexIns.resolveLexType(localData, null, multinameIndex, isStatic, false);
        stack.push(new GetLexAVM2Item(ins, localData.lineStartInstruction, multiname, localData.getConstants(), type, isStatic.getVal()));
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
