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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public abstract class GetLocalTypeIns extends InstructionDefinition {

    public GetLocalTypeIns(int instructionCode, String instructionName, int[] operands) {
        super(instructionCode, instructionName, operands);
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, String path, HashMap<Integer, Integer> regAssignCount, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        int regId = getRegisterId(ins);
        GraphTargetItem computedValue = localRegs.get(regId);
        int assignCount = 0;
        if (regAssignCount.containsKey(regId)) {
            assignCount = regAssignCount.get(regId);
        }
        if (assignCount > 5) { //Do not allow change register more than 5 - for deobfuscation
            computedValue = new NotCompileTimeItem(ins, computedValue);
        }
        /*if (!isRegisterCompileTime(regId, ip, refs, code)) {
         computedValue = new NotCompileTimeAVM2Item(ins, computedValue);
         }
         if (computedValue == null) {
         if (!localRegNames.containsKey(regId)) {
         computedValue = new UndefinedAVM2Item(null); //In some obfuscated code there seems to be reading of undefined registers
         }
         }*/
        stack.push(new LocalRegAVM2Item(ins, regId, computedValue));
    }

    @Override
    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    public abstract int getRegisterId(AVM2Instruction ins);
}
