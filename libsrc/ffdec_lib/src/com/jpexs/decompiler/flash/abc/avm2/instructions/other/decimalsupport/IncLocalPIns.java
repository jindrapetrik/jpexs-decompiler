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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Runtime;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2VerifyErrorException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.IncLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.AddAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

/**
 * inclocal_p instruction - increment a local register with number context.
 *
 * @author JPEXS
 */
public class IncLocalPIns extends InstructionDefinition {

    /**
     * Constructor
     */
    public IncLocalPIns() {
        super(0x9D, "inclocal_p", new int[]{AVM2Code.DAT_NUMBER_CONTEXT, AVM2Code.DAT_LOCAL_REG_INDEX}, true /*?*/, AVM2InstructionFlag.ES4_NUMERICS_MINOR, AVM2InstructionFlag.NO_FLASH_PLAYER);
    }

    @Override
    public void verify(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2VerifyErrorException {
        if (lda.getRuntime() == AVM2Runtime.ADOBE_FLASH) {
            illegalOpCode(lda, ins);
        }

        super.verify(lda, constants, ins);
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 0;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 0;
    }
    
    //same for inclocal and inclocalp (decimal)
    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int regId = ins.operands[1];
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
            stack.addToOutput(new IncLocalAVM2Item(ins, localData.lineStartInstruction, regId));
        }
        if (localData.localRegs.containsKey(regId)) {
            localData.localRegs.put(regId, new AddAVM2Item(ins, localData.lineStartInstruction, localData.localRegs.get(regId), new IntegerValueAVM2Item(ins, localData.lineStartInstruction, 1)));
        }
        if (!localData.localRegAssignmentIps.containsKey(regId)) {
            localData.localRegAssignmentIps.put(regId, 0);
        }
        localData.localRegAssignmentIps.put(regId, localData.localRegAssignmentIps.get(regId) + 1);
    }
}
