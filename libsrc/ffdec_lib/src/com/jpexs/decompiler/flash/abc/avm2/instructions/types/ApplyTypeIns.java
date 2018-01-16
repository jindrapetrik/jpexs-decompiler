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
package com.jpexs.decompiler.flash.abc.avm2.instructions.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ApplyTypeIns extends InstructionDefinition {

    public ApplyTypeIns() {
        super(0x53, "applytype", new int[]{AVM2Code.DAT_ARG_COUNT}, true);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        int argCount = ins.getParamAsLong(constants, 0).intValue();
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < argCount; i++) {
            params.add(lda.operandStack.pop());
        }
        Collections.reverse(params);
        //TODO: pop type and push type<params>
        return false;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int argCount = ins.operands[0];
        List<GraphTargetItem> params = new ArrayList<>();
        for (int i = 0; i < argCount; i++) {
            params.add(0, stack.pop());
        }
        stack.push(new ApplyTypeAVM2Item(ins, localData.lineStartInstruction, stack.pop(), params));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return ins.operands[0] + 1;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
