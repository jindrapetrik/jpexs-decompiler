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
import com.jpexs.decompiler.flash.abc.avm2.model.CallStaticAVM2Item;
import com.jpexs.decompiler.flash.ecma.NotCompileTime;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CallStaticIns extends InstructionDefinition {

    public CallStaticIns() {
        super(0x44, "callstatic", new int[]{AVM2Code.DAT_METHOD_INDEX, AVM2Code.DAT_ARG_COUNT}, true);
    }

    @Override
    public boolean isNotCompileTimeSupported() {
        return true;
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        //int methodIndex = ins.getParamAsLong(constants, 0).intValue(); //index of method_info
        int argCount = ins.getParamAsLong(constants, 1).intValue();
        /*List<Object> passArguments = new ArrayList<Object>();
         for (int i = argCount - 1; i >= 0; i--) {
         passArguments.set(i, lda.operandStack.pop());
         }*/
        for (int i = 0; i < argCount; i++) {
            lda.operandStack.pop();
        }

        Object receiver = lda.operandStack.pop();

        //push(result)
        lda.operandStack.push(NotCompileTime.INSTANCE);
        //lda.executionException = "Call to unknown static method";
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int methodIndex = ins.operands[0];
        int argCount = ins.operands[1];
        List<GraphTargetItem> args = new ArrayList<>();
        for (int a = 0; a < argCount; a++) {
            args.add(0, stack.pop());
        }
        GraphTargetItem receiver = stack.pop();
        String methodName = localData.getMethodInfo().get(methodIndex).getName(localData.getConstants());
        stack.push(new CallStaticAVM2Item(ins, localData.lineStartInstruction, receiver, methodName, args));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return ins.operands[1] + 1;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
