/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetSuperAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSuperAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.CompoundableBinaryOp;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class SetSuperIns extends InstructionDefinition implements SetTypeIns {

    public SetSuperIns() {
        super(0x05, "setsuper", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];

        GraphTargetItem value = stack.pop();

        FullMultinameAVM2Item multiname = resolveMultiname(localData, true, stack, localData.getConstants(), multinameIndex, ins);
        GraphTargetItem obj = stack.pop();
        SetSuperAVM2Item result = new SetSuperAVM2Item(ins, localData.lineStartInstruction, value, obj, multiname);

        if (value.getNotCoercedNoDup() instanceof CompoundableBinaryOp) {
            if (!obj.hasSideEffect() && !multiname.hasSideEffect()) {
                CompoundableBinaryOp binaryOp = (CompoundableBinaryOp) value.getNotCoercedNoDup();
                if (binaryOp.getLeftSide() instanceof GetSuperAVM2Item) {
                    GetSuperAVM2Item getSuper = (GetSuperAVM2Item) binaryOp.getLeftSide();
                    if (Objects.equals(obj, getSuper.object.getThroughDuplicate()) && Objects.equals(multiname, getSuper.propertyName)) {
                        result.setCompoundValue(binaryOp.getRightSide());
                        result.setCompoundOperator(binaryOp.getOperator());
                    }
                }
            }
        }

        SetTypeIns.handleResult(value, stack, output, localData, result, -1);
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        int multinameIndex = ins.operands[0];
        return 2 + getMultinameRequiredStackSize(abc.constants, multinameIndex);
    }

}
