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
package com.jpexs.decompiler.flash.abc.avm2.instructions.stack;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class PushScopeIns extends InstructionDefinition {

    public PushScopeIns() {
        super(0x30, "pushscope", new int[]{}, false);
    }

    @Override
    public boolean isNotCompileTimeSupported() {
        return true;
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        lda.scopeStack.push(lda.operandStack.pop());
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        GraphTargetItem top = stack.pop();
        
        //Hack for catch inside catch to not detect pushscope register as used
        if (top instanceof LocalRegAVM2Item) {
            LocalRegAVM2Item getLocal = (LocalRegAVM2Item)top;;
            if(getLocal.getSrc() != null){
                int getLocalIp = localData.code.adr2pos(getLocal.getSrc().getAddress());
                for(int setLocalPos : localData.setLocalPosToGetLocalPos.keySet()){
                    if (localData.setLocalPosToGetLocalPos.get(setLocalPos).contains(getLocalIp)){
                        localData.setLocalPosToGetLocalPos.get(setLocalPos).remove(getLocalIp);
                    }
                }
            }
            
        }
        localData.scopeStack.push(top);
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    @Override
    public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
