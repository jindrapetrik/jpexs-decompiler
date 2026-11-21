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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LabelItem;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Regression tests for goto processing. */
public class GraphGotoTest {

    @Test
    public void testSharedSwitchTailPreferredOverTiedFallthroughCase() throws InterruptedException {
        GraphPart firstCase = new GraphPart(0, 0);
        GraphPart secondCase = new GraphPart(1, 1);
        GraphPart thirdCase = new GraphPart(2, 2);
        GraphPart fallthroughCase = new GraphPart(3, 3);
        GraphPart nextFallthroughCase = new GraphPart(4, 4);
        GraphPart defaultReturn = new GraphPart(5, 5);
        GraphPart shared = new GraphPart(6, 6);
        GraphPart earlyReturn = new GraphPart(7, 7);
        GraphPart continuation = new GraphPart(8, 8);
        firstCase.nextParts.add(shared);
        secondCase.nextParts.add(shared);
        thirdCase.nextParts.add(shared);
        fallthroughCase.nextParts.add(nextFallthroughCase);
        nextFallthroughCase.nextParts.add(defaultReturn);
        shared.nextParts.addAll(Arrays.asList(earlyReturn, continuation));
        // Both defaultReturn and shared are reachable from three case entries.
        // With equal closing times, the earlier default used to win the tie.
        GraphSource source = new ActionGraphSource("", false, Collections.<Action>nCopies(9, new ActionEnd()),
                10, new HashMap<>(), new HashMap<>(), new HashMap<>(), Utf8Helper.charsetName, 0);
        Graph graph = new Graph(null, source, Collections.emptyList(), 0);
        Assert.assertSame(graph.getMostCommonPart(new BaseLocalData() { },
                Arrays.asList(firstCase, secondCase, thirdCase, fallthroughCase, nextFallthroughCase, defaultReturn),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>()), shared);
    }

    @Test
    public void testSharedSwitchTailStartsBeforeItsConditionalExit() throws InterruptedException {
        GraphPart firstCase = new GraphPart(0, 0);
        GraphPart secondCase = new GraphPart(1, 1);
        GraphPart defaultReturn = new GraphPart(2, 2);
        GraphPart shared = new GraphPart(3, 3);
        GraphPart earlyReturn = new GraphPart(4, 4);
        GraphPart continuation = new GraphPart(5, 5);
        GraphPart done = new GraphPart(6, 6);
        firstCase.nextParts.add(shared);
        secondCase.nextParts.add(shared);
        shared.nextParts.addAll(Arrays.asList(earlyReturn, continuation));
        continuation.nextParts.add(done);
        // Descendants are closed before their predecessors in the graph walk.
        shared.closedTime = 4;
        earlyReturn.closedTime = 3;
        continuation.closedTime = 2;
        done.closedTime = 1;
        GraphSource source = new ActionGraphSource("", false, Collections.<Action>nCopies(7, new ActionEnd()),
                10, new HashMap<>(), new HashMap<>(), new HashMap<>(), Utf8Helper.charsetName, 0);
        Graph graph = new Graph(null, source, Collections.emptyList(), 0);

        Assert.assertSame(graph.getMostCommonPart(new BaseLocalData() { },
                Arrays.asList(firstCase, secondCase, defaultReturn), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()), shared);
    }

    @Test
    public void testKeepGotoWhenItsTargetContainsTheJump() throws ReflectiveOperationException {
        GotoItem jump = new GotoItem(null, null, null, "target");
        List<GraphTargetItem> branch = new ArrayList<>(Collections.singletonList(jump));
        IfItem inner = new IfItem(null, null, null, null, branch, Collections.emptyList());
        IfItem outer = new IfItem(null, null, null, null, Collections.singletonList(inner), Collections.emptyList());
        LabelItem label = new LabelItem(null, null, null, "target");
        ReturnVoidAVM2Item exit = new ReturnVoidAVM2Item(null, null);
        List<GraphTargetItem> target = new ArrayList<>(Arrays.asList(label, outer, exit));
        List<List<GraphTargetItem>> processed = new ArrayList<>(Collections.singletonList(target));

        Method processIfGotos = Graph.class.getDeclaredMethod("processIfGotos2", List.class, List.class, List.class, List.class);
        processIfGotos.setAccessible(true);
        Graph graph = new Graph(null, null, Collections.emptyList(), 0);
        processIfGotos.invoke(graph, processed, Collections.singletonList(jump), branch, target);

        Assert.assertEquals(branch.size(), 1);
        Assert.assertSame(branch.get(0), jump);
        Assert.assertEquals(target.size(), 3);
        Assert.assertSame(target.get(0), label);
        Assert.assertSame(target.get(1), outer);
        Assert.assertSame(target.get(2), exit);
    }
}
