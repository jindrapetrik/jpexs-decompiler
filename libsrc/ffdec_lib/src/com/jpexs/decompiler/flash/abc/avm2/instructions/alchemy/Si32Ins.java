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
package com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2RangeErrorException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.AlchemyStoreAVM2Item;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class Si32Ins extends InstructionDefinition implements AlchemyTypeIns {

    public Si32Ins() {
        super(0x3C, "si32", new int[]{}, true, AVM2InstructionFlag.DOMAIN_MEMORY);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2RangeErrorException {
        int addr = EcmaScript.toInt32(lda.operandStack.pop());
        byte[] domainMemory = lda.getDomainMemory();
        if (addr < 0 || addr >= domainMemory.length) {
            throw new AVM2RangeErrorException(1506, lda.isDebug());
        }

        int value = EcmaScript.toInt32(lda.operandStack.pop());
        // todo: set domainMemory
        //domainMemory[addr] = ...
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        GraphTargetItem ofs = stack.pop();
        GraphTargetItem value = stack.pop();
        output.add(new AlchemyStoreAVM2Item(ins, localData.lineStartInstruction, value, ofs, "i", 32));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 2;
    }
}
