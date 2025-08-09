/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.deobfuscation;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.FixItemCounterTranslateStack;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.KillIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.helpers.CancellableWorker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AVM2 Deobfuscator removing single get / set registers.
 * <p>
 * Example: getlocal_1, getlocal_2, (kill 1), (kill 2), setlocal_2, setlocal_1
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorGetSet extends SWFDecompilerAdapter {

    /**
     * Undefined item
     */
    private static final UndefinedAVM2Item UNDEFINED_ITEM = new UndefinedAVM2Item(null, null);

    /**
     * Not compile time undefined item
     */
    private static final NotCompileTimeItem NOT_COMPILE_TIME_UNDEFINED_ITEM = new NotCompileTimeItem(null, null, UNDEFINED_ITEM);

    /**
     * Execution limit
     */
    private final int executionLimit = 30000;

    /**
     * Constructor.
     */
    public AVM2DeobfuscatorGetSet() {
    }

    /**
     * Remove obfuscation get sets
     *
     * @param classIndex Class index
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param abc ABC
     * @param body Method body
     * @param inlineIns Inline instructions
     * @return True if removed
     * @throws InterruptedException On interrupt
     */
    protected boolean removeObfuscationGetSets(int classIndex, boolean isStatic, int scriptIndex, ABC abc, MethodBody body, List<AVM2Instruction> inlineIns) throws InterruptedException {
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

        AVM2LocalData localData = newLocalData(scriptIndex, abc, abc.constants, body, isStatic, classIndex);
        int localReservedCount = body.getLocalReservedCount();
        for (int i = 0; i < code.code.size(); i++) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }

            localData.scopeStack.clear();
            localData.localScopeStack.clear();
            localData.localRegs.clear();
            localData.localRegAssignmentIps.clear();
            localData.localRegs.clear();
            initLocalRegs(localData, localReservedCount, body.max_regs);

            executeInstructions(body, code, localData, i, code.code.size() - 1);
        }

        return false;
    }

    /**
     * Remove unreachable instructions.
     *
     * @param code AVM2 code
     * @param body Method body
     * @throws InterruptedException On interrupt
     */
    protected void removeUnreachableInstructions(AVM2Code code, MethodBody body) throws InterruptedException {
        code.removeDeadCode(body);
    }

    /**
     * Create new local data.
     *
     * @param scriptIndex Script index
     * @param abc ABC
     * @param cpool Constant pool
     * @param body Method body
     * @param isStatic Is static
     * @param classIndex Class index
     * @return AVM2 local data
     */
    protected AVM2LocalData newLocalData(int scriptIndex, ABC abc, AVM2ConstantPool cpool, MethodBody body, boolean isStatic, int classIndex) {
        AVM2LocalData localData = new AVM2LocalData();
        localData.isStatic = isStatic;
        localData.classIndex = classIndex;
        localData.localRegs = new HashMap<>(body.max_regs);
        localData.localRegAssignmentIps = new HashMap<>();
        localData.scopeStack = new ScopeStack(true);
        localData.localScopeStack = new ScopeStack(true);
        List<MethodBody> callStack = new ArrayList<>();
        callStack.add(body);
        localData.callStack = callStack;
        localData.methodBody = body;
        localData.abc = abc;
        localData.localRegNames = new HashMap<>();
        localData.localRegTypes = new HashMap<>();
        localData.scriptIndex = scriptIndex;
        localData.ip = 0;
        localData.code = body.getCode();      
        return localData;
    }

    /**
     * Initialize local registers.
     *
     * @param localData AVM2 local data
     * @param localReservedCount Local reserved count
     * @param maxRegs Maximum registers
     */
    protected void initLocalRegs(AVM2LocalData localData, int localReservedCount, int maxRegs) {
        for (int i = 0; i < localReservedCount; i++) {
            localData.localRegs.put(i, NOT_COMPILE_TIME_UNDEFINED_ITEM);
        }
        for (int i = localReservedCount; i < maxRegs; i++) {
            localData.localRegs.put(i, UNDEFINED_ITEM);
        }
    }

    /**
     * Execute instructions.
     *
     * @param body Method body
     * @param code AVM2 code
     * @param localData AVM2 local data
     * @param idx Index
     * @param endIdx End index
     * @throws InterruptedException On interrupt
     */
    private void executeInstructions(MethodBody body, AVM2Code code, AVM2LocalData localData, int idx, int endIdx) throws InterruptedException {
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

            if (def instanceof SetLocalTypeIns) {
                int regId = ((SetLocalTypeIns) def).getRegisterId(ins);
                if (!stack.isEmpty() && (stack.peek() instanceof LocalRegAVM2Item) && (((LocalRegAVM2Item) stack.peek()).regIndex == regId)) {
                    stack.pop();
                    code.replaceInstruction(idx, new AVM2Instruction(ins.getAddress(), DeobfuscatePopIns.getInstance(), null), body);
                    idx++;
                    continue;
                }
            }
            if (ins.definition instanceof NewFunctionIns) {
                if (idx + 1 < code.code.size()) {
                    if (code.code.get(idx + 1).definition instanceof PopIns) {
                        code.removeInstruction(idx + 1, body);
                        code.removeInstruction(idx, body);
                        continue;
                    }
                }
            } else {
                // do not throw EmptyStackException, much faster
                int requiredStackSize = ins.getStackPopCount(localData);
                if (stack.size() < requiredStackSize) {
                    return;
                }

                stack.setConnectedOutput(0, output, localData);        
                ins.translate(localData, stack, output, 0, "");
            }

            boolean ok = false;
            if (def instanceof SetLocalTypeIns
                    || def instanceof KillIns
                    || def instanceof GetLocalTypeIns) {
                ok = true;
            }

            if (!ok) {
                break;
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
                ifed = true;
                //break;
            } else {
                idx++;
            }

            instructionsProcessed++;

            if (ifed) {
                break;
            }
        }
    }

    /**
     * AVM2 code remove traps.
     *
     * @param path Path
     * @param classIndex Class index
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param abc ABC
     * @param trait Trait
     * @param methodInfo Method info
     * @param body Method body
     * @throws InterruptedException On interrupt
     */
    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        AVM2Code code = body.getCode();
        removeUnreachableInstructions(code, body);
        removeObfuscationGetSets(classIndex, isStatic, scriptIndex, abc, body, new ArrayList<>());
    }
}
