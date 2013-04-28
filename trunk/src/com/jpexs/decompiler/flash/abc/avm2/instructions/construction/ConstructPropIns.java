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
package com.jpexs.decompiler.flash.abc.avm2.instructions.construction;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ConstructPropTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.XMLTreeItem;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ConstructPropIns extends InstructionDefinition {

    public ConstructPropIns() {
        super(0x4a, "constructprop", new int[]{AVM2Code.DAT_MULTINAME_INDEX, AVM2Code.DAT_ARG_COUNT});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        /*int multinameIndex = (int) ((Long) arguments.get(0)).longValue();
         int argCount = (int) ((Long) arguments.get(1)).longValue();
         List passArguments = new ArrayList();
         for (int i = argCount - 1; i >= 0; i--) {
         passArguments.set(i, lda.operandStack.pop());
         }*/
        //if multiname[multinameIndex] is runtime
        //pop(name) pop(ns)
        throw new RuntimeException("Cannot construct property");
        //create property
        //push new instance
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        int multinameIndex = ins.operands[0];
        int argCount = ins.operands[1];
        List<GraphTargetItem> args = new ArrayList<GraphTargetItem>();
        for (int a = 0; a < argCount; a++) {
            args.add(0, (GraphTargetItem) stack.pop());
        }
        FullMultinameTreeItem multiname = resolveMultiname(stack, constants, multinameIndex, ins);
        GraphTargetItem obj = (GraphTargetItem) stack.pop();

        if (multiname.isXML(constants, localRegNames, fullyQualifiedNames)) {
            if (args.size() == 1) {
                GraphTargetItem arg = args.get(0);
                List<GraphTargetItem> xmlLines = new ArrayList<GraphTargetItem>();
                if (ConstructIns.walkXML(arg, xmlLines)) {
                    stack.push(new XMLTreeItem(ins, xmlLines));
                    return;
                }
            }
        }

        stack.push(new ConstructPropTreeItem(ins, obj, multiname, args));
    }

    @Override
    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        int ret = -ins.operands[1] - 1 + 1;
        int multinameIndex = ins.operands[0];
        if (abc.constants.constant_multiname[multinameIndex].needsName()) {
            ret--;
        }
        if (abc.constants.constant_multiname[multinameIndex].needsNs()) {
            ret--;
        }
        return ret;
    }
}
