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
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.DecLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.SubtractAVM2Item;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.NotCompileTime;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class DecLocalIns extends InstructionDefinition {

    public DecLocalIns() {
        super(0x94, "declocal", new int[]{AVM2Code.DAT_LOCAL_REG_INDEX}, true);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        int locRegIndex = ins.getParamAsLong(constants, 0).intValue();
        Object obj = lda.localRegisters.get(locRegIndex);
        if (obj != NotCompileTime.INSTANCE) {
            lda.localRegisters.put(locRegIndex, EcmaScript.toNumber(obj) - 1);
        }

        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int regId = ins.operands[0];
        output.add(new DecLocalAVM2Item(ins, localData.lineStartInstruction, regId));
        if (localData.localRegs.containsKey(regId)) {
            localData.localRegs.put(regId, new SubtractAVM2Item(ins, localData.lineStartInstruction, localData.localRegs.get(regId), new IntegerValueAVM2Item(ins, localData.lineStartInstruction, 1L)));
        } else {
            //localRegs.put(regIndex, new SubtractAVM2Item(ins, localData.lineStartInstruction, new IntegerValueAVM2Item(ins, localData.lineStartInstruction, new Long(0)), new IntegerValueAVM2Item(ins, localData.lineStartInstruction, new Long(1))));
        }
        if (!localData.localRegAssignmentIps.containsKey(regId)) {
            localData.localRegAssignmentIps.put(regId, 0);
        }
        localData.localRegAssignmentIps.put(regId, localData.localRegAssignmentIps.get(regId) + 1);
    }
}
