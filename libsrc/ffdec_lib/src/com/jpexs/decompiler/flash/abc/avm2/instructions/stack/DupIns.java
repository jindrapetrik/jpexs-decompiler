/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions.stack;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphTargetDialect;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.HasTempIndex;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import java.util.List;

/**
 * dup instruction - Duplicate the top value on the stack.
 *
 * @author JPEXS
 */
public class DupIns extends InstructionDefinition {

    /**
     * Constructor
     */
    public DupIns() {
        super(0x2a, "dup", new int[]{}, false);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        Object obj = lda.operandStack.pop();
        lda.operandStack.push(obj);
        lda.operandStack.push(obj);
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        GraphTargetItem v = stack.pop();
        int temp = 0;
                  
        if (v instanceof SimpleValue) {
            SimpleValue sv = (SimpleValue) v;
            if (sv.isSimpleValue()) {
                stack.push(v);
                stack.push(v);
                return;
            }
        }
        
        if (v instanceof NewActivationAVM2Item 
                || v instanceof ExceptionAVM2Item) {
            stack.push(v);
        } else {
            if (v instanceof HasTempIndex) {
                temp = ((HasTempIndex) v).getTempIndex();
                stack.push(v);
            } else {
                temp = localData.maxTempIndex.getVal() + 1;
                localData.maxTempIndex.setVal(temp);
                stack.finishBlock(output);
                stack.addToOutput(new SetTemporaryItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction, v, temp, "dup", 2));
                stack.finishBlock(output);
                
                stack.push(new DuplicateSourceItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction, v, temp));
            }            
        }
        //stack.push(v);
        stack.push(new DuplicateItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction, v, temp));
        //v.moreSrc.add(new GraphSourceItemPos(ins, 0));

    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 2;
    }
}
