/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWF;
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
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * AVM2 Deobfuscator removing single get / set registers
 *
 * Example: getlocal_1, getlocal_2, (kill 1), (kill 2), setlocal_2, setlocal_1
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorGetSet implements SWFDecompilerListener {

    private static final UndefinedAVM2Item UNDEFINED_ITEM = new UndefinedAVM2Item(null, null);

    private static final NotCompileTimeItem NOT_COMPILE_TIME_UNDEFINED_ITEM = new NotCompileTimeItem(null, null, UNDEFINED_ITEM);

    private final int executionLimit = 30000;

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {

    }

    protected AVM2Instruction makePush(Object ovalue, AVM2ConstantPool cpool) {
        if (ovalue instanceof Long) {
            long value = (Long) ovalue;
            if (value >= -128 && value <= 127) {
                return new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{(int) (long) value});
            } else if (value >= -32768 && value <= 32767) {
                return new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{((int) (long) value) & 0xffff});
            } else {
                return new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{cpool.getIntId(value, true)});
            }
        }
        if (ovalue instanceof Double) {
            return new AVM2Instruction(0, AVM2Instructions.PushDouble, new int[]{cpool.getDoubleId((Double) ovalue, true)});
        }
        if (ovalue instanceof String) {
            return new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{cpool.getStringId((String) ovalue, true)});
        }
        if (ovalue instanceof Boolean) {
            if ((Boolean) ovalue) {
                return new AVM2Instruction(0, AVM2Instructions.PushTrue, null);
            }
            return new AVM2Instruction(0, AVM2Instructions.PushFalse, null);
        }
        if (ovalue == Null.INSTANCE) {
            return new AVM2Instruction(0, AVM2Instructions.PushNull, null);
        }
        if (ovalue == Undefined.INSTANCE) {
            return new AVM2Instruction(0, AVM2Instructions.PushUndefined, null);
        }
        return null;
    }

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
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            localData.scopeStack.clear();
            localData.localRegs.clear();
            localData.localRegAssignmentIps.clear();
            localData.localRegs.clear();
            initLocalRegs(localData, localReservedCount, body.max_regs);

            executeInstructions(body, code, localData, i, code.code.size() - 1);
        }

        return false;
    }

    protected void removeUnreachableInstructions(AVM2Code code, MethodBody body) throws InterruptedException {
        code.removeDeadCode(body);
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
                    code.replaceInstruction(idx, new AVM2Instruction(ins.offset, DeobfuscatePopIns.getInstance(), null), body);
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

                ins.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");
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
                long address = ins.offset + ins.getBytesLength() + ins.operands[0];
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

    @Override
    public void actionTreeCreated(List<GraphTargetItem> tree, SWF swf) {
    }

    @Override
    public byte[] proxyFileCatched(byte[] data) {
        return null;
    }

    @Override
    public void swfParsed(SWF swf) {
    }

    @Override
    public void abcParsed(ABC abc, SWF swf) {
    }

    @Override
    public void methodBodyParsed(MethodBody body, SWF swf) {

    }

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        AVM2Code code = body.getCode();
        removeUnreachableInstructions(code, body);
        removeObfuscationGetSets(classIndex, isStatic, scriptIndex, abc, body, new ArrayList<>());
    }
}
