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
package com.jpexs.decompiler.flash.abc.avm2.instructions.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.HashMap;
import java.util.List;

public class ConvertDIns extends InstructionDefinition implements CoerceOrConvertTypeIns {

    public ConvertDIns() {
        super(0x75, "convert_d", new int[]{});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List<Object> arguments) {
        Object value = lda.operandStack.pop();
        double ret;
        if (value == null) {
            ret = 0;
        } else if (value instanceof Boolean) {
            if (((Boolean) value).booleanValue()) {
                ret = 1;
            } else {
                ret = 0;
            }
        } else if (value instanceof Long) {
            ret = ((Long) value).longValue();
        } else if (value instanceof Double) {
            ret = ((Double) value).doubleValue();
        } else if (value instanceof String) {
            ret = Double.parseDouble((String) value);
        } else {
            ret = 1; //must call toPrimitive
        }
        lda.operandStack.push(Double.valueOf(ret));
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, ConstantPool constants, AVM2Instruction ins, List<MethodInfo> method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        stack.push(new ConvertAVM2Item(ins, stack.pop(), getTargetType(constants, ins, fullyQualifiedNames)));
    }

    @Override
    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        return -1 + 1;
    }

    @Override
    public GraphTargetItem getTargetType(ConstantPool constants, AVM2Instruction ins, List<String> fullyQualifiedNames) {
        return new TypeItem("Number");
    }
}
