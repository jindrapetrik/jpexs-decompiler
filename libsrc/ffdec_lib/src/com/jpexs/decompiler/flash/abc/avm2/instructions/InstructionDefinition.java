/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2VerifyErrorException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public abstract class InstructionDefinition implements Serializable {

    public static final long serialVersionUID = 1L;

    public int[] operands;

    public String instructionName = "";

    public int instructionCode = 0;

    public boolean canThrow;

    public AVM2InstructionFlag[] flags;

    public InstructionDefinition(int instructionCode, String instructionName, int[] operands, boolean canThrow, AVM2InstructionFlag... flags) {
        this.instructionCode = instructionCode;
        this.instructionName = instructionName;
        this.operands = operands;
        this.canThrow = canThrow;
        this.flags = flags;
    }

    public boolean hasFlag(AVM2InstructionFlag flag) {
        for (AVM2InstructionFlag f : flags) {
            if (f == flag) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(instructionName);
        for (int i = 0; i < operands.length; i++) {
            s.append(AVM2Code.operandTypeToString(operands[i], true));
        }
        return s.toString();
    }

    public void verify(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2VerifyErrorException {
        for (int i = 0; i < operands.length; i++) {
            int operand = operands[i];
            if (operand == AVM2Code.DAT_MULTINAME_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getMultinameCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getMultinameCount()});
                }
            } else if (operand == AVM2Code.DAT_DOUBLE_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getDoubleCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getDoubleCount()});
                }
            } else if (operand == AVM2Code.DAT_INT_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getIntCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getIntCount()});
                }
            } else if (operand == AVM2Code.DAT_UINT_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getUIntCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getUIntCount()});
                }
            } else if (operand == AVM2Code.DAT_STRING_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getStringCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            } else if (operand == AVM2Code.DAT_NAMESPACE_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getNamespaceCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            } else if (operand == AVM2Code.DAT_FLOAT_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getFloatCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            } else if (operand == AVM2Code.DAT_FLOAT4_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getFloat4Count()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            } else if (operand == AVM2Code.DAT_DECIMAL_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getDecimalCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            }
        }
    }

    public boolean isNotCompileTimeSupported() {
        return false;
    }

    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2ExecutionException {
        //throw new UnsupportedOperationException("Instruction " + instructionName + " not implemented");
        return false;
    }

    protected void illegalOpCode(LocalDataArea lda, AVM2Instruction ins) throws AVM2VerifyErrorException {
        throw new AVM2VerifyErrorException(AVM2VerifyErrorException.ILLEGAL_OPCODE, lda.isDebug(), new Object[]{lda.methodName, instructionCode, ins.getAddress()});
    }

    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) throws InterruptedException {
    }

    public void translate(Reference<GraphSourceItem> lineStartItem, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, AVM2Instruction ins, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code, boolean thisHasDefaultToPrimitive) throws InterruptedException {
        AVM2LocalData localData = new AVM2LocalData();
        localData.isStatic = isStatic;
        localData.scriptIndex = scriptIndex;
        localData.classIndex = classIndex;
        localData.lineStartInstruction = lineStartItem.getVal();
        localData.localRegs = localRegs;
        localData.scopeStack = scopeStack;
        localData.methodBody = body;
        localData.abc = abc;
        localData.localRegNames = localRegNames;
        localData.fullyQualifiedNames = fullyQualifiedNames;
        localData.localRegAssignmentIps = localRegsAssignmentIps;
        localData.ip = ip;
        localData.refs = refs;
        localData.code = code;
        localData.thisHasDefaultToPrimitive = thisHasDefaultToPrimitive;
        translate(localData, stack, ins, output, path);
        lineStartItem.setVal(localData.lineStartInstruction);
    }

    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 0;
    }

    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 0;
    }

    protected void resolveMultiname(LocalDataArea localData, AVM2ConstantPool constants, int multinameIndex) {
        if (multinameIndex > 0 && multinameIndex < constants.getMultinameCount()) {
            Multiname multiname = constants.getMultiname(multinameIndex);
            if (multiname.needsName()) {
                Object name = localData.operandStack.pop();
            }
            if (multiname.needsNs()) {
                Object ns = localData.operandStack.pop();
            }
        }
    }

    protected FullMultinameAVM2Item resolveMultiname(AVM2LocalData localData, boolean property, TranslateStack stack, AVM2ConstantPool constants, int multinameIndex, AVM2Instruction ins) {
        GraphTargetItem ns = null;
        GraphTargetItem name = null;
        if (multinameIndex > 0 && multinameIndex < constants.getMultinameCount()) {
            Multiname multiname = constants.getMultiname(multinameIndex);
            if (multiname.needsName()) {
                name = stack.pop();
            }
            if (multiname.needsNs()) {
                ns = stack.pop();
            }
        }

        return new FullMultinameAVM2Item(property, ins, localData.lineStartInstruction, multinameIndex, localData.abc.constants.getMultiname(multinameIndex).getName(localData.getConstants(), new ArrayList<>(), true, true), name, ns);
    }

    protected int getMultinameRequiredStackSize(AVM2ConstantPool constants, int multinameIndex) {
        int res = 0;
        if (multinameIndex > 0 && multinameIndex < constants.getMultinameCount()) {
            //Note: In official compiler, the stack can be wrong(greater) for some MULTINAMEL/A, e.g. increments
            /*
             var arr=[1,2,3];
             return arr[2]++;
             */
            if (constants.getMultiname(multinameIndex).needsName()) {
                res++;
            }
            if (constants.getMultiname(multinameIndex).needsNs()) {
                res++;
            }
        }

        return res;
    }

    protected int resolvedCount(AVM2ConstantPool constants, int multinameIndex) {
        int pos = 0;
        if (constants.getMultiname(multinameIndex).needsNs()) {
            pos++;
        }
        if (constants.getMultiname(multinameIndex).needsName()) {
            pos++;
        }
        return pos;

    }

    protected String resolveMultinameNoPop(int pos, Stack<AVM2Item> stack, AVM2ConstantPool constants, int multinameIndex, AVM2Instruction ins, List<DottedChain> fullyQualifiedNames) {
        String ns = "";
        String name;
        if (constants.getMultiname(multinameIndex).needsNs()) {
            ns = "[" + stack.get(pos) + "]";
            pos++;
        }
        if (constants.getMultiname(multinameIndex).needsName()) {
            name = stack.get(pos).toString();
        } else {
            name = GraphTextWriter.hilighOffset(constants.getMultiname(multinameIndex).getName(constants, fullyQualifiedNames, false, true), ins.getAddress());
        }
        return name + ns;
    }

    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        return getStackPushCount(ins, abc) - getStackPopCount(ins, abc);
    }

    public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
        return 0;
    }

    protected boolean isRegisterCompileTime(int regId, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        Set<Integer> previous = new HashSet<>();
        AVM2Code.getPreviousReachableIps(ip, refs, previous, new HashSet<>());
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

    public boolean isExitInstruction() {
        return false;
    }
}
