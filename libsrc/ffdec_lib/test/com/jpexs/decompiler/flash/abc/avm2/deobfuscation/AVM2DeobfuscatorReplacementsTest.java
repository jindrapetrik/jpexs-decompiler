/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AVM2DeobfuscatorReplacementsTest {
    private MethodBody body() {
        AVM2Code code = new AVM2Code();
        code.code.add(new AVM2Instruction(0, AVM2Instructions.GetLocal, new int[]{128}));
        code.code.add(new AVM2Instruction(3, AVM2Instructions.IfTrue, new int[]{3}));
        code.code.add(new AVM2Instruction(7, AVM2Instructions.GetLocal, new int[]{129}));
        code.code.add(new AVM2Instruction(10, AVM2Instructions.LookupSwitch, new int[]{-10, 1, -3, 11}));
        code.code.add(new AVM2Instruction(21, AVM2Instructions.Jump, new int[]{-25}));
        code.code.add(new AVM2Instruction(25, AVM2Instructions.ReturnVoid, null));
        ABCException exception = new ABCException();
        exception.start = 3;
        exception.end = 26;
        exception.target = 21;
        MethodBody body = new MethodBody(null, new Traits(), new byte[0], new ABCException[]{exception});
        body.setCode(code);
        return body;
    }

    @Test
    public void testMatchesSequentialReplacementAcrossBranchesAndExceptions() {
        MethodBody sequential = body();
        MethodBody batched = body();
        AVM2DeobfuscatorSimpleOld.InstructionReplacements replacements =
                new AVM2DeobfuscatorSimpleOld.InstructionReplacements(batched.getCode(), batched);
        // Out-of-order replacements, including replacing one position twice,
        // shrinking and growing encodings, and targets at replaced positions.
        int[] indices = {2, 0, 2};
        int[] opcodes = {AVM2Instructions.PushUndefined, AVM2Instructions.PushString, AVM2Instructions.PushByte};
        int[][] operands = {null, new int[]{2097152}, new int[]{7}};
        for (int i = 0; i < indices.length; i++) {
            sequential.getCode().replaceInstruction(indices[i], new AVM2Instruction(0, opcodes[i], operands[i]), sequential);
            replacements.put(indices[i], new AVM2Instruction(0, opcodes[i], operands[i]));
        }
        replacements.flush();
        replacements.flush();
        for (int i = 0; i < sequential.getCode().code.size(); i++) {
            AVM2Instruction expected = sequential.getCode().code.get(i);
            AVM2Instruction actual = batched.getCode().code.get(i);
            Assert.assertEquals(actual.getAddress(), expected.getAddress());
            Assert.assertEquals(actual.getBytes(), expected.getBytes());
        }
        Assert.assertEquals(batched.exceptions[0].start, sequential.exceptions[0].start);
        Assert.assertEquals(batched.exceptions[0].end, sequential.exceptions[0].end);
        Assert.assertEquals(batched.exceptions[0].target, sequential.exceptions[0].target);
        batched.getCode().checkValidOffsets(batched);
    }

    @Test
    public void testRepeatedAssignmentsOnlyInStraightLineCode() {
        MethodBody body = new MethodBody(null, new Traits(), new byte[0], new ABCException[0]);
        AVM2Code code = new AVM2Code();
        body.setCode(code);
        code.code.add(new AVM2Instruction(0, AVM2Instructions.SetLocal, new int[]{8}));
        code.code.add(new AVM2Instruction(2, AVM2Instructions.SetLocal, new int[]{8}));
        code.code.add(new AVM2Instruction(4, AVM2Instructions.SetLocal, new int[]{9}));
        Assert.assertEquals(AVM2DeobfuscatorRegistersOld.getRepeatedlyAssignedRegisters(body), java.util.Collections.singleton(8));
        int[] unsafe = {AVM2Instructions.IfTrue, AVM2Instructions.Jump, AVM2Instructions.LookupSwitch, AVM2Instructions.NewFunction};
        for (int opcode : unsafe) {
            code.code.add(new AVM2Instruction(6, opcode, new int[]{0, 0, 0}));
            Assert.assertTrue(AVM2DeobfuscatorRegistersOld.getRepeatedlyAssignedRegisters(body).isEmpty());
            code.code.remove(code.code.size() - 1);
        }
        body.exceptions = new ABCException[]{new ABCException()};
        Assert.assertTrue(AVM2DeobfuscatorRegistersOld.getRepeatedlyAssignedRegisters(body).isEmpty());
    }
}