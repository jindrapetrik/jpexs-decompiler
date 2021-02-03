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

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        AVM2Code code = body.getCode();
        removeZeroJumps(code, body);
        removeNullPushes(code, body);
    }
}
