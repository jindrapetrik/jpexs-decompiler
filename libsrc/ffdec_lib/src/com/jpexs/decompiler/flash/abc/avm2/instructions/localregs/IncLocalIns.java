/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.IncLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.AddAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.HashMap;
import java.util.List;

public class IncLocalIns extends InstructionDefinition {

    public IncLocalIns() {
        super(0x92, "inclocal", new int[]{AVM2Code.DAT_LOCAL_REG_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, AVM2ConstantPool constants, AVM2Instruction ins, List<MethodInfo> method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, String path, HashMap<Integer, Integer> regAssignCount, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        int regId = ins.operands[0];
        output.add(new IncLocalAVM2Item(ins, regId));
        if (localRegs.containsKey(regId)) {
            localRegs.put(regId, new AddAVM2Item(ins, localRegs.get(regId), new IntegerValueAVM2Item(ins, 1L)));
        } else {
            //localRegs.put(regIndex, new AddAVM2Item(ins, null, new IntegerValueAVM2Item(ins, new Long(1))));
        }
        if (!regAssignCount.containsKey(regId)) {
            regAssignCount.put(regId, 0);
        }
        regAssignCount.put(regId, regAssignCount.get(regId) + 1);
    }
}
