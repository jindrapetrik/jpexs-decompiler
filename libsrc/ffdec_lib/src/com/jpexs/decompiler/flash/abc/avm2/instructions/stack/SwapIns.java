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
import com.jpexs.decompiler.flash.abc.avm2.model.EscapeXAttrAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.HasTempIndex;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import com.jpexs.decompiler.graph.model.SwapItem;
import com.jpexs.decompiler.graph.model.TemporaryItem;
import java.util.List;

/**
 * swap instruction - Swaps the top two values on the stack.
 *
 * @author JPEXS
 */
public class SwapIns extends InstructionDefinition {

    /**
     * Constructor
     */
    public SwapIns() {
        super(0x2b, "swap", new int[]{}, false);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        Object obj1 = lda.operandStack.pop();
        Object obj2 = lda.operandStack.pop();
        lda.operandStack.push(obj1);
        lda.operandStack.push(obj2);
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {

        GraphTargetItem o1 = stack.pop();
        GraphTargetItem o2 = stack.pop();
        
        
        /*if (true) {
            stack.push(o1);
            stack.push(o2);
            o1.getMoreSrc().add(new GraphSourceItemPos(ins, 0));
            o2.getMoreSrc().add(new GraphSourceItemPos(ins, 0));        
            return;
        }*/
        if (((o1 instanceof ExceptionAVM2Item) && (o2 instanceof ExceptionAVM2Item))
                ||
                (
                (
                    ((o1 instanceof SimpleValue) && ((SimpleValue) o1).isSimpleValue() && !(o1 instanceof DuplicateItem))
                    ||
                    (o1 instanceof EscapeXAttrAVM2Item)
                )
                &&
                (
                    ((o2 instanceof SimpleValue) && ((SimpleValue) o2).isSimpleValue() && !(o2 instanceof DuplicateItem))
                    ||
                    (o2 instanceof EscapeXAttrAVM2Item)
                )
                
                )                
            ) {
            stack.push(o1);
            stack.push(o2);
            o1.getMoreSrc().add(new GraphSourceItemPos(ins, 0));
            o2.getMoreSrc().add(new GraphSourceItemPos(ins, 0));
            return;
        }
        
        /*
        stack.finishBlock(output);
        if (!(o2 instanceof PopItem)) {
            output.add(new PushItem(o2));
        }
        if (!(o2 instanceof PopItem && o1 instanceof PopItem)) {
            output.add(new PushItem(o1));
        }
        output.add(new SwapItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction));
        */
        /*if (o2 instanceof HasTempIndex) {
            stack.push(o1);
            stack.push(o2);
            return;
        }*/
        /*
        if (o2 instanceof SetTemporaryItem || o2 instanceof DuplicateSourceItem) {
            HasTempIndex ti = (HasTempIndex) o2;            
            stack.addToOutput(new SetTemporaryItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction, o2.value, ti.getTempIndex(), "swap"));
            stack.push(o1);
            stack.push(new TemporaryItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction, o2, ti.getTempIndex()));
            return;
        }
        
        if (o2 instanceof TemporaryItem || o2 instanceof DuplicateItem) {
            stack.push(o1);
            stack.push(o2);
            return;
        }
        */
        /*int temp = localData.maxTempIndex.getVal() + 1;
        localData.maxTempIndex.setVal(temp);
        stack.finishBlock(output);
        stack.addToOutput(new SetTemporaryItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction, o2, temp, "swap"));
        stack.finishBlock(output);  
        stack.push(o1);
        stack.push(new TemporaryItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction, o2, temp)); */
        
        if (o2 instanceof TemporaryItem) {
            stack.push(o1);
            stack.push(o2);
            return;
        }
        if (o2 instanceof DuplicateItem) {
            stack.push(o1);
            stack.push(o2);
            return;
        }
        
        if (o2 instanceof DuplicateSourceItem) {
            stack.push(o1);
            stack.push(o2);
            return;
        }
        
        
        int temp = localData.maxTempIndex.getVal() + 1;
        localData.maxTempIndex.setVal(temp);
        stack.finishBlock(output);
        stack.addToOutput(new SetTemporaryItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction, o2, temp, "swap", 1));
        stack.push(o1);
        stack.push(new TemporaryItem(AVM2GraphTargetDialect.INSTANCE, ins, localData.lineStartInstruction, o2, temp));       
        stack.finishBlock(output);        
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 2;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 2;
    }
}
