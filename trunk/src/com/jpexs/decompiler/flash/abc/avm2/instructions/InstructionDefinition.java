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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class InstructionDefinition implements Serializable {

    public static final long serialVersionUID = 1L;

    public int[] operands;
    public String instructionName = "";
    public int instructionCode = 0;

    public InstructionDefinition(int instructionCode, String instructionName, int[] operands) {
        this.instructionCode = instructionCode;
        this.instructionName = instructionName;
        this.operands = operands;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(instructionName);
        for (int i = 0; i < operands.length; i++) {
            if ((operands[i] & 0xff00) == AVM2Code.OPT_U30) {
                s.append(" U30");
            }
            if ((operands[i] & 0xff00) == AVM2Code.OPT_U8) {
                s.append(" U8");
            }
            if ((operands[i] & 0xff00) == AVM2Code.OPT_BYTE) {
                s.append(" BYTE");
            }
            if ((operands[i] & 0xff00) == AVM2Code.OPT_S24) {
                s.append(" S24");
            }
            if ((operands[i] & 0xff00) == AVM2Code.OPT_CASE_OFFSETS) {
                s.append(" U30 S24,[S24]...");
            }
        }
        return s.toString();
    }

    public void execute(LocalDataArea lda, ConstantPool constants, List<Object> arguments) {
        throw new UnsupportedOperationException("Instruction " + instructionName + " not implemented");
    }

    public void translate(boolean isStatic, int scriptIndex, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
    }

    protected FullMultinameAVM2Item resolveMultiname(Stack<GraphTargetItem> stack, ConstantPool constants, int multinameIndex, AVM2Instruction ins) {
        GraphTargetItem ns = null;
        GraphTargetItem name = null;
        if (constants.constant_multiname[multinameIndex].needsName()) {
            name = (GraphTargetItem) stack.pop();
        }
        if (constants.constant_multiname[multinameIndex].needsNs()) {
            ns = (GraphTargetItem) stack.pop();
        }
        return new FullMultinameAVM2Item(ins, multinameIndex, name, ns);
    }

    protected int resolvedCount(ConstantPool constants, int multinameIndex) {
        int pos = 0;
        if (constants.constant_multiname[multinameIndex].needsNs()) {
            pos++;
        }
        if (constants.constant_multiname[multinameIndex].needsName()) {
            pos++;
        }
        return pos;

    }

    protected String resolveMultinameNoPop(int pos, Stack<AVM2Item> stack, ConstantPool constants, int multinameIndex, AVM2Instruction ins, List<String> fullyQualifiedNames) {
        String ns = "";
        String name;
        if (constants.constant_multiname[multinameIndex].needsNs()) {
            ns = "[" + stack.get(pos) + "]";
            pos++;
        }
        if (constants.constant_multiname[multinameIndex].needsName()) {
            name = stack.get(pos).toString();
        } else {
            name = GraphTextWriter.hilighOffset(constants.constant_multiname[multinameIndex].getName(constants, fullyQualifiedNames), ins.offset);
        }
        return name + ns;
    }

    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        return 0;
    }

    public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
        return 0;
    }

    protected boolean isRegisterCompileTime(int regId, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        Set<Integer> previous = new HashSet<>();
        AVM2Code.getPreviousReachableIps(ip, refs, previous, new HashSet<Integer>());
        for (int p : previous) {
            if (p < 0) {
                continue;
            }
            if (p >= code.code.size()) {
                continue;
            }
            AVM2Instruction sins = code.code.get(p);
            if (code.code.get(p).definition instanceof SetLocalTypeIns) {
                SetLocalTypeIns sl = (SetLocalTypeIns) sins.definition;
                if (sl.getRegisterId(sins) == regId) {
                    if (!AVM2Code.isDirectAncestor(ip, p, refs)) {
                        return false;
                    }
                }
            }
            if ((code.code.get(p).definition instanceof IncLocalIns)
                    || (code.code.get(p).definition instanceof IncLocalIIns)
                    || (code.code.get(p).definition instanceof DecLocalIns)
                    || (code.code.get(p).definition instanceof DecLocalIIns)) {
                if (sins.operands[0] == regId) {
                    if (!AVM2Code.isDirectAncestor(ip, p, refs)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
