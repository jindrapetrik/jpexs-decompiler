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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.deobfuscation;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.AddIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.AddIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DivideIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.ModuloIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.MultiplyIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.MultiplyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NegateIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NegateIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.SubtractIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.SubtractIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitAndIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitOrIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitXorIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.LShiftIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.RShiftIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.URShiftIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.EqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.GreaterEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.GreaterThanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.LessEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.LessThanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.StrictEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewArrayIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewObjectIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushDoubleIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushFalseIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushIntegerTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushNanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushNullIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushShortIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushStringIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushTrueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushUIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushUndefinedIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.SwapIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceOrConvertTypeIns;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.ecma.NotCompileTime;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import com.jpexs.decompiler.flash.helpers.collections.FixItemCounterStack;
import com.jpexs.decompiler.graph.TranslateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorSimple extends SWFDecompilerAdapter {

    private final int executionLimit = 30000;

    protected boolean removeObfuscationIfs(int classIndex, boolean isStatic, int scriptIndex, ABC abc, MethodBody body, AVM2Instruction inlineIns) throws InterruptedException {
        AVM2Code code = body.getCode();
        if (code.code.isEmpty()) {
            return false;
        }

        Map<Integer, Object> staticRegs = new HashMap<>();
        if (inlineIns != null && inlineIns.definition instanceof GetLocalTypeIns) {
            staticRegs.put(((GetLocalTypeIns) inlineIns.definition).getRegisterId(inlineIns), Undefined.INSTANCE);
        }

        if (code.code.isEmpty()) {
            return false;
        }

        FixItemCounterStack stack = new FixItemCounterStack();
        LocalDataArea localData = new LocalDataArea();
        localData.operandStack = stack;

        int localReservedCount = body.getLocalReservedCount();
        for (int i = 0; i < code.code.size(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            localData.clear();
            initLocalRegs(localData, localReservedCount, body.max_regs, i == 0);

            if (executeInstructions(staticRegs, body, abc, code, localData, i, code.code.size() - 1, null, inlineIns)) {
                code.removeDeadCode(body);
                i--;
            }
        }

        return false;
    }

    protected boolean removeZeroJumps(AVM2Code code, MethodBody body) throws InterruptedException {
        boolean result = false;
        for (int i = 0; i < code.code.size(); i++) {
            AVM2Instruction ins = code.code.get(i);
            if (ins.definition instanceof JumpIns) {
                if (ins.operands[0] == 0) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    code.removeInstruction(i, body);
                    i--;
                    result = true;
                }
            }
        }
        return result;
    }

    protected boolean removeNullPushes(AVM2Code code, MethodBody body) throws InterruptedException {
        boolean result = false;
        Set<Long> offsets = code.getImportantOffsets(body, true);

        // Deliberately skip over instruction zero
        for (int i = 1; i < code.code.size(); i++) {
            AVM2Instruction ins1 = code.code.get(i - 1);
            AVM2Instruction ins2 = code.code.get(i);
            if (ins2.definition instanceof PopIns
                    && !offsets.contains(ins2.getAddress())
                    && (ins1.definition instanceof PushByteIns
                    || ins1.definition instanceof PushDoubleIns
                    || ins1.definition instanceof PushFalseIns
                    || ins1.definition instanceof PushIntIns
                    || ins1.definition instanceof PushNanIns
                    || ins1.definition instanceof PushNullIns
                    || ins1.definition instanceof PushShortIns
                    || ins1.definition instanceof PushStringIns
                    || ins1.definition instanceof PushTrueIns
                    || ins1.definition instanceof PushUIntIns
                    || ins1.definition instanceof PushUndefinedIns)) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }

                code.removeInstruction(i - 1, body);
                i--;
                code.removeInstruction(i, body);
                i--;
                offsets = code.getImportantOffsets(body, true); //update offsets, they changed because of removing instruction
                result = true;
            }
        }
        return result;
    }

    protected void initLocalRegs(LocalDataArea localData, int localReservedCount, int maxRegs, boolean executeFromFirst) {
        for (int i = 0; i < localReservedCount; i++) {
            localData.localRegisters.put(i, NotCompileTime.INSTANCE);
        }
        for (int i = localReservedCount; i < maxRegs; i++) {
            localData.localRegisters.put(i, executeFromFirst ? Undefined.INSTANCE : NotCompileTime.INSTANCE);
        }
    }

    private boolean executeInstructions(Map<Integer, Object> staticRegs, MethodBody body, ABC abc, AVM2Code code, LocalDataArea localData, int idx, int endIdx, ExecutionResult result, AVM2Instruction inlineIns) throws InterruptedException {
        int instructionsProcessed = 0;

        FixItemCounterStack stack = (FixItemCounterStack) localData.operandStack;
        Set<Long> refs = code.getImportantOffsets(body, false);
        boolean modified = false;
        while (true) {
            if (idx > endIdx) {
                break;
            }

            if (instructionsProcessed > executionLimit) {
                break;
            }

            AVM2Instruction ins = code.code.get(idx);
            if (instructionsProcessed > 0 && refs.contains(ins.getAddress())) {
                break;
            }

            modified = modified | code.inlineJumpExit();
            InstructionDefinition def = ins.definition;
            if (inlineIns == ins) {
                if (def instanceof SetLocalTypeIns) {
                    int regId = ((SetLocalTypeIns) def).getRegisterId(ins);
                    staticRegs.put(regId, localData.localRegisters.get(regId));
                    code.replaceInstruction(idx, new AVM2Instruction(0, DeobfuscatePopIns.getInstance(), null), body);
                    modified = true;
                }
            }
            if (def instanceof GetLocalTypeIns) {
                int regId = ((GetLocalTypeIns) def).getRegisterId(ins);
                if (staticRegs.containsKey(regId)) {
                    AVM2Instruction pushins = abc.constants.makePush(staticRegs.get(regId));
                    if (pushins == null) {
                        break;
                    }

                    code.replaceInstruction(idx, pushins, body);
                    modified = true;
                    ins = pushins;
                    def = ins.definition;
                }
            }

            if (def instanceof NewFunctionIns
                    && idx + 1 < code.code.size()
                    && code.code.get(idx + 1).definition instanceof PopIns) {
                code.removeInstruction(idx + 1, body);
                code.removeInstruction(idx, body);
                modified = true;
                continue;
            }

            boolean ok = false;
            // todo: honfika: order by statistics
            if (def.isNotCompileTimeSupported() || def instanceof PushByteIns
                    || def instanceof PushShortIns
                    || def instanceof PushIntIns
                    || def instanceof PushDoubleIns
                    || def instanceof PushStringIns
                    || def instanceof PushNullIns
                    || def instanceof PushUndefinedIns
                    || def instanceof PushFalseIns
                    || def instanceof PushTrueIns
                    || def instanceof DupIns
                    || def instanceof SwapIns
                    || def instanceof AddIns
                    || def instanceof AddIIns
                    || def instanceof SubtractIns
                    || def instanceof SubtractIIns
                    || def instanceof ModuloIns
                    || def instanceof MultiplyIns
                    || def instanceof MultiplyIIns//
                    || def instanceof DivideIns//
                    || def instanceof BitAndIns
                    || def instanceof BitXorIns
                    || def instanceof BitOrIns
                    || def instanceof LShiftIns
                    || def instanceof RShiftIns
                    || def instanceof URShiftIns
                    || def instanceof EqualsIns
                    || def instanceof NotIns
                    || def instanceof NegateIns//
                    || def instanceof NegateIIns//
                    || def instanceof IncrementIns//
                    || def instanceof IncrementIIns//
                    || def instanceof DecrementIns//
                    || def instanceof DecrementIIns //
                    || def instanceof IfTypeIns
                    || def instanceof JumpIns
                    || def instanceof EqualsIns
                    || def instanceof LessEqualsIns
                    || def instanceof GreaterEqualsIns
                    || def instanceof GreaterThanIns
                    || def instanceof LessThanIns
                    || def instanceof StrictEqualsIns
                    || def instanceof PopIns
                    || def instanceof GetLocalTypeIns
                    || def instanceof SetLocalTypeIns
                    || def instanceof NewFunctionIns
                    || def instanceof NewArrayIns
                    || def instanceof NewObjectIns
                    || def instanceof GetPropertyIns
                    || def instanceof CoerceOrConvertTypeIns) {
                ok = true;
            }

            if (!ok) {
                break;
            }

            if (!(def instanceof NewFunctionIns)) {
                // do not throw EmptyStackException, much faster
                int requiredStackSize = def.getStackPopCount(ins, abc);
                if (stack.size() < requiredStackSize) {
                    break;
                }

                if (requiredStackSize > 0 && !def.isNotCompileTimeSupported()) {
                    boolean notCompileTime = false;
                    for (int i = 0; i < requiredStackSize; i++) {
                        if (stack.peek(i + 1) == NotCompileTime.INSTANCE) {
                            notCompileTime = true;
                            break;
                        }
                    }

                    if (notCompileTime) {
                        break;
                    }
                }

                if (localData.scopeStack.size() < -def.getScopeStackDelta(ins, abc)) {
                    break;
                }

                boolean supported;
                try {
                    localData.jump = null;
                    supported = def.execute(localData, abc.constants, ins);
                } catch (AVM2ExecutionException ex) {
                    supported = false;
                }

                if (!supported) {
                    break;
                }
            }

            boolean ifed = false;
            if (def instanceof IfTypeIns && !(def instanceof JumpIns)) {
                long address = ins.getTargetAddress();
                int nidx = code.adr2pos(address);
                AVM2Instruction tarIns = code.code.get(nidx);

                //Some IfType instructions need more than 1 operand, we must pop out all of them
                int stackCount = -def.getStackDelta(ins, abc);

                if (localData.jump != null) {
                    //System.err.println("replacing " + ins + " on " + idx + " with jump");
                    AVM2Instruction jumpIns = new AVM2Instruction(0, AVM2Instructions.Jump, new int[]{0});
                    //jumpIns.operands[0] = ins.operands[0] /*- ins.getBytes().length*/ + jumpIns.getBytes().length;
                    code.replaceInstruction(idx, jumpIns, body);
                    jumpIns.operands[0] = (int) (tarIns.getAddress() - jumpIns.getAddress() - jumpIns.getBytesLength());
                    for (int s = 0; s < stackCount; s++) {
                        code.insertInstruction(idx, new AVM2Instruction(ins.getAddress(), DeobfuscatePopIns.getInstance(), null), true, body);
                    }

                    idx = code.adr2pos(jumpIns.getTargetAddress());
                } else {
                    //System.err.println("replacing " + ins + " on " + idx + " with pop");
                    code.replaceInstruction(idx, new AVM2Instruction(ins.getAddress(), DeobfuscatePopIns.getInstance(), null), body);
                    for (int s = 1 /*first is replaced*/; s < stackCount; s++) {
                        code.insertInstruction(idx, new AVM2Instruction(ins.getAddress(), DeobfuscatePopIns.getInstance(), null), true, body);
                    }
                    //ins.definition = DeobfuscatePopIns.getInstance();
                    idx++;
                }

                modified = true;
                ifed = true;
            } else {
                idx++;
            }

            instructionsProcessed++;

            if (result != null && stack.allItemsFixed()) {
                result.idx = idx == code.code.size() ? idx - 1 : idx;
                result.instructionsProcessed = instructionsProcessed;
                result.stack.clear();
                result.stack.addAll(stack);
            }

            if (ifed) {
                break;
            }

            if (localData.jump != null) {
                idx = code.adr2pos(localData.jump);

                if (idx == -1) {
                    throw new TranslateException("Jump target not found: " + localData.jump);
                }
            }
        }

        return modified;
    }

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        AVM2Code code = body.getCode();
        code.removeDeadCode(body);
        removeObfuscationIfs(classIndex, isStatic, scriptIndex, abc, body, null);
        removeZeroJumps(code, body);
        removeNullPushes(code, body);
    }

    class ExecutionResult {

        public int idx = -1;

        public int instructionsProcessed = -1;

        public Stack<Object> stack = new Stack<>();
    }
}
