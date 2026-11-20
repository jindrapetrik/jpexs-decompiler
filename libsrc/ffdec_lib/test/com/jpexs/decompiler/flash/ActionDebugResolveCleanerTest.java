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
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import java.util.HashMap;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ActionDebugResolveCleanerTest {

    private static final String DELETE = "Push \"_global\"\nGetVariable\nPush \"__resolve\"\nDelete\nPop\n";

    private static final String RESTORE = "Push \"_global\"\nGetVariable\nPush \"__resolve\", \"_global\"\nGetVariable\n"
            + "Push \"__debugResolve\"\nGetMember\nSetMember\n";

    private ActionList parse(SWF swf, String pcode) throws Exception {
        ActionList actions = new ActionList(swf.getCharset(), swf.version);
        actions.addAll(ASMParser.parse(0, true, pcode + "\nEnd\n", swf.version, false, swf.getCharset(), swf.getSwfProfile()));
        return actions;
    }

    private String source(SWF swf, ActionList actions, boolean remove, boolean autoDeobfuscate) throws Exception {
        boolean oldRemove = Configuration.deobfuscateAs12RemoveGlobalDebugResolve.get();
        boolean oldAuto = Configuration.autoDeobfuscate.get();
        Configuration.deobfuscateAs12RemoveGlobalDebugResolve.set(remove);
        Configuration.autoDeobfuscate.set(autoDeobfuscate);
        try {
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(new HashMap<>(), new DoActionTag(swf), actions, "", writer, swf.getCharset());
            writer.finishHilights();
            String result = writer.toString().trim().replace("\r\n", "\n");
            Assert.assertFalse(result.contains("Decompilation error"), result);
            return result;
        } finally {
            Configuration.deobfuscateAs12RemoveGlobalDebugResolve.set(oldRemove);
            Configuration.autoDeobfuscate.set(oldAuto);
        }
    }

    @DataProvider(name = "wrapperFormats")
    public Object[][] wrapperFormats() {
        return new Object[][]{{false, false}, {false, true}, {true, false}, {true, true}};
    }

    @Test(dataProvider = "wrapperFormats")
    public void testTogglePreservesOriginalActions(boolean pooled, boolean autoDeobfuscate) throws Exception {
        SWF swf = new SWF();
        String pool = pooled ? "ConstantPool \"_global\", \"__resolve\", \"__debugResolve\", \"p\", \"lockedIndex\"\n" : "";
        ActionList actions = parse(swf, pool + DELETE + "Push undefined, \"p\"\nGetVariable\n"
                + "Push \"lockedIndex\"\nGetMember\nEquals2\n" + RESTORE + "Trace\n");
        byte[] original = Action.actionsToBytes(actions, true, swf.version);
        Assert.assertEquals(source(swf, actions, true, autoDeobfuscate), "trace(undefined == p.lockedIndex);");
        Assert.assertEquals(Action.actionsToBytes(actions, true, swf.version), original);
        String disabled = source(swf, actions, false, autoDeobfuscate);
        Assert.assertTrue(disabled.contains("delete _global.__resolve;"), disabled);
        Assert.assertTrue(disabled.contains("_global.__resolve = _global.__debugResolve;"), disabled);
        Assert.assertEquals(Action.actionsToBytes(actions, true, swf.version), original);
        Assert.assertEquals(source(swf, actions, true, autoDeobfuscate), "trace(undefined == p.lockedIndex);");
    }

    @Test
    public void testNestedWrappersAndCalls() throws Exception {
        SWF swf = new SWF();
        ActionList actions = parse(swf, DELETE + DELETE + "Push 0, \"read\"\nCallFunction\n" + RESTORE + RESTORE + "Trace\n");
        Assert.assertEquals(source(swf, actions, true, false), "trace(read());");
    }

    @DataProvider(name = "containerWrappers")
    public Object[][] containerWrappers() {
        return new Object[][]{
            {"DefineFunction \"f\", 0 {\n", "}\n"},
            {"DefineFunction2 \"f\", 0, 1, false, false, false, false, false, false, false, false, false {\n", "}\n"},
            {"Push \"scope\"\nGetVariable\nWith {\n", "}\n"},
            {"Try \"e\" {\n", "}\nCatch {\nPush \"error\"\nTrace\n}\nFinally {\nPush \"done\"\nTrace\n}\n"}
        };
    }

    @Test(dataProvider = "containerWrappers")
    public void testWrappersInsideContainers(String prefix, String suffix) throws Exception {
        SWF swf = new SWF();
        ActionList actions = parse(swf, prefix + DELETE + "Push \"kept\"\n" + RESTORE + "Trace\n" + suffix);
        String result = source(swf, actions, true, false);
        Assert.assertTrue(result.contains("trace(\"kept\");"), result);
        Assert.assertFalse(result.contains("__resolve") || result.contains("__debugResolve"), result);
    }

    @DataProvider(name = "unmatchedWrappers")
    public Object[][] unmatchedWrappers() {
        return new Object[][]{
            {DELETE + "Push \"kept\"\nTrace\n"},
            {RESTORE + "Push \"kept\"\nTrace\n"},
            {DELETE.replace("\"_global\"", "\"other\"") + "Push \"kept\"\n" + RESTORE + "Trace\n"},
            {DELETE + "Push \"kept\"\n" + RESTORE.replace("\"__debugResolve\"", "\"other\"") + "Trace\n"},
            {DELETE.replace("Pop\n", "Trace\n") + "Push \"kept\"\n" + RESTORE + "Trace\n"},
            {DELETE + "Jump inside\ninside: Push \"kept\"\n" + RESTORE + "Trace\n"},
            {"Push \"condition\"\nGetVariable\nIf inside\n" + DELETE + "inside: Push \"kept\"\n" + RESTORE + "Trace\n"},
            {"Push \"condition\"\nGetVariable\nIf restore\n" + DELETE + "Push \"kept\"\nTrace\nrestore: " + RESTORE},
            {"DefineFunction \"f\", 0 {\n" + DELETE + "}\n" + RESTORE},
            {DELETE + "DefineFunction \"f\", 0 {\n" + RESTORE + "}\n"}
        };
    }

    @Test(dataProvider = "unmatchedWrappers")
    public void testDoesNotRemoveUnmatchedOrSharedCode(String pcode) throws Exception {
        SWF swf = new SWF();
        ActionList actions = parse(swf, pcode);
        Assert.assertEquals(source(swf, actions, true, false), source(swf, actions, false, false));
    }
}
