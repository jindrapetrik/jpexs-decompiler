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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.IncLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.AddAVM2Item;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.NotCompileTime;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IncLocalIIns extends InstructionDefinition {

    public IncLocalIIns() {
        super(0xc2, "inclocal_i", new int[]{AVM2Code.DAT_LOCAL_REG_INDEX}, true);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        int locRegIndex = ins.getParamAsLong(constants, 0).intValue();
        Object obj = lda.localRegisters.get(locRegIndex);
        if (obj != NotCompileTime.INSTANCE) {
            lda.localRegisters.put(locRegIndex, EcmaScript.toInt32(obj) + 1);
        }

        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int regId = ins.operands[0];
        boolean isPostInc = false;
        if (!stack.isEmpty()) {
            GraphTargetItem stackTop = stack.peek();
            if (stackTop instanceof LocalRegAVM2Item) {
                if (regId == ((LocalRegAVM2Item) stackTop).regIndex) {
                    stack.pop();
                    stack.push(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, stackTop));
                    isPostInc = true;
                }
            }
        }
        if (!isPostInc) {
            output.add(new IncLocalAVM2Item(ins, localData.lineStartInstruction, regId));
        }
        if (localData.localRegs.containsKey(regId)) {
            localData.localRegs.put(regId, new AddAVM2Item(ins, localData.lineStartInstruction, localData.localRegs.get(regId), new IntegerValueAVM2Item(ins, localData.lineStartInstruction, 1L)));
        }
        if (!localData.localRegAssignmentIps.containsKey(regId)) {
            localData.localRegAssignmentIps.put(regId, 0);
        }
        localData.localRegAssignmentIps.put(regId, localData.localRegAssignmentIps.get(regId) + 1);
    }
}
