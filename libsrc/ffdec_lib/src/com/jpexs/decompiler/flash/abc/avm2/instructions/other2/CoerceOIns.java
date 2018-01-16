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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other2;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceOrConvertTypeIns;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;

/**
 *
 * @author JPEXS
 */
public class CoerceOIns extends InstructionDefinition implements CoerceOrConvertTypeIns {

    public CoerceOIns() {
        super(0x89, "coerce_o", new int[]{}, true); // stack: -1+1
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        Object value = lda.operandStack.pop();
        // The only action appears to be to convert undefined to null.
        if (value == Undefined.INSTANCE) {
            value = Null.INSTANCE;
        }

        lda.operandStack.push(value);
        return true;
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    @Override
    public GraphTargetItem getTargetType(AVM2ConstantPool constants, AVM2Instruction ins) {
        return new TypeItem(DottedChain.OBJECT);
    }
}
