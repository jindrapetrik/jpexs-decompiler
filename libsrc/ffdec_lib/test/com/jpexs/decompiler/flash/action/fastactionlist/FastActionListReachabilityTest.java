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
package com.jpexs.decompiler.flash.action.fastactionlist;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FastActionListReachabilityTest {

    private FastActionList parse(String pcode) throws Exception {
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        return new FastActionList(new ActionList(actions, Utf8Helper.charsetName));
    }

    private List<ActionItem> snapshot(FastActionList list) {
        List<ActionItem> items = new ArrayList<>();
        for (ActionItem item : list) {
            items.add(item);
        }
        return items;
    }

    @Test
    public void testReverseJumpChainAndHypotheticalShortcut() throws Exception {
        int length = 2000;
        StringBuilder pcode = new StringBuilder("Jump node" + length + "\n");
        for (int i = 1; i <= length; i++) {
            pcode.append("node").append(i).append(": Jump ").append(i == 1 ? "done" : "node" + (i - 1)).append('\n');
        }
        pcode.append("done: End\nPush \"unreachable\"\nTrace\nEnd\n");
        FastActionList list = parse(pcode.toString());
        List<ActionItem> items = snapshot(list);
        ActionItem first = items.get(0);
        ActionItem target = first.getJumpTarget();
        int expectedReachable = length + 2;
        Assert.assertEquals(list.getUnreachableActionCount(first, items.get(length + 1)), items.size() - 1);
        Assert.assertSame(first.getJumpTarget(), target);
        list.removeUnreachableActions();
        Assert.assertEquals(list.size(), expectedReachable);
        for (ActionItem item : list) {
            Assert.assertEquals(item.reachable, 2);
        }
    }

    @Test
    public void testConditionalCycleKeepsBothBranches() throws Exception {
        FastActionList list = parse("loop: Push true\nIf right\nPush \"left\"\nTrace\nJump done\n"
                + "right: Push false\nIf loop\nPush \"right\"\nTrace\ndone: End\nPush \"dead\"\nTrace\nEnd\n");
        List<ActionItem> items = snapshot(list);
        Assert.assertEquals(list.getUnreachableActionCount(items.get(1), items.get(5)), 7);
        list.removeUnreachableActions();
        Assert.assertEquals(snapshot(list), items.subList(0, 10));
    }

    @DataProvider(name = "containers")
    public Object[][] containers() {
        return new Object[][]{
            {"DefineFunction \"f\" 0 {\nPush \"body\"\nTrace\nReturn\nPush \"dead\"\nTrace\n}\n", 2},
            {"DefineFunction2 \"f\" 0 1 false false false false false false false false false {\n"
                + "Push \"body\"\nTrace\nReturn\nPush \"dead\"\nTrace\n}\n", 2},
            {"With {\nPush \"body\"\nTrace\nJump outside\nPush \"dead\"\nTrace\n}\n", 2},
            {"Try \"e\" {\nPush \"try\"\nTrace\nReturn\n}\nCatch {\nPush \"catch\"\nTrace\nReturn\n}\n"
                + "Finally {\nPush \"finally\"\nTrace\nReturn\n}\n", 0}
        };
    }

    @Test(dataProvider = "containers")
    public void testContainerBodiesAndContinuations(String container, int removed) throws Exception {
        FastActionList list = parse(container + "outside: Push \"after\"\nTrace\nEnd\n");
        List<ActionItem> items = snapshot(list);
        ActionItem continuation = items.get(items.size() - 3);
        list.removeUnreachableActions();
        Assert.assertEquals(list.size(), items.size() - removed);
        Assert.assertTrue(list.contains(continuation));
        Assert.assertTrue(list.contains(items.get(1)));
        for (ActionItem item : list) {
            Assert.assertEquals(item.reachable, 2);
        }
    }

    @Test
    public void testEmptyListAndSelfLoop() throws Exception {
        FastActionList empty = new FastActionList(new ActionList(Utf8Helper.charsetName, SWF.DEFAULT_VERSION));
        empty.removeUnreachableActions();
        Assert.assertTrue(empty.isEmpty());
        FastActionList loop = parse("loop: Jump loop\nPush \"dead\"\nTrace\nEnd\n");
        ActionItem first = loop.get(0);
        Assert.assertEquals(loop.getUnreachableActionCount(first, first), loop.size());
        loop.removeUnreachableActions();
        Assert.assertEquals(loop.size(), 1);
        Assert.assertSame(loop.get(0).getJumpTarget(), first);
    }

    @Test
    public void testUnreachableShortcutSourceIsNotCountedTwice() throws Exception {
        FastActionList list = parse("End\nJump done\nPush \"dead\"\nTrace\ndone: End\n");
        List<ActionItem> items = snapshot(list);
        Assert.assertEquals(list.getUnreachableActionCount(items.get(1), items.get(4)), items.size() - 1);
        list.removeUnreachableActions();
        Assert.assertEquals(list.size(), 1);
    }
}
