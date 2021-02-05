/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushDoubleIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushFalseIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushNanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushNullIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushShortIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushStringIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushTrueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushUIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushUndefinedIns;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorZeroJumpsNullPushes extends SWFDecompilerAdapter {

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

    private boolean isSimplePush(InstructionDefinition def) {
        return (def instanceof PushByteIns
                || def instanceof PushDoubleIns
                || def instanceof PushFalseIns
                || def instanceof PushIntIns
                || def instanceof PushNanIns
                || def instanceof PushNullIns
                || def instanceof PushShortIns
                || def instanceof PushStringIns
                || def instanceof PushTrueIns
                || def instanceof PushUIntIns
                || def instanceof PushUndefinedIns);
    }

    protected boolean removeNullPushes(AVM2Code code, MethodBody body) throws InterruptedException {
        boolean result = false;
        Set<Long> offsets = code.getImportantOffsets(body, true);

        for (int i = 0; i < code.code.size(); i++) {
            if (i == 0) {
                // Deliberately skip over instruction zero
                continue;
            }
            AVM2Instruction ins1 = code.code.get(i - 1);
            AVM2Instruction ins2 = code.code.get(i);
            if (ins2.definition instanceof PopIns
                    && !offsets.contains(ins2.getAddress())
                    && isSimplePush(ins1.definition)) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }

                code.removeInstruction(i - 1, body);
                i--;
                code.removeInstruction(i, body);
                i--;
                offsets = code.getImportantOffsets(body, true); //update offsets, they changed because of removing instruction
                result = true;
            } else if (i >= 2) {
                AVM2Instruction ins0 = code.code.get(i - 2);
                if ((ins2.definition instanceof PopIns)
                        && (ins1.definition instanceof NotIns)
                        && !offsets.contains(ins2.getAddress())
                        && !offsets.contains(ins1.getAddress())
                        && isSimplePush(ins0.definition)) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    code.removeInstruction(i - 2, body);
                    i--;
                    code.removeInstruction(i - 1, body);
                    i--;
                    code.removeInstruction(i, body);
                    i--;
                    offsets = code.getImportantOffsets(body, true); //update offsets, they changed because of removing instruction
                    result = true;
                }
            }
        }
        return result;
    }

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        AVM2Code code = body.getCode();
        removeZeroJumps(code, body);
        removeNullPushes(code, body);
    }
}
