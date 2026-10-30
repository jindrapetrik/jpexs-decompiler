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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionWaitForFrame2;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionWith;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ActionSourceGeneratorPushTest {

    private static final String CHARSET = Utf8Helper.charsetName;

    private ActionPush push(long value) {
        return new ActionPush(value, CHARSET);
    }

    private List<Action> group(int version, GraphSourceItem... items) {
        return new ActionSourceGenerator(version, new ArrayList<String>(), 0, CHARSET, null)
                .toActionList(Arrays.asList(items));
    }

    private int length(List<Action> actions) {
        return Action.actionsToBytes(actions, false, 7).length;
    }

    private void assertValues(Action action, Long... values) {
        Assert.assertTrue(action instanceof ActionPush);
        Assert.assertEquals(((ActionPush) action).values, Arrays.asList(values));
    }

    @Test
    public void groupsValuesInOrderAndIsIdempotent() {
        ActionPush first = push(1);
        ActionPush second = push(2);
        List<Action> result = group(5, first, second, push(3));
        Assert.assertEquals(result.size(), 1);
        assertValues(result.get(0), 1L, 2L, 3L);
        Assert.assertEquals(result.get(0).getTotalActionLength(), 18);
        Assert.assertEquals(first.values, Collections.singletonList(1L));
        Assert.assertEquals(second.values, Collections.singletonList(2L));
        Assert.assertEquals(Action.actionsToBytes(group(5, result.toArray(new GraphSourceItem[0])), false, 5),
                Action.actionsToBytes(result, false, 5));
    }

    @Test
    public void leavesSwf4PushesSeparate() {
        Assert.assertEquals(group(4, push(1), push(2)).size(), 2);
    }

    @DataProvider(name = "branches")
    public Object[][] branches() {
        return new Object[][]{{false, false}, {true, false}, {false, true}, {true, true}};
    }

    @Test(dataProvider = "branches")
    public void preservesEntryIntoPushSequence(boolean conditional, boolean backward) {
        ActionPush first = push(1);
        ActionPush target = push(2);
        ActionPush third = push(3);
        int offset = backward ? -(target.getBytesLength() + third.getBytesLength() + 5) : first.getBytesLength();
        Action branch = conditional ? new ActionIf(offset, CHARSET) : new ActionJump(offset, CHARSET);
        List<Action> result = backward ? group(7, first, target, third, branch) : group(7, branch, first, target, third);
        Assert.assertEquals(result.size(), 3);
        assertValues(result.get(backward ? 0 : 1), 1L);
        assertValues(result.get(backward ? 1 : 2), 2L, 3L);
        int relocatedOffset = conditional ? ((ActionIf) branch).getJumpOffset() : ((ActionJump) branch).getJumpOffset();
        Assert.assertEquals(relocatedOffset, backward ? -(result.get(1).getBytesLength() + 5) : first.getBytesLength());
    }

    @Test
    public void relocatesBranchesOverMergedPushesAndToListEnd() {
        ActionJump forward = new ActionJump(21, CHARSET);
        ActionJump backward = new ActionJump(-21, CHARSET);
        List<Action> result = group(7, forward, push(1), push(2), backward);
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(forward.getJumpOffset(), length(result) - 5);
        Assert.assertEquals(backward.getJumpOffset(), 5 - length(result));
    }

    @Test
    public void preservesExternalBranchTargetsInPartialLists() {
        ActionJump forward = new ActionJump(26, CHARSET);
        List<Action> result = group(7, forward, push(1), push(2));
        Assert.assertEquals(forward.getJumpOffset() + 5, length(result) + 10);
        ActionJump backward = new ActionJump(-31, CHARSET);
        result = group(7, push(1), push(2), backward);
        Assert.assertEquals(length(result) + backward.getJumpOffset(), -10);
    }

    @DataProvider(name = "containers")
    public Object[][] containers() {
        return new Object[][]{
            {new ActionDefineFunction("f", Collections.<String>emptyList(), 16, CHARSET)},
            {new ActionDefineFunction2("f", false, false, false, false, false, false, false, false, false,
                0, 16, Collections.<String>emptyList(), Collections.<Integer>emptyList(), CHARSET)},
            {new ActionWith(16, CHARSET)}
        };
    }

    @Test(dataProvider = "containers")
    public void preservesContainerEndAndUpdatesSize(Action container) {
        List<Action> result = group(7, container, push(1), push(2), push(3), push(4));
        Assert.assertEquals(result.size(), 3);
        assertValues(result.get(1), 1L, 2L);
        assertValues(result.get(2), 3L, 4L);
        Assert.assertEquals(((GraphSourceItemContainer) container).getContainerSizes(), Collections.singletonList(13L));
    }

    @Test
    public void preservesNestedContainerEnds() {
        ActionWith inner = new ActionWith(16, CHARSET);
        ActionDefineFunction outer = new ActionDefineFunction("f", Collections.<String>emptyList(),
                16 + inner.getBytesLength() + 24, CHARSET);
        List<Action> result = group(7, outer, push(1), push(2), inner, push(3), push(4), push(5), push(6));
        Assert.assertEquals(result.size(), 6);
        assertValues(result.get(1), 1L, 2L);
        assertValues(result.get(3), 3L, 4L);
        assertValues(result.get(4), 5L);
        assertValues(result.get(5), 6L);
        Assert.assertEquals(inner.codeSize, 13);
        Assert.assertEquals(outer.codeSize, 13 + inner.getBytesLength() + 21);
    }

    @Test
    public void preservesTryCatchFinallyDivisions() {
        ActionTry container = new ActionTry(false, true, true, "error", 0, 16, 16, 16, 7, CHARSET);
        List<Action> result = group(7, container, push(1), push(2), push(3), push(4), push(5), push(6), push(7), push(8));
        Assert.assertEquals(result.size(), 5);
        for (int i = 1; i <= 4; i++) {
            assertValues(result.get(i), 2L * i - 1, 2L * i);
        }
        Assert.assertEquals(container.getContainerSizes(), Arrays.asList(13L, 13L, 13L));
    }

    @Test
    public void updatesFrameSkipCountAndPreservesSkipTarget() {
        ActionWaitForFrame2 wait = new ActionWaitForFrame2(2, CHARSET, 7);
        List<Action> result = group(7, wait, push(1), push(2), push(3), push(4));
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(wait.skipCount, 1);
        Assert.assertEquals(wait.skipped, result.subList(1, 2));
        assertValues(result.get(1), 1L, 2L);
        assertValues(result.get(2), 3L, 4L);
    }

    @Test
    public void respectsMaximumPushPayloadLength() {
        char[] chars = new char[65530];
        Arrays.fill(chars, 'a');
        List<Action> result = group(7, new ActionPush(new String(chars), CHARSET),
                new ActionPush("x", CHARSET), new ActionPush(true, CHARSET));
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getBytesLength() - 3, 0xFFFF);
        byte[] bytes = Action.actionsToBytes(result, false, 7);
        Assert.assertEquals(bytes[1] & 0xFF, 0xFF);
        Assert.assertEquals(bytes[2] & 0xFF, 0xFF);
    }

    @Test
    public void compiledFunctionsAndTypedCatchesHaveValidBoundaries() throws Exception {
        for (int version : new int[]{5, 6, 7}) {
            SWF swf = new SWF();
            swf.version = version;
            String script = "function f(x) { var a = [1, 2, 3]; function g() { return [4, 5]; }"
                    + "try { if (x) return a; } catch (e:Error) { trace([6, 7]); }"
                    + "catch (e:Object) { trace([8, 9]); } finally { trace([10, 11]); } return g(); }";
            List<Action> actions = new ActionScript2Parser(swf, new DoActionTag(swf)).actionsFromString(script, CHARSET);
            byte[] bytes = Action.actionsToBytes(actions, false, version);
            SWFInputStream input = new SWFInputStream(swf, bytes);
            List<Long> boundaries = new ArrayList<>();
            List<Long> targets = new ArrayList<>();
            while (input.available() > 0) {
                long address = input.getPos();
                boundaries.add(address);
                Action action = input.readAction();
                Assert.assertNotNull(action);
                long next = address + action.getBytesLength();
                if (action instanceof ActionIf) {
                    targets.add(next + ((ActionIf) action).getJumpOffset());
                } else if (action instanceof ActionJump) {
                    targets.add(next + ((ActionJump) action).getJumpOffset());
                }
                if (action instanceof GraphSourceItemContainer) {
                    GraphSourceItemContainer container = (GraphSourceItemContainer) action;
                    long end = address + container.getHeaderSize();
                    for (long size : container.getContainerSizes()) {
                        end += size;
                        targets.add(end);
                    }
                }
                input.seek(next);
            }
            boundaries.add((long) bytes.length);
            Assert.assertTrue(boundaries.containsAll(targets), "Invalid branch or container boundary in SWF " + version);
        }
    }
}
