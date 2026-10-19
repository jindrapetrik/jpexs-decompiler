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
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.EmptyCommand;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Regression tests for eliminating unnecessary multilevel control flow. */
public class MultilevelContinueRestructureTest {

    private static class TestGraph extends Graph {

        TestGraph(GraphTargetDialect dialect) {
            super(dialect, null, new ArrayList<>(), 0);
        }

        void process(List<GraphTargetItem> commands) {
            finalProcessAfter(commands, 0, null, "");
        }
    }

    private static class Scenario {

        final GraphTargetDialect dialect;
        final TrueItem condition;
        final BreakItem innerBreak;
        final EmptyCommand outerIncrement;
        final GraphTargetItem breakPath;
        final WhileItem innerWhile;
        final List<GraphTargetItem> outerBody;

        Scenario(GraphTargetDialect dialect, GraphTargetItem breakPath) {
            this.dialect = dialect;
            this.breakPath = breakPath;
            Loop outerLoop = new Loop(1, null, null);
            Loop innerLoop = new Loop(2, null, null);
            condition = new TrueItem(dialect, null, null);
            innerBreak = new BreakItem(dialect, null, null, innerLoop.id);
            outerIncrement = new EmptyCommand(dialect);

            IfItem guardedBody = new IfItem(dialect, null, null, condition,
                    new ArrayList<>(Arrays.asList(innerBreak)), new ArrayList<>());
            innerWhile = new WhileItem(dialect, null, null, innerLoop,
                    new ArrayList<>(Arrays.asList(new TrueItem(dialect, null, null))),
                    new ArrayList<>(Arrays.asList(guardedBody, outerIncrement,
                            new ContinueItem(dialect, null, null, outerLoop.id))));
            outerBody = new ArrayList<>(Arrays.asList(innerWhile, breakPath));
        }

        void process() {
            new TestGraph(dialect).process(outerBody);
        }
    }

    @Test
    public void testTrailingOuterContinueIsRestructuredWhenDialectAllowsLabels() {
        Scenario scenario = new Scenario(AVM2GraphTargetDialect.INSTANCE,
                new EmptyCommand(AVM2GraphTargetDialect.INSTANCE));

        scenario.process();

        Assert.assertTrue(scenario.dialect.doesAllowMultilevelBreaks());
        Assert.assertEquals(scenario.innerWhile.expression, Arrays.asList(scenario.condition));
        Assert.assertEquals(scenario.innerWhile.commands,
                Arrays.asList(scenario.breakPath, scenario.innerBreak));
        Assert.assertEquals(scenario.outerBody,
                Arrays.asList(scenario.innerWhile, scenario.outerIncrement));
    }

    @Test
    public void testBreakAfterMovedExitIsRemoved() {
        Scenario scenario = new Scenario(ActionGraphTargetDialect.INSTANCE,
                new ScriptEndItem(ActionGraphTargetDialect.INSTANCE));

        scenario.process();

        Assert.assertEquals(scenario.innerWhile.commands, Arrays.asList(scenario.breakPath));
        Assert.assertEquals(scenario.outerBody,
                Arrays.asList(scenario.innerWhile, scenario.outerIncrement));
    }
}
