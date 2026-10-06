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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import java.util.List;
import java.util.Random;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Compare bulk removal with the original individual offset updates.
 */
public class AVM2CodeRemoveIgnoredTest {

    private AVM2Code createCode() {
        AVM2Code code = new AVM2Code();
        code.code.add(new AVM2Instruction(0, 0x10, new int[]{0})); // jump
        code.code.add(new AVM2Instruction(0, 0xf0, new int[]{16384})); // debugline
        code.code.add(new AVM2Instruction(0, 0x24, new int[]{7})); // pushbyte
        code.code.add(new AVM2Instruction(0, 0x1b, new int[]{0, 2, 0, 0, 0})); // lookupswitch
        code.code.add(new AVM2Instruction(0, 0xf0, new int[]{128}));
        code.code.add(new AVM2Instruction(0, 0x11, new int[]{0})); // iftrue
        code.code.add(new AVM2Instruction(0, 0x02, null)); // nop
        code.code.add(new AVM2Instruction(0, 0x47, null)); // returnvoid
        long address = 0;
        for (AVM2Instruction ins : code.code) {
            ins.setAddress(address);
            address += ins.getBytesLength();
        }
        setTarget(code.code.get(0), code.code.get(4).getAddress());
        AVM2Instruction lookup = code.code.get(3);
        lookup.operands[0] = (int) (code.code.get(7).getAddress() - lookup.getAddress());
        lookup.operands[2] = (int) (code.code.get(1).getAddress() - lookup.getAddress());
        lookup.operands[3] = 0; // self-target
        lookup.operands[4] = (int) (address - lookup.getAddress()); // end of code
        setTarget(code.code.get(5), code.code.get(0).getAddress());
        return code;
    }

    private void setTarget(AVM2Instruction ins, long target) {
        ins.operands[0] = (int) (target - ins.getAddress() - ins.getBytesLength());
    }

    private MethodBody body(int start, int end, int target) {
        MethodBody body = new MethodBody();
        ABCException ex = new ABCException();
        ex.start = start;
        ex.end = end;
        ex.target = target;
        ex.type_index = 3;
        ex.name_index = 5;
        body.exceptions = new ABCException[]{ex};
        return body;
    }

    private void compareWithIndividualRemoval(AVM2Code actual, MethodBody actualBody) throws InterruptedException {
        AVM2Code expected = actual.clone();
        MethodBody expectedBody = null;
        if (actualBody != null) {
            ABCException ex = actualBody.exceptions[0];
            expectedBody = body(ex.start, ex.end, ex.target);
        }
        for (int i = 0; i < expected.code.size(); i++) {
            if (expected.code.get(i).isIgnored()) {
                expected.removeInstruction(i--, expectedBody);
            }
        }
        List<AVM2Instruction> originalList = actual.code;
        actual.removeIgnored(actualBody);
        Assert.assertSame(actual.code, originalList);
        Assert.assertEquals(actual.code.size(), expected.code.size());
        for (int i = 0; i < expected.code.size(); i++) {
            AVM2Instruction a = actual.code.get(i);
            AVM2Instruction e = expected.code.get(i);
            Assert.assertSame(a.definition, e.definition, "Instruction " + i);
            Assert.assertEquals(a.getAddress(), e.getAddress(), "Address " + i);
            Assert.assertEquals(a.operands, e.operands, "Operands " + i);
            Assert.assertFalse(a.isIgnored());
        }
        if (actualBody != null) {
            ABCException a = actualBody.exceptions[0];
            ABCException e = expectedBody.exceptions[0];
            Assert.assertEquals(a.start, e.start);
            Assert.assertEquals(a.end, e.end);
            Assert.assertEquals(a.target, e.target);
            Assert.assertEquals(a.type_index, e.type_index);
            Assert.assertEquals(a.name_index, e.name_index);
        }
        // Repeated removal must be a no-op.
        actual.removeIgnored(actualBody);
        Assert.assertEquals(actual.code.size(), expected.code.size());
    }

    @Test
    public void testAllRemovalCombinations() throws InterruptedException {
        // Covers adjacent, leading and trailing removals, no removals, all
        // removals, forward/backward jumps and every lookupswitch target.
        for (int mask = 0; mask < 256; mask++) {
            AVM2Code code = createCode();
            for (int i = 0; i < code.code.size(); i++) {
                code.code.get(i).setIgnored((mask & (1 << i)) != 0, 0);
            }
            compareWithIndividualRemoval(code, body((int) code.code.get(1).getAddress(), (int) code.getEndOffset(), (int) code.code.get(4).getAddress()));
        }
        compareWithIndividualRemoval(new AVM2Code(), null);
    }

    @Test
    public void testInvalidTargetsAndExceptionBoundaries() throws InterruptedException {
        for (int target : new int[]{-10, 1, 5, 9, 20, 100}) {
            AVM2Code code = createCode();
            code.code.get(1).setIgnored(true, 0);
            code.code.get(2).setIgnored(true, 0);
            code.code.get(4).setIgnored(true, 0);
            setTarget(code.code.get(0), target);
            compareWithIndividualRemoval(code, null);

            code = createCode();
            code.code.get(1).setIgnored(true, 0);
            code.code.get(2).setIgnored(true, 0);
            compareWithIndividualRemoval(code, body(target, target + 1, target));
        }
    }

    @Test
    public void testRandomTargetsAndExceptionRanges() throws InterruptedException {
        Random random = new Random(2750);
        for (int run = 0; run < 500; run++) {
            AVM2Code code = createCode();
            long[] targets = new long[code.code.size() + 1];
            for (int i = 0; i < code.code.size(); i++) {
                targets[i] = code.code.get(i).getAddress();
            }
            targets[code.code.size()] = code.getEndOffset();
            for (AVM2Instruction ins : code.code) {
                ins.setIgnored(random.nextBoolean(), 0);
                if (ins.definition instanceof LookupSwitchIns) {
                    for (int i = 0; i < ins.operands.length; i++) {
                        if (i != 1) {
                            ins.operands[i] = (int) (targets[random.nextInt(targets.length)] - ins.getAddress());
                        }
                    }
                } else if (ins.definition instanceof IfTypeIns) {
                    setTarget(ins, targets[random.nextInt(targets.length)]);
                }
            }
            int start = random.nextInt(targets.length);
            int end = start + random.nextInt(targets.length - start);
            compareWithIndividualRemoval(code, body((int) targets[start], (int) targets[end],
                    (int) targets[random.nextInt(targets.length)]));
        }
    }

    @Test
    public void testNonContiguousAddresses() throws InterruptedException {
        AVM2Code code = createCode();
        code.code.get(1).setIgnored(true, 0);
        code.code.get(2).setAddress(code.code.get(2).getAddress() + 1);
        compareWithIndividualRemoval(code, null);
    }
}
