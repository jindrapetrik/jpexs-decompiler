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
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.EmptyCommand;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Regression tests for trailing continues in AS1/2 and AS3. */
public class TrailingContinueTest {

    @DataProvider(name = "dialects")
    public Object[][] dialects() {
        return new Object[][] {
            {ActionGraphTargetDialect.INSTANCE},
            {AVM2GraphTargetDialect.INSTANCE}
        };
    }

    private static class TestGraph extends Graph {

        TestGraph(GraphTargetDialect dialect) {
            super(dialect, null, new ArrayList<>(), 0);
        }

        void process(GraphTargetItem item) {
            finalProcessAfter(commands(item), 0, null, "");
        }
    }

    private static List<GraphTargetItem> commands(GraphTargetItem... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    private ContinueItem continueTo(GraphTargetDialect dialect, long id) {
        return new ContinueItem(dialect, null, null, id);
    }

    private WhileItem whileLoop(GraphTargetDialect dialect, long id, GraphTargetItem... body) {
        return new WhileItem(dialect, null, null, new Loop(id, null, null),
                commands(dialect.valToItem(false)), commands(body));
    }

    @Test(dataProvider = "dialects")
    public void testNestedWhileBodies(GraphTargetDialect dialect) {
        EmptyCommand increment = new EmptyCommand(dialect);
        WhileItem inner = whileLoop(dialect, 2, increment, continueTo(dialect, 2));
        WhileItem outer = whileLoop(dialect, 1, inner, continueTo(dialect, 1));
        IfItem branch = new IfItem(dialect, null, null, dialect.valToItem(false),
                commands(outer), commands());

        new TestGraph(dialect).process(branch);

        Assert.assertEquals(inner.commands, commands(increment));
        Assert.assertEquals(outer.commands, commands(inner));
    }

    @Test(dataProvider = "dialects")
    public void testDoWhileAndForBodies(GraphTargetDialect dialect) {
        EmptyCommand statement = new EmptyCommand(dialect);
        DoWhileItem doWhile = new DoWhileItem(dialect, null, null,
                new Loop(1, null, null), commands(statement, continueTo(dialect, 1)),
                commands(dialect.valToItem(false)));
        ForItem forLoop = new ForItem(dialect, null, null, new Loop(2, null, null),
                commands(), dialect.valToItem(false), commands(statement),
                commands(doWhile, continueTo(dialect, 2)));

        new TestGraph(dialect).process(forLoop);

        Assert.assertEquals(doWhile.commands, commands(statement));
        Assert.assertEquals(forLoop.commands, commands(doWhile));
        Assert.assertEquals(forLoop.finalCommands, commands(statement));
    }

    @Test(dataProvider = "dialects")
    public void testNecessaryContinuesArePreserved(GraphTargetDialect dialect) {
        ContinueItem early = continueTo(dialect, 2);
        ContinueItem conditional = continueTo(dialect, 2);
        ContinueItem outerContinue = continueTo(dialect, 1);
        EmptyCommand statement = new EmptyCommand(dialect);
        IfItem branch = new IfItem(dialect, null, null, dialect.valToItem(false),
                commands(conditional), commands());
        WhileItem inner = whileLoop(dialect, 2, early, statement, branch, outerContinue);
        WhileItem outer = whileLoop(dialect, 1, inner, statement);

        new TestGraph(dialect).process(outer);

        Assert.assertEquals(inner.commands, commands(early, statement, branch, outerContinue));
        Assert.assertEquals(branch.onTrue, commands(conditional));
    }

    @Test(dataProvider = "dialects")
    public void testSwitchCasesAndLoopHeadersArePreserved(GraphTargetDialect dialect) {
        ContinueItem caseContinue = continueTo(dialect, 1);
        SwitchItem switchItem = new SwitchItem(dialect, null, null,
                new Loop(2, null, null), dialect.valToItem("value"),
                commands(dialect.valToItem(0L)),
                new ArrayList<>(Arrays.asList(commands(caseContinue))), Arrays.asList(0));
        ContinueItem headerContinue = continueTo(dialect, 1);
        ForItem forLoop = new ForItem(dialect, null, null, new Loop(1, null, null),
                commands(headerContinue), dialect.valToItem(false), commands(headerContinue),
                commands(switchItem, continueTo(dialect, 1)));

        new TestGraph(dialect).process(forLoop);

        Assert.assertEquals(forLoop.commands, commands(switchItem));
        Assert.assertEquals(switchItem.caseCommands.get(0), commands(caseContinue));
        Assert.assertEquals(forLoop.firstCommands, commands(headerContinue));
        Assert.assertEquals(forLoop.finalCommands, commands(headerContinue));
    }
}
