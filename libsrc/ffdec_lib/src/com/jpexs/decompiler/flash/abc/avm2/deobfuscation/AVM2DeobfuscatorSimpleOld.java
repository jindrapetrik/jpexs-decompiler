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
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.FixItemCounterTranslateStack;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitNotIns;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewArrayIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewObjectIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallIns;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.TypeOfIns;
import com.jpexs.decompiler.flash.abc.avm2.model.FloatValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewFunctionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorSimpleOld extends SWFDecompilerAdapter {

    private static final UndefinedAVM2Item UNDEFINED_ITEM = new UndefinedAVM2Item(null, null);

    private static final NotCompileTimeItem NOT_COMPILE_TIME_UNDEFINED_ITEM = new NotCompileTimeItem(null, null, UNDEFINED_ITEM);

    private final int executionLimit = 30000;

    protected AVM2Instruction makePush(AVM2ConstantPool cpool, GraphTargetItem graphTargetItem) {
        if (graphTargetItem instanceof IntegerValueAVM2Item) {
            IntegerValueAVM2Item iv = (IntegerValueAVM2Item) graphTargetItem;
            return cpool.makePush(iv.value);
        } else if (graphTargetItem instanceof FloatValueAVM2Item) {
            FloatValueAVM2Item fv = (FloatValueAVM2Item) graphTargetItem;
            return cpool.makePush(fv.value);
        } else if (graphTargetItem instanceof StringAVM2Item) {
            StringAVM2Item fv = (StringAVM2Item) graphTargetItem;
            return cpool.makePush(fv.getValue());
        } else if (graphTargetItem instanceof TrueItem) {
            return cpool.makePush(Boolean.TRUE);
        } else if (graphTargetItem instanceof FalseItem) {
            return cpool.makePush(Boolean.FALSE);
        } else if (graphTargetItem instanceof NullAVM2Item) {
            return cpool.makePush(Null.INSTANCE);
        } else if (graphTargetItem instanceof UndefinedAVM2Item) {
            return cpool.makePush(Undefined.INSTANCE);
        }

        return null;
    }

    protected boolean removeObfuscationIfs(int classIndex, boolean isStatic, int scriptIndex, ABC abc, MethodBody body, List<AVM2Instruction> inlineIns) throws InterruptedException {
        AVM2Code code = body.getCode();
        if (code.code.isEmpty()) {
            return false;
        }

        Map<Integer, GraphTargetItem> staticRegs = new HashMap<>();
        for (AVM2Instruction ins : inlineIns) {
            if (ins.definition instanceof GetLocalTypeIns) {
                staticRegs.put(((GetLocalTypeIns) ins.definition).getRegisterId(ins), new UndefinedAVM2Item(ins, null));
            }
        }

        if (code.code.isEmpty()) {
            return false;
        }

        // find jump targets
        List<Integer> jumpTargets = new ArrayList<>();
        for (int i = 0; i < code.code.size(); i++) {
            AVM2Instruction ins = code.code.get(i);
            if (ins.definition instanceof JumpIns) {
                long address = ins.getTargetAddress();
                jumpTargets.add(code.adr2pos(address));
            }
        }

        AVM2LocalData localData = newLocalData(scriptIndex, abc, abc.constants, body, isStatic, classIndex);
        int localReservedCount = body.getLocalReservedCount();
        for (int i = 0; i < code.code.size(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            localData.scopeStack.clear();
            localData.localRegs.clear();
            localData.localRegAssignmentIps.clear();
            localData.localRegs.clear();
            initLocalRegs(localData, localReservedCount, body.max_regs);

            executeInstructions(staticRegs, body, abc, code, localData, i, code.code.size() - 1, null, inlineIns, jumpTargets);
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

    protected AVM2LocalData newLocalData(int scriptIndex, ABC abc, AVM2ConstantPool cpool, MethodBody body, boolean isStatic, int classIndex) {
        AVM2LocalData localData = new AVM2LocalData();
        localData.isStatic = isStatic;
        localData.classIndex = classIndex;
        localData.localRegs = new HashMap<>(body.max_regs);
        localData.localRegAssignmentIps = new HashMap<>();
        localData.scopeStack = new ScopeStack(true);
        localData.methodBody = body;
        localData.abc = abc;
        localData.localRegNames = new HashMap<>();
        localData.scriptIndex = scriptIndex;
        localData.ip = 0;
        localData.code = body.getCode();
        return localData;
    }

    protected void initLocalRegs(AVM2LocalData localData, int localReservedCount, int maxRegs) {
        for (int i = 0; i < localReservedCount; i++) {
            localData.localRegs.put(i, NOT_COMPILE_TIME_UNDEFINED_ITEM);
        }
        for (int i = localReservedCount; i < maxRegs; i++) {
            localData.localRegs.put(i, UNDEFINED_ITEM);
        }
    }

    private void executeInstructions(Map<Integer, GraphTargetItem> staticRegs, MethodBody body, ABC abc, AVM2Code code, AVM2LocalData localData, int idx, int endIdx, ExecutionResult result, List<AVM2Instruction> inlineIns, List<Integer> jumpTargets) throws InterruptedException {
        List<GraphTargetItem> output = new ArrayList<>();

        FixItemCounterTranslateStack stack = new FixItemCounterTranslateStack("");
        int instructionsProcessed = 0;

        while (true) {
            if (idx > endIdx) {
                break;
            }

            if (instructionsProcessed > executionLimit) {
                break;
            }

            AVM2Instruction ins = code.code.get(idx);
            InstructionDefinition def = ins.definition;
            //System.err.println("" + ins + " stack size:" + stack.size());
            /*if (ins.definition instanceof NewFunctionIns) {
             if (idx + 1 < code.code.size()) {
             if (code.code.get(idx + 1).definition instanceof PopIns) {
             code.removeInstruction(idx + 1, body);
             code.removeInstruction(idx, body);
             continue;
             }
             }
             } else */
            {
                // do not throw EmptyStackException, much faster
                int requiredStackSize = ins.getStackPopCount(localData);
                if (stack.size() < requiredStackSize) {
                    return;
                }

                //Do not duplicate, whole tree, simplify first.  (Simplify always?)
                if (def instanceof DupIns) {
                    stack.simplify();
                }

                if (def instanceof PopIns) {
                    if (!stack.isEmpty()) {
                        //System.err.println("pop:" + stack.peek().getClass());
                        if (stack.peek() instanceof NewFunctionAVM2Item) {
                            AVM2Instruction fins = ((AVM2Instruction) stack.peek().getSrc());
                            AVM2Instruction nins = idx + 1 < code.code.size() ? code.code.get(idx + 1) : null;
                            if (fins.definition instanceof NewFunctionIns) {
                                int fidx = code.code.indexOf(fins);
                                code.removeInstruction(fidx, body);
                            }
                            int nidx = code.code.indexOf(ins);
                            code.removeInstruction(nidx, body);
                            if (nins == null) {
                                idx = code.code.size();
                            } else {
                                idx = code.code.indexOf(nins);
                            }
                            continue;
                        }
                    }
                }

                ins.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");
            }

            if (inlineIns.contains(ins)) {
                if (def instanceof SetLocalTypeIns) {
                    InstructionDefinition prevDef = code.code.get(idx - 1).definition;
                    if ((prevDef instanceof DupIns && !jumpTargets.contains(idx - 2)) || !jumpTargets.contains(idx - 1)) {
                        int regId = ((SetLocalTypeIns) def).getRegisterId(ins);
                        staticRegs.put(regId, localData.localRegs.get(regId).getNotCoerced());
                        code.replaceInstruction(idx, new AVM2Instruction(0, DeobfuscatePopIns.getInstance(), null), body);
                    }
                }
            }
            if (def instanceof GetLocalTypeIns) {
                int regId = ((GetLocalTypeIns) def).getRegisterId(ins);
                if (staticRegs.containsKey(regId)) {
                    if (stack.isEmpty()) {
                        return;
                    }

                    stack.pop();
                    AVM2Instruction pushins = makePush(abc.constants, staticRegs.get(regId));
                    if (pushins == null) {
                        return;
                    }

                    code.replaceInstruction(idx, pushins, body);
                    stack.push(staticRegs.get(regId));
                    ins = pushins;
                    def = ins.definition;
                }
            }

            boolean ok = false;
            // todo: honfika: order by statistics
            if (def instanceof PushByteIns
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
                    || def instanceof BitNotIns
                    || def instanceof StrictEqualsIns
                    || def instanceof PopIns
                    || def instanceof GetLocalTypeIns
                    || def instanceof NewFunctionIns
                    || def instanceof NewArrayIns
                    || def instanceof NewObjectIns
                    || def instanceof GetPropertyIns
                    || def instanceof CoerceOrConvertTypeIns
                    || def instanceof ConstructIns
                    || def instanceof CallIns
                    || def instanceof TypeOfIns) {
                ok = true;
            }

            if (def instanceof GetPropertyIns) {
                GetPropertyAVM2Item avi = (GetPropertyAVM2Item) stack.peek();
                ok = false;
                if (avi.object instanceof NewArrayAVM2Item) {
                    if (((NewArrayAVM2Item) avi.object).values.isEmpty()) {
                        stack.pop();
                        stack.push(new UndefinedAVM2Item(null, null));
                        ok = true;
                    }
                }
            }

            if (!ok) {
                break;
            }

            if (def instanceof GetLocalTypeIns) {
                int regId = ((GetLocalTypeIns) def).getRegisterId(ins);
                if (staticRegs.containsKey(regId)) {
                    if (stack.isEmpty()) {
                        return;
                    }

                    stack.pop();
                    stack.push(staticRegs.get(regId));
                } else if (regId > 0) {
                    break;
                }
            }
            boolean ifed = false;
            if (def instanceof JumpIns) {
                long address = ins.getTargetAddress();
                idx = code.adr2pos(address);
                if (idx == -1) {
                    throw new TranslateException("Jump target not found: " + address);
                }
            } else if (def instanceof IfTypeIns) {
                if (stack.isEmpty()) {
                    return;
                }

                GraphTargetItem top = stack.pop();
                Boolean res = top.getResultAsBoolean();
                long address = ins.getTargetAddress();
                int nidx = code.adr2pos(address);//code.indexOf(code.getByAddress(address));
                AVM2Instruction tarIns = code.code.get(nidx);

                //Some IfType instructions need more than 1 operand, we must pop out all of them
                int stackCount = -def.getStackDelta(ins, abc);
                if (res) {
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
                ifed = true;
                //break;
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
                //break;
            }
        }
    }

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        AVM2Code code = body.getCode();
        code.removeDeadCode(body);
        removeObfuscationIfs(classIndex, isStatic, scriptIndex, abc, body, new ArrayList<>());
        removeZeroJumps(code, body);
        removeNullPushes(code, body);
    }

    class ExecutionResult {

        public int idx = -1;

        public int instructionsProcessed = -1;

        public TranslateStack stack = new TranslateStack("?");
    }
}
