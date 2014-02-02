/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.DecLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.SubtractAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class DecLocalIIns extends InstructionDefinition {

    public DecLocalIIns() {
        super(0xc3, "declocal_i", new int[]{AVM2Code.DAT_LOCAL_REG_INDEX});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List<Object> arguments) {
        int locRegIndex = (int) ((Long) arguments.get(0)).longValue();
        Object obj = lda.localRegisters.get(locRegIndex);
        if (obj instanceof Long) {
            Long obj2 = ((Long) obj).longValue() - 1;
            lda.localRegisters.put(locRegIndex, obj2);
        } else if (obj instanceof Double) {
            Double obj2 = ((Double) obj).doubleValue() - 1;
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
    public void translate(boolean isStatic, int scriptIndex, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, String path, HashMap<Integer, Integer> regAssignCount, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        int regId = ins.operands[0];
        output.add(new DecLocalAVM2Item(ins, regId));
        if (localRegs.containsKey(regId)) {
            localRegs.put(regId, new SubtractAVM2Item(ins, localRegs.get(regId), new IntegerValueAVM2Item(ins, Long.valueOf(1))));
        } else {
            //localRegs.put(regIndex, new SubtractAVM2Item(ins, new IntegerValueAVM2Item(ins, new Long(0)), new IntegerValueAVM2Item(ins, new Long(1))));
        }
        if (!regAssignCount.containsKey(regId)) {
            regAssignCount.put(regId, 0);
        }
        regAssignCount.put(regId, regAssignCount.get(regId) + 1);

    }
}
