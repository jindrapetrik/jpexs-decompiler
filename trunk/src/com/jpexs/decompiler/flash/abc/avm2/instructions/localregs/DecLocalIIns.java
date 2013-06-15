/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.DecLocalTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.IntegerValueTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NotCompileTimeTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.operations.SubtractTreeItem;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
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
    public void translate(boolean isStatic, int scriptIndex, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        int regIndex = ins.operands[0];
        output.add(new DecLocalTreeItem(ins, regIndex));
        if (localRegs.containsKey(regIndex)) {
            localRegs.put(regIndex, new NotCompileTimeTreeItem(ins, new SubtractTreeItem(ins, localRegs.get(regIndex), new IntegerValueTreeItem(ins, new Long(1)))));
        } else {
            //localRegs.put(regIndex, new SubtractTreeItem(ins, new IntegerValueTreeItem(ins, new Long(0)), new IntegerValueTreeItem(ins, new Long(1))));
        }

    }
}
