/*
 * Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.ActionRegisterUsage;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ActionRegisterUsageTest {

    @DataProvider(name = "registerReads")
    public Object[][] registerReads() {
        String store = "Push 1\nStoreRegister 0\nPop\n";
        String read = "Push register0\nTrace\n";
        String branch = "Push \"condition\"\nGetVariable\nIf other\n";
        return new Object[][]{
            {store + read, Arrays.asList(1)},
            {store + "Push register0, register0\nTrace\nTrace\n", Arrays.asList(2)},
            {store + branch + read + "Jump done\nother: " + store + "done: " + read, Arrays.asList(2, 1)},
            {store + branch + store + "Jump done\nother: " + store + "done: " + read, Arrays.asList(0, 1, 1)},
            {store + "loop: " + read + "Push \"again\"\nGetVariable\nIf loop\n", Arrays.asList(1)},
            {"loop: " + read + store + "Push \"again\"\nGetVariable\nIf loop\n", Arrays.asList(1)},
            {"loop: " + store + read + "Push \"again\"\nGetVariable\nIf loop\n", Arrays.asList(1)},
            {store + "Jump done\n" + read + "done:\n", Arrays.asList(0)},
            {store + "DefineFunction \"nested\", 0 {\n" + store + read + "}\n" + read, Arrays.asList(1, -1)},
            {store + "Push \"obj\"\nGetVariable\nWith {\n" + read + "}\n" + read, Arrays.asList(2)},
            {store + "Try \"e\" {\n" + read + "} Catch {\n" + read + "}\n" + read, Arrays.asList(3)},
            {"Push \"obj\"\nGetVariable\nWith {\n" + store + "}\n" + read, Arrays.asList(1)},
            {store + "Push \"obj\"\nGetVariable\nWith {\n" + store + "}\n" + read, Arrays.asList(0, 1)},
            {"Push \"obj\"\nGetVariable\nWith {\nPush \"inner\"\nGetVariable\nWith {\n" + store + "}\n}\n" + read, Arrays.asList(1)},
            {"Try \"e\" {\n" + store + "} Catch {\n" + read + "}\n" + read, Arrays.asList(1)},
            {store + "Try {\nPush undefined\nReturn\n} Finally {\n" + read + "}\n", Arrays.asList(1)},
            {store + "Try {\nJump done\n} Finally {\n" + read + "}\ndone: " + read, Arrays.asList(2)},
            {store + "Try {\nPush \"failure\"\nThrow\n} Finally {\n" + read + "}\n" + read, Arrays.asList(2)},
            {store + "Try {\nTry {\nPush undefined\nReturn\n} Finally {\n" + read + "}\n} Finally {\n" + read + "}\n", Arrays.asList(2)},
            {store + "Try \"outer\" {\nTry {\nPush \"failure\"\nThrow\n} Finally {\n" + read + "}\n} Catch {\n" + read + "}\n", Arrays.asList(2)},
            {store + "Try register0 {\nPush \"failure\"\nThrow\n} Catch {\n" + read + "}\n", Arrays.asList(0)},
            {store + "Try {\nPush undefined\nReturn\n} Finally {\n" + store + read + "}\n", Arrays.asList(0, 1)},
            {store + "Try \"outer\" {\nTry \"inner\" {\nPush \"failure\"\nThrow\n} Catch {\nPush \"another\"\nThrow\n} Finally {\n" + read + "}\n} Catch {\n" + read + "}\n", Arrays.asList(2)},
            {store + "Try \"e\" {\nPush true\nIf tryEnd\n" + store + "tryEnd:\n} Catch {\n" + read + "} Finally {\n" + read + "}\n", Arrays.asList(1, 1)},
            {"loop: Try {\n" + store + "Jump loop\n} Finally {\n" + read + "}\n", Arrays.asList(1)},
            {"Try register0 {\n" + store + "Push \"failure\"\nThrow\n} Catch {\n" + read + "} Finally {\n" + read + "}\n" + read, Arrays.asList(0)},
            {store + "Push 2\nStoreRegister 1\nPop\n" + read, Arrays.asList(1, 0)}
        };
    }

    @Test(dataProvider = "registerReads")
    public void testReadsPerWrite(String pcode, List<Integer> expected) throws Exception {
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        ActionGraphSource code = new ActionGraphSource("", false, actions, swf.version,
                new HashMap<>(), new HashMap<>(), new HashMap<>(), swf.getCharset(), 0);
        ActionRegisterUsage usage = new ActionRegisterUsage(code, 0);
        List<Integer> actual = new ArrayList<>();
        for (Action action : actions) {
            if (action instanceof ActionStoreRegister) {
                List<GraphSourceItemPos> reads = usage.getReads(action);
                actual.add(reads == null ? -1 : reads.size());
            }
        }
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testWithSectionDoesNotAssumeItsRegisterDiesAtTheEnd() throws Exception {
        SWF swf = new SWF();
        String pcode = "Push \"obj\"\nGetVariable\nWith {\nPush 1\nStoreRegister 0\nPop\n}\nPush register0\nTrace\n";
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        int start = 3;
        int end = 6;
        ActionGraphSource code = new ActionGraphSource("", false, actions.subList(0, end), swf.version,
                new HashMap<>(), new HashMap<>(), new HashMap<>(), swf.getCharset(), start);
        Assert.assertNull(new ActionRegisterUsage(code, start).getReads(actions.get(4)));
    }

    @Test
    public void testCatchRegisterIsAnImplicitWrite() throws Exception {
        SWF swf = new SWF();
        String pcode = "Try register0 {\nPush \"failure\"\nThrow\n} Catch {\nPush register0\nTrace\n}\nPush register0\nTrace\n";
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        ActionGraphSource code = new ActionGraphSource("", false, actions, swf.version,
                new HashMap<>(), new HashMap<>(), new HashMap<>(), swf.getCharset(), 0);
        Assert.assertEquals(new ActionRegisterUsage(code, 0).getReads(actions.get(0)).size(), 2);
    }
}
