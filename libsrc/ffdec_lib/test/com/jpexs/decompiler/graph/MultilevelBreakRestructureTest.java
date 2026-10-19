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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphTargetDialect;
import com.jpexs.decompiler.flash.action.ActionGraphTargetDialect;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Regression tests for eliminating redundant labeled breaks in AS1/2 and AS3. */
public class MultilevelBreakRestructureTest {

    @DataProvider(name = "dialects")
    public Object[][] dialects() {
        return new Object[][] {
            {ActionGraphTargetDialect.INSTANCE},
            {AVM2GraphTargetDialect.INSTANCE}
        };
    }

    private static class BreakTestGraph extends Graph {

        BreakTestGraph(GraphTargetDialect dialect) {
            super(dialect, null, new ArrayList<>(), 0);
        }

        void process(GraphTargetItem item) {
            finalProcessAfter(commands(item), 0, null, "");
        }
    }

    private static List<GraphTargetItem> commands(GraphTargetItem... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    private BreakItem breakTo(GraphTargetDialect dialect, long id) {
        return new BreakItem(dialect, null, null, id);
    }

    @SafeVarargs
    private final SwitchItem switchWithCases(GraphTargetDialect dialect, long id, List<GraphTargetItem>... cases) {
        List<GraphTargetItem> values = new ArrayList<>();
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < cases.length; i++) {
            values.add(dialect.valToItem((long) i));
            mapping.add(i);
        }
        return new SwitchItem(dialect, null, null,
                new Loop(id, null, null), dialect.valToItem("value"),
                values, new ArrayList<>(Arrays.asList(cases)), mapping);
    }

    @Test(dataProvider = "dialects")
    public void testBreakThroughThreeTailSwitchesAndIf(GraphTargetDialect dialect) {
        BreakItem outerBreak = breakTo(dialect, 1);
        IfItem branch = new IfItem(dialect, null, null,
                dialect.valToItem(true), commands(outerBreak), commands());
        SwitchItem inner = switchWithCases(dialect, 3, commands(branch));
        SwitchItem middle = switchWithCases(dialect, 2, commands(inner));
        SwitchItem outer = switchWithCases(dialect, 1, commands(middle));
        new BreakTestGraph(dialect).process(outer);
        Assert.assertEquals(outerBreak.loopId, 3L);
    }

    @Test(dataProvider = "dialects")
    public void testBreakThroughSwitchBeforeExplicitOuterBreak(GraphTargetDialect dialect) {
        BreakItem outerBreak = breakTo(dialect, 1);
        SwitchItem inner = switchWithCases(dialect, 2, commands(outerBreak), commands());
        BreakItem followingBreak = breakTo(dialect, 1);
        SwitchItem outer = switchWithCases(dialect, 1, commands(inner, followingBreak),
                commands(dialect.valToItem("following case")));
        new BreakTestGraph(dialect).process(outer);
        Assert.assertEquals(outerBreak.loopId, 2L);
        Assert.assertSame(outer.caseCommands.get(0).get(1), followingBreak);
    }

    @Test(dataProvider = "dialects")
    public void testOuterBreakPreservedWhenCaseFallsThrough(GraphTargetDialect dialect) {
        BreakItem outerBreak = breakTo(dialect, 1);
        SwitchItem inner = switchWithCases(dialect, 2, commands(outerBreak), commands());
        SwitchItem outer = switchWithCases(dialect, 1, commands(inner),
                commands(dialect.valToItem("following case")));
        new BreakTestGraph(dialect).process(outer);
        Assert.assertEquals(outerBreak.loopId, 1L);
    }

    @Test(dataProvider = "dialects")
    public void testOuterBreakPreservedWhenSwitchHasFollowingCode(GraphTargetDialect dialect) {
        BreakItem outerBreak = breakTo(dialect, 1);
        SwitchItem inner = switchWithCases(dialect, 2, commands(outerBreak), commands());
        GraphTargetItem following = dialect.valToItem("following code");
        SwitchItem outer = switchWithCases(dialect, 1, commands(inner, following));
        new BreakTestGraph(dialect).process(outer);
        Assert.assertEquals(outerBreak.loopId, 1L);
        Assert.assertSame(outer.caseCommands.get(0).get(1), following);
    }

    @Test(dataProvider = "dialects")
    public void testOuterBreakPreservedAcrossRealLoop(GraphTargetDialect dialect) {
        BreakItem outerBreak = breakTo(dialect, 1);
        SwitchItem inner = switchWithCases(dialect, 3, commands(outerBreak), commands());
        WhileItem loop = new WhileItem(dialect, null, null,
                new Loop(2, null, null), commands(dialect.valToItem(true)), commands(inner));
        SwitchItem outer = switchWithCases(dialect, 1, commands(loop));
        new BreakTestGraph(dialect).process(outer);
        Assert.assertEquals(outerBreak.loopId, 1L);
    }
}
