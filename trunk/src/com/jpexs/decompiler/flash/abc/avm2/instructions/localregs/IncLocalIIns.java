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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.IncLocalTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.IntegerValueTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NotCompileTimeTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.operations.AddTreeItem;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class IncLocalIIns extends InstructionDefinition {

    public IncLocalIIns() {
        super(0xc2, "inclocal_i", new int[]{AVM2Code.DAT_LOCAL_REG_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        int regIndex = ins.operands[0];
        output.add(new IncLocalTreeItem(ins, regIndex));
        if (localRegs.containsKey(regIndex)) {
            localRegs.put(regIndex, new NotCompileTimeTreeItem(ins, new AddTreeItem(ins, localRegs.get(regIndex), new IntegerValueTreeItem(ins, Long.valueOf(1)))));
        } else {
            //localRegs.put(regIndex, new AddTreeItem(ins, null, new IntegerValueTreeItem(ins, new Long(1))));
        }
    }
}
