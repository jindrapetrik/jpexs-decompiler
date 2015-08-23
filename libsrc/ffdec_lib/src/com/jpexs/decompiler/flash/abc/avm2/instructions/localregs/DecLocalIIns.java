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
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.DecLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.SubtractAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.HashMap;
import java.util.List;

public class DecLocalIIns extends InstructionDefinition {

    public DecLocalIIns() {
        super(0xc3, "declocal_i", new int[]{AVM2Code.DAT_LOCAL_REG_INDEX}, true);
    }

    @Override
    public void execute(LocalDataArea lda, AVM2ConstantPool constants, List<Object> arguments) {
        int locRegIndex = (int) ((Long) arguments.get(0)).longValue();
        Object obj = lda.localRegisters.get(locRegIndex);
        if (obj instanceof Long) {
            Long obj2 = ((Long) obj) - 1;
            lda.localRegisters.put(locRegIndex, obj2);
        } else if (obj instanceof Double) {
            Double obj2 = ((Double) obj) - 1;
            lda.localRegisters.put(locRegIndex, obj2);
        }
        if (obj instanceof String) {
            Double obj2 = Double.parseDouble((String) obj) - 1;
            lda.localRegisters.put(locRegIndex, obj2);
        } else {
            throw new RuntimeException("Cannot decrement local register");
        }
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, AVM2ConstantPool constants, AVM2Instruction ins, List<MethodInfo> method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, String path, HashMap<Integer, Integer> regAssignCount, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        int regId = ins.operands[0];
        output.add(new DecLocalAVM2Item(ins, regId));
        if (localRegs.containsKey(regId)) {
            localRegs.put(regId, new SubtractAVM2Item(ins, localRegs.get(regId), new IntegerValueAVM2Item(ins, 1L)));
        } else {
            //localRegs.put(regIndex, new SubtractAVM2Item(ins, new IntegerValueAVM2Item(ins, new Long(0)), new IntegerValueAVM2Item(ins, new Long(1))));
        }
        if (!regAssignCount.containsKey(regId)) {
            regAssignCount.put(regId, 0);
        }
        regAssignCount.put(regId, regAssignCount.get(regId) + 1);
    }
}
