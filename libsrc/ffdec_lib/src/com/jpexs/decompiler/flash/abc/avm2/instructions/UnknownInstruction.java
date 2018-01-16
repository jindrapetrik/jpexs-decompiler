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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2VerifyErrorException;

/**
 *
 * @author JPEXS
 */
public class UnknownInstruction extends InstructionDefinition {

    public UnknownInstruction(int instructionCode) {
        super(instructionCode, "instruction_" + Integer.toString(instructionCode), new int[0], false);
    }

    @Override
    public void verify(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2VerifyErrorException {
        illegalOpCode(lda, ins);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2ExecutionException {
        return false;
    }
}
