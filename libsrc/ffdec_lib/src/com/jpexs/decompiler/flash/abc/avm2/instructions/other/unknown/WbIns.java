/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Runtime;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2VerifyErrorException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;

/**
 * wb - undocumented opcode.
 *
 * @author JPEXS
 * <p>
 * source:
 * https://github.com/apache/flex-sdk/blob/414b9a3e55effd243a697e614b702d1fa0b53efe/modules/asc/src/java/macromedia/abc/Opcodes.java
 */
public class WbIns extends InstructionDefinition {

    /**
     * Constructor
     */
    public WbIns() {
        super(0xF8, "wb", new int[]{}, false /*?*/, AVM2InstructionFlag.NO_FLASH_PLAYER, AVM2InstructionFlag.UNDOCUMENTED, AVM2InstructionFlag.UNKNOWN_OPERANDS, AVM2InstructionFlag.UNKNOWN_STACK);
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
