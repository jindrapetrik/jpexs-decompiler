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
package com.jpexs.decompiler.flash.abc.avm2.instructions.construction;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.ecma.ArrayType;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.PopItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class NewArrayIns extends InstructionDefinition {

    public NewArrayIns() {
        super(0x56, "newarray", new int[]{AVM2Code.DAT_ARG_COUNT}, true);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2ExecutionException {
        //TODO: moar
        if (ins.operands[0] == 0) {
            lda.operandStack.push(ArrayType.EMPTY_ARRAY);
            return true;
        }
        return false;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int argCount = ins.operands[0];
        List<GraphTargetItem> args = new ArrayList<>();
        for (int a = 0; a < argCount; a++) {
            GraphTargetItem item = stack.pop();
            //No PopItems in this loop, since some obfuscators put there large numbers
            if (item instanceof PopItem) {
                stack.push(new NullAVM2Item(ins, localData.lineStartInstruction));
                return;
            }
            args.add(0, item);
        }
        stack.push(new NewArrayAVM2Item(ins, localData.lineStartInstruction, args));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return ins.operands[0];
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
