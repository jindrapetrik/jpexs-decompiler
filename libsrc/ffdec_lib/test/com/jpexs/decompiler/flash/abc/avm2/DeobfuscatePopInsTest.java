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

import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Regression tests for removing duplicated values during deobfuscation. */
public class DeobfuscatePopInsTest {

    @Test
    public void testPopAfterTwoDups() throws InterruptedException {
        AVM2LocalData localData = new AVM2LocalData();
        List<GraphTargetItem> output = new ArrayList<>();
        TranslateStack stack = new FixItemCounterTranslateStack("");
        stack.setConnectedOutput(0, output, localData);
        GraphTargetItem object = new ThisAVM2Item(null, null, DottedChain.OBJECT, false, false);
        GraphTargetItem coerced = new CoerceAVM2Item(null, null, object, new TypeItem(DottedChain.OBJECT));
        stack.push(coerced);

        AVM2Instruction dup = new AVM2Instruction(0, AVM2Instructions.Dup, null);
        AVM2Instruction pop = new AVM2Instruction(2, DeobfuscatePopIns.getInstance(), null);
        dup.translate(localData, stack, output, 0, "");
        dup.translate(localData, stack, output, 0, "");
        SetTemporaryItem temporary = (SetTemporaryItem) output.get(0);
        int referencesAfterDups = temporary.refCount;

        pop.translate(localData, stack, output, 0, "");
        Assert.assertEquals(referencesAfterDups, 3);
        Assert.assertEquals(stack.size(), 2);
        Assert.assertEquals(output.size(), 1);
        Assert.assertSame(output.get(0), temporary);
        Assert.assertEquals(temporary.refCount, 2);

        pop.translate(localData, stack, output, 0, "");
        Assert.assertEquals(stack.size(), 1);
        Assert.assertSame(stack.peek(), coerced);
        Assert.assertTrue(output.isEmpty());
    }
}
