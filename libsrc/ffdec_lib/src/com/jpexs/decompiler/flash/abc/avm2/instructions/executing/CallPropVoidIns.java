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
package com.jpexs.decompiler.flash.abc.avm2.instructions.executing;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CallPropVoidIns extends InstructionDefinition {

    public CallPropVoidIns() {
        super(0x4f, "callpropvoid", new int[]{AVM2Code.DAT_MULTINAME_INDEX, AVM2Code.DAT_ARG_COUNT}, true);
    }

    @Override
    public boolean isNotCompileTimeSupported() {
        return true;
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        //same as callproperty
        int multinameIndex = ins.operands[0];
        int argCount = ins.getParamAsLong(constants, 1).intValue();
        /*List<Object> passArguments = new ArrayList<Object>();
         for (int i = argCount - 1; i >= 0; i--) {
         passArguments.set(i, lda.operandStack.pop());
         }*/
        for (int i = 0; i < argCount; i++) {
            lda.operandStack.pop();
        }

        //if multiname[multinameIndex] is runtime
        //pop(name) pop(ns)
        resolveMultiname(lda, constants, multinameIndex);
        Object obj = lda.operandStack.pop();

        //do not push anything
        //lda.executionException = "Call to unknown property";
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];
        int argCount = ins.operands[1];
        List<GraphTargetItem> args = new ArrayList<>();
        for (int a = 0; a < argCount; a++) {
            args.add(0, stack.pop());
        }
        FullMultinameAVM2Item multiname = resolveMultiname(localData, true, stack, localData.getConstants(), multinameIndex, ins);

        GraphTargetItem receiver = stack.pop();

        output.add(new CallPropertyAVM2Item(ins, localData.lineStartInstruction, true, receiver, multiname, args));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        int multinameIndex = ins.operands[0];
        return ins.operands[1] + 1 + getMultinameRequiredStackSize(abc.constants, multinameIndex);
    }
}
