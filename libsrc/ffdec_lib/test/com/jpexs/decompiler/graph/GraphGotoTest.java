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
import com.jpexs.decompiler.flash.abc.avm2.model.ThrowAVM2Item;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LabelItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.CommentItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.UniversalLoopItem;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

/** Regression tests for goto processing. */
public class GraphGotoTest {

    private List<GraphTargetItem> commands(GraphTargetItem... items) {
        return new GraphPartMarkedArrayList<>(Arrays.asList(items));
    }

    private void removeUnreachable(List<GraphTargetItem> commands) {
        new Graph(null, null, Collections.emptyList(), 0).removeUnreachableCommands(commands);
    }

    @DataProvider(name = "conditionalTailGuards")
    public Object[][] conditionalTailGuards() {
        return new Object[][]{{"external jump"}, {"duplicate label"}, {"break"}, {"other jump"}, {"nested loop"}};
    }

    @Test(dataProvider = "conditionalTailGuards")
    public void testConditionalSharedTailKeepsOtherEntriesAndControlScopes(String guard) throws ReflectiveOperationException {
        GraphTargetItem prefix = new CommentItem("prefix");
        if (guard.equals("break")) {
            prefix = new BreakItem(null, null, null, 1);
        } else if (guard.equals("other jump")) {
            prefix = new GotoItem(null, null, null, "other");
        } else if (guard.equals("nested loop")) {
            prefix = new UniversalLoopItem(null, null, null, new Loop(1, null, null),
                    commands(new BreakItem(null, null, null, 1)));
        }
        LabelItem label = new LabelItem(null, null, null, "shared");
        GotoItem jump = new GotoItem(null, null, null, "shared");
        IfItem gate = new IfItem(null, null, null, new TrueItem(null, null, null), commands(jump), commands());
        List<GraphTargetItem> target = commands(prefix, label, new CommentItem("shared"));
        List<GraphTargetItem> originalTarget = new ArrayList<>(target);
        IfItem branch = new IfItem(null, null, null, new TrueItem(null, null, null), target, commands(gate));
        List<GraphTargetItem> root = commands(branch);
        if (guard.equals("external jump")) {
            root.add(new GotoItem(null, null, null, "shared"));
        } else if (guard.equals("duplicate label")) {
            root.add(new LabelItem(null, null, null, "shared"));
        }
        Method structure = Graph.class.getDeclaredMethod("structureSharedConditionalTails", List.class, List.class, List.class);
        structure.setAccessible(true);
        structure.invoke(new Graph(null, null, Collections.emptyList(), 0), root, root, new ArrayList<Loop>());
        Assert.assertSame(root.get(0), branch);
        Assert.assertEquals(target, originalTarget);
        Assert.assertSame(gate.onTrue.get(0), jump);
    }

    @DataProvider(name = "unconditionalTransfers")
    public Object[][] unconditionalTransfers() {
        return new Object[][]{{new BreakItem(null, null, null, 14)}, {new ContinueItem(null, null, null, 0)},
            {new ReturnVoidAVM2Item(null, null)}, {new ThrowAVM2Item(null, null, new TrueItem(null, null, null))},
            {new GotoItem(null, null, null, "target")}, {new ScriptEndItem(null)}};
    }

    @Test(dataProvider = "unconditionalTransfers")
    public void testTransferRemovesDeadTailButKeepsLabelEntry(GraphTargetItem exit) {
        LabelItem label = new LabelItem(null, null, null, "target");
        CommentItem live = new CommentItem("reachable");
        List<GraphTargetItem> root = commands(exit, new BreakItem(null, null, null, 1),
                new GotoItem(null, null, null, "wrong"), new CommentItem("dead"), label, live);
        removeUnreachable(root);
        Assert.assertEquals(root, Arrays.asList(exit, label, live));
    }

    @Test
    public void testExpandedGotoExitAlsoTerminatesItsParentList() {
        GotoItem expanded = new GotoItem(null, null, null, null);
        ReturnVoidAVM2Item exit = new ReturnVoidAVM2Item(null, null);
        expanded.targetCommands = commands(exit, new CommentItem("dead in target"));
        List<GraphTargetItem> root = commands(expanded, new BreakItem(null, null, null, 0));
        removeUnreachable(root);
        Assert.assertEquals(root, Collections.singletonList(expanded));
        Assert.assertEquals(expanded.targetCommands, Collections.singletonList(exit));
    }

    @Test
    public void testUnreachableTailKeepsNestedIncomingLabel() {
        IfItem incoming = new IfItem(null, null, null, new TrueItem(null, null, null),
                commands(new LabelItem(null, null, null, "entry"), new CommentItem("live")), commands());
        CommentItem after = new CommentItem("after entry");
        List<GraphTargetItem> root = commands(new ReturnVoidAVM2Item(null, null), new CommentItem("dead"), incoming, after);
        removeUnreachable(root);
        Assert.assertEquals(root, Arrays.asList(root.get(0), incoming, after));
    }

    @Test
    public void testExpandedGotoLabelKeepsItsIncomingEntry() {
        GotoItem target = new GotoItem(null, null, null, "entry");
        CommentItem live = new CommentItem("live");
        target.targetCommands = commands(live);
        GotoItem jump = new GotoItem(null, null, null, "entry");
        CommentItem after = new CommentItem("after entry");
        List<GraphTargetItem> root = commands(jump, new CommentItem("dead"), target, after);
        removeUnreachable(root);
        Assert.assertEquals(root, Arrays.asList(jump, target, after));
        Assert.assertEquals(target.targetCommands, Collections.singletonList(live));
    }

    @Test
    public void testBothExitingBranchesTerminateTheirParentList() {
        ReturnVoidAVM2Item exit = new ReturnVoidAVM2Item(null, null);
        BreakItem jump = new BreakItem(null, null, null, 14);
        IfItem branch = new IfItem(null, null, null, new TrueItem(null, null, null),
                commands(exit, new CommentItem("dead true")), commands(jump, new BreakItem(null, null, null, 0)));
        List<GraphTargetItem> root = commands(branch, new CommentItem("dead after if"));
        removeUnreachable(root);
        Assert.assertEquals(root, Collections.singletonList(branch));
        Assert.assertEquals(branch.onTrue, Collections.singletonList(exit));
        Assert.assertEquals(branch.onFalse, Collections.singletonList(jump));
    }

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
