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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSuperAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class SetSuperIns extends InstructionDefinition implements SetTypeIns {

    public SetSuperIns() {
        super(0x05, "setsuper", new int[]{AVM2Code.DAT_MULTINAME_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        int multinameIndex = ins.operands[0];

        GraphTargetItem value = (GraphTargetItem) stack.pop();
        FullMultinameAVM2Item multiname = resolveMultiname(stack, constants, multinameIndex, ins);
        GraphTargetItem obj = (GraphTargetItem) stack.pop();
        output.add(new SetSuperAVM2Item(ins, value, obj, multiname));
    }

    @Override
    public String getObject(Stack<AVM2Item> stack, ABC abc, AVM2Instruction ins, List<AVM2Item> output, MethodBody body, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) throws InterruptedException {
        int multinameIndex = ins.operands[0];
        String multiname = resolveMultinameNoPop(1, stack, abc.constants, multinameIndex, ins, fullyQualifiedNames);
        HilightedTextWriter writer = new HilightedTextWriter(false);
        stack.get(1 + resolvedCount(abc.constants, multinameIndex)).toString(writer, LocalData.create(abc.constants, localRegNames, fullyQualifiedNames));
        String obj = writer.toString();
        return obj + ".super." + multiname;
    }

    @Override
    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        int ret = -2;
        int multinameIndex = ins.operands[0];
        if (abc.constants.getMultiname(multinameIndex).needsName()) {
            ret--;
        }
        if (abc.constants.getMultiname(multinameIndex).needsNs()) {
            ret--;
        }
        return ret;
    }
}
