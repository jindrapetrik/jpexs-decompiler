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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2.instructions.executing;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.CallAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.ecma.NotCompileTime;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CallIns extends InstructionDefinition {

    public CallIns() {
        super(0x41, "call", new int[]{AVM2Code.DAT_ARG_COUNT}, true);
    }

    @Override
    public boolean isNotCompileTimeSupported() {
        return true;
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        int argCount = ins.getParamAsLong(constants, 0).intValue();
        /* List<Object> passArguments = new ArrayList<Object>();
         for (int i = argCount - 1; i >= 0; i--) {
         passArguments.set(i, lda.operandStack.pop());
         }*/
        for (int i = 0; i < argCount; i++) {
            lda.operandStack.pop();
        }

        Object receiver = lda.operandStack.pop();
        Object function = lda.operandStack.pop();

        //push(result)
        lda.operandStack.push(NotCompileTime.INSTANCE);
        //lda.executionException = "Call to unknown function";
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int argCount = ins.operands[0];
        List<GraphTargetItem> args = new ArrayList<>();
        for (int a = 0; a < argCount; a++) {
            args.add(0, stack.pop());
        }
        GraphTargetItem receiver = stack.pop();
        GraphTargetItem function = stack.pop();

        if (function instanceof GetPropertyAVM2Item) {
            GetPropertyAVM2Item getProperty = (GetPropertyAVM2Item) function;
            if (getProperty.object instanceof DuplicateItem) {
                if (getProperty.object.value == receiver) {
                    getProperty.object = receiver;
                }
            }
            else if (getProperty.object instanceof SetLocalAVM2Item) {
                SetLocalAVM2Item setLocal = (SetLocalAVM2Item) getProperty.object;
                if (receiver instanceof LocalRegAVM2Item) {
                    LocalRegAVM2Item getLocal = (LocalRegAVM2Item) receiver;
                    if (getLocal.regIndex == setLocal.regIndex) {
                        getProperty.object = getProperty.object.value;
                        receiver = getProperty.object;
                    }
                }
            }
        }
        stack.push(new CallAVM2Item(ins, localData.lineStartInstruction, receiver, function, args));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return ins.operands[0] + 2;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
