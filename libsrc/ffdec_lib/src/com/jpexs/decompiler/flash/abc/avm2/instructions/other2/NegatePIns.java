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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other2;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Runtime;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2VerifyErrorException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;

/**
 *
 * @author JPEXS
 */
public class NegatePIns extends InstructionDefinition {

    public NegatePIns() {
        super(0x8F, "negate_p", new int[]{AVM2Code.DAT_NUMBER_CONTEXT}, true /*?*/, AVM2InstructionFlag.NO_FLASH_PLAYER, AVM2InstructionFlag.ES4_NUMERICS_MINOR);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        throw new UnsupportedOperationException();
    }
}
