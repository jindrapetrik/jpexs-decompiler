/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other2;

import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Runtime;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;

public class AddPIns extends InstructionDefinition {

    public AddPIns() {
        super(0xB5, "add_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}, true /*?*/);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2ExecutionException {
        if (lda.runtime == AVM2Runtime.ADOBE_FLASH) {
            illegalOpCode(lda, ins);
        }

        return super.execute(lda, constants, ins);
    }
}
