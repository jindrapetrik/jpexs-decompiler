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

import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Initial register values must not leak between interpreter attempts.
 */
public class AVM2DeobfuscatorLocalRegsTest {

    @Test
    public void testResetChangedAndOutOfRangeRegisters() {
        AVM2DeobfuscatorSimpleOld.ResettableLocalRegs registers =
                new AVM2DeobfuscatorSimpleOld.ResettableLocalRegs(3, 8);
        Map<Integer, GraphTargetItem> initial = new HashMap<>(registers);
        Assert.assertTrue(registers.get(2) instanceof NotCompileTimeItem);
        Assert.assertTrue(registers.get(3) instanceof UndefinedAVM2Item);
        Assert.assertFalse(registers.containsKey(8));
        for (int attempt = 0; attempt < 3; attempt++) {
            GraphTargetItem value = new IntegerValueAVM2Item(null, null, attempt);
            registers.put(0, value);
            registers.put(2, value);
            registers.put(3, value);
            registers.put(3, null);
            registers.put(7, value);
            registers.put(-1, value);
            registers.put(8, value);
            registers.put(null, value);
            registers.reset();
            Assert.assertEquals(registers, initial);
            registers.reset();
            Assert.assertEquals(registers, initial);
        }
    }

    @Test
    public void testReservedRegistersBeyondDeclaredCount() {
        AVM2DeobfuscatorSimpleOld.ResettableLocalRegs registers =
                new AVM2DeobfuscatorSimpleOld.ResettableLocalRegs(3, 1);
        Assert.assertEquals(registers.size(), 3);
        registers.put(2, new IntegerValueAVM2Item(null, null, 10));
        registers.reset();
        Assert.assertTrue(registers.get(2) instanceof NotCompileTimeItem);
        Assert.assertEquals(registers.size(), 3);
    }
}
