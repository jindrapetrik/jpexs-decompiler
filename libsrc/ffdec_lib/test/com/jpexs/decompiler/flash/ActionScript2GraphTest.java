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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.ActionScriptObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.special.ActionUnknown;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionTrace;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * ActionScript 1/2 graph decompilation tests.
 *
 * @author JPEXS
 */
public class ActionScript2GraphTest {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
    }
    
    private String compileAndDecompile(String source) throws Exception {
        SWF swf = new SWF();
        DoActionTag tag = new DoActionTag(swf);
        ActionScript2Parser parser = new ActionScript2Parser(swf, tag);
        List<Action> compiledActions = parser.actionsFromString(source, Utf8Helper.charsetName);
        return decompile(swf, tag, compiledActions);
    }

    private String decompile(SWF swf, ASMSource tag, List<Action> compiledActions) throws Exception {
        return decompile(swf, tag, compiledActions, 1);
    }

    private String decompile(SWF swf, ASMSource tag, List<Action> compiledActions, int deobfuscationMode) throws Exception {
        byte[] actionBytes = Action.actionsToBytes(compiledActions, true, swf.version);
        ActionList actions = ActionListReader.readActionListTimeout(tag, new ArrayList<>(),
                new SWFInputStream(swf, actionBytes), swf.version, 0, actionBytes.length, "", deobfuscationMode);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), tag, actions, "", writer, swf.getCharset());
        writer.finishHilights();
        return writer.toString().trim().replace("\r\n", "\n");
    }

    private String duplicatedComparisonChain(String value, boolean reverse, boolean equal, boolean fallback, boolean assignments) {
        StringBuilder pcode = new StringBuilder(value);
        for (int i = 1; i <= 3; i++) {
            pcode.append("PushDuplicate\nPush ").append(i).append('\n');
            if (reverse) {
                pcode.append("StackSwap\n");
            }
            pcode.append("Equals2\n");
            if (equal) {
                pcode.append("Not\n");
            }
            pcode.append("If next").append(i).append('\n');
            if (assignments) {
                pcode.append("Push \"result\", ").append(i).append("\nSetVariable\n");
            } else {
                pcode.append("Push 0, \"handle").append(i).append("\"\nCallFunction\nPop\n");
            }
            pcode.append("Jump done\nnext").append(i).append(":\n");
        }
        if (fallback) {
            pcode.append(assignments ? "Push \"result\", 0\nSetVariable\n" : "Push 0, \"fallback\"\nCallFunction\nPop\n");
        }
        return pcode.append("done: Pop\n").toString();
    }

    @DataProvider(name = "duplicatedComparisonChains")
    public Object[][] duplicatedComparisonChains() {
        return new Object[][]{{false, true, false}, {false, true, true}, {true, true, true}, {false, false, true}, {true, false, false}};
    }

    @Test(dataProvider = "duplicatedComparisonChains")
    public void testDuplicatedLooseComparisonChain(boolean reverse, boolean equal, boolean fallback) throws Exception {
        String result = decompilePcode(duplicatedComparisonChain("Push \"value\"\nGetVariable\n", reverse, equal, fallback, false));
        StringBuilder expected = new StringBuilder("var _temp_1 = value;\n");
        for (int i = 1; i <= 3; i++) {
            if (i > 1) {
                expected.append("else ");
            }
            expected.append("if(").append(reverse ? i : "_temp_1").append(equal ? " == " : " != ")
                    .append(reverse ? "_temp_1" : i).append(")\n{\n   handle").append(i).append("();\n}\n");
        }
        if (fallback) {
            expected.append("else\n{\n   fallback();\n}\n");
        }
        Assert.assertEquals(result, expected.toString().trim());
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    private Object runComparisonAssignments(List<Action> actions, Object value) {
        return runComparisonAssignments(actions, value, null);
    }

    private Object runComparisonAssignments(List<Action> actions, Object value, Object y) {
        Action.setActionsAddresses(actions, 0);
        LocalDataArea local = new LocalDataArea(null);
        local.localVariables.put("value", value);
        local.localVariables.put("y", y);
        int steps = 0;
        for (int ip = 0; ip < actions.size();) {
            Assert.assertTrue(++steps < 1000, "Comparison chain did not terminate");
            Action action = actions.get(ip);
            if (action.isExit()) {
                break;
            }
            local.jump = null;
            if (action instanceof ActionStoreRegister) {
                // AVM1 StoreRegister copies the top value without popping it.
                local.localRegisters.put(((ActionStoreRegister) action).registerNumber, local.stack.peek());
            } else if (action instanceof ActionGetMember) {
                String name = local.popAsString();
                ActionScriptObject object = (ActionScriptObject) local.pop();
                local.push(object.getOwnMembers().get(name));
            } else if (action instanceof ActionSetMember) {
                Object assignedValue = local.pop();
                String name = local.popAsString();
                ActionScriptObject object = (ActionScriptObject) local.pop();
                object.setOwnMember(name, assignedValue);
            } else {
                Assert.assertTrue(action.execute(local), action.toString());
            }
            ip = local.jump == null ? ip + 1 : Action.adr2ip(actions, local.jump);
        }
        Assert.assertTrue(local.stack.isEmpty(), "Comparison chain did not discard its saved value");
        return local.localVariables.get("result");
    }

    @Test
    public void testDuplicatedComparisonEvaluatesInputOnce() throws Exception {
        String result = decompilePcode(duplicatedComparisonChain("Push 0, \"read\"\nCallFunction\n", false, true, true, false));
        Assert.assertFalse(result.contains("§§") || result.contains("switch("), result);
        Assert.assertEquals(result.split("read\\(\\)", -1).length - 1, 1, result);
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    @DataProvider(name = "looseComparisonValues")
    public Object[][] looseComparisonValues() {
        return new Object[][]{{"1", 1L}, {1L, 1L}, {true, 1L}, {"2", 2L}, {2.0, 2L}, {"3", 3L}, {false, 0L}, {"other", 0L}};
    }

    @Test(dataProvider = "looseComparisonValues")
    public void testDuplicatedComparisonPreservesLooseEquality(Object value, Object expected) throws Exception {
        SWF swf = new SWF();
        String pcode = duplicatedComparisonChain("Push \"value\"\nGetVariable\n", false, true, true, true);
        List<Action> original = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), original);
        Assert.assertFalse(result.contains("§§") || result.contains("switch("), result);
        List<Action> compiled = new ActionScript2Parser(swf, new DoActionTag(swf)).actionsFromString(result, Utf8Helper.charsetName);
        Assert.assertEquals(runComparisonAssignments(original, value), expected);
        Assert.assertEquals(runComparisonAssignments(compiled, value), expected);
    }

    @Test
    public void testComparisonCaseConsumingSavedValueIsPreserved() throws Exception {
        String pcode = duplicatedComparisonChain("Push 0, \"read\"\nCallFunction\n", false, true, false, false)
                .replace("Push 0, \"handle1\"", "Push 1, \"consume\"\nCallFunction\nPop\nPush 0, \"handle1\"");
        String result = decompilePcode(pcode);
        Assert.assertTrue(result.contains("consume(§§pop())"), result);
        Assert.assertTrue(result.contains("handle1();"), result);
    }

    private String copiedEnumerationLoop(String enumerate, boolean nullFirst, String body, String label) {
        return enumerate + label + "Loop: StoreRegister 0\n"
                + (nullFirst ? "Push null, register0\n" : "Push register0, null\n")
                + "Equals2\nIf " + label + "Drain\nPop\nPush \"key\", register0\nSetVariable\n"
                + body + "Jump " + label + "Loop\n"
                + label + "Drain: Push null\n" + (nullFirst ? "StackSwap\n" : "")
                + "Equals2\nIf " + label + "Done\nJump " + label + "Drain\n" + label + "Done:\n";
    }

    @DataProvider(name = "copiedEnumerationKeys")
    public Object[][] copiedEnumerationKeys() {
        return new Object[][]{
            {"Push \"obj\"\nGetVariable\nEnumerate2\n", false},
            {"Push \"obj\"\nGetVariable\nEnumerate2\n", true},
            {"Push \"obj\"\nEnumerate\n", false},
            {"Push \"obj\"\nEnumerate\n", true}
        };
    }

    private String decompilePcode(String pcode) throws Exception {
        return decompilePcode(pcode, SWF.DEFAULT_VERSION);
    }

    @Test
    public void testForInSwitchCaseFallsThroughToSharedReturn() throws Exception {
        String result = decompilePcode("DefineFunction \"sample\", 0 {\nPush register1\n"
                + "Push \"kind\"\nGetVariable\nStoreRegister 0\nPush \"string\"\nStrictEquals\nIf emptyCase\n"
                + "Push register0, \"object\"\nStrictEquals\nIf loopCase\n"
                + "Push register0, \"function\"\nStrictEquals\nIf done\nJump done\n"
                + "emptyCase: Jump done\nloopCase: Push \"obj\"\nGetVariable\nEnumerate2\n"
                + "nextKey: StoreRegister 0\nPush null\nEquals2\nIf done\n"
                + "Push \"key\", register0\nSetVariable\nPush \"key\"\nGetVariable\nTrace\nJump nextKey\n"
                + "done: StoreRegister 1\nPop\n}\n");
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertTrue(result.contains("for(key in obj)"), result);
        Assert.assertTrue(result.contains("trace(key);"), result);
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    private String decompilePcode(String pcode, int version) throws Exception {
        SWF swf = new SWF();
        swf.version = version;
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        return decompile(swf, new DoActionTag(swf), actions);
    }

    @DataProvider(name = "enumeratedObjects")
    public Object[][] enumeratedObjects() {
        return new Object[][]{
            {"Push \"parent\"\nEnumerate\n", "parent"},
            {"Push \"objectName\"\nGetVariable\nEnumerate\n", "eval(objectName)"},
            {"Push \"parent\"\nGetVariable\nEnumerate2\n", "parent"},
            {"Push \"parent\"\nEnumerate2\n", "\"parent\""}
        };
    }

    @Test(dataProvider = "enumeratedObjects")
    public void testForInResolvesOnlyEnumerateVariableNames(String enumeration, String object) throws Exception {
        for (boolean namedKey : new boolean[]{false, true}) {
            String pcode = enumeration + "nextKey: StoreRegister 0\nPush null\nEquals2\nIf done\n"
                    + (namedKey ? "Push \"key\", register0\nDefineLocal\nPush \"key\"\nGetVariable\n" : "Push register0\n")
                    + "Trace\nJump nextKey\ndone:\n";
            for (int mode : new int[]{0, 1}) {
                SWF swf = new SWF();
                List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
                String result = decompile(swf, new DoActionTag(swf), actions, mode);
                Assert.assertTrue(result.contains(" in " + object + ")"), result);
                Assert.assertFalse(result.contains("§§"), result);
            }
        }
    }

    @DataProvider(name = "conditionalSharedTailPaths")
    public Object[][] conditionalSharedTailPaths() {
        Object[][] paths = new Object[][]{{false, -1L}, {false, 0L}, {false, 1L}, {false, 2L},
            {true, -1L}, {true, 0L}, {true, 1L}, {true, 2L},
            {true, Double.NaN}, {true, "invalid"}, {false, Double.NaN}};
        Object[][] result = new Object[paths.length * 2][];
        for (int i = 0; i < paths.length; i++) {
            result[2 * i] = new Object[]{paths[i][0], paths[i][1], false};
            result[2 * i + 1] = new Object[]{paths[i][0], paths[i][1], true};
        }
        return result;
    }

    private LocalDataArea runConditionalSharedTail(List<Action> actions, boolean initialized, Object counter) {
        Action.setActionsAddresses(actions, 0);
        LocalDataArea local = new LocalDataArea(null);
        local.localVariables.put("initialized", initialized);
        local.localVariables.put("counter", counter);
        int steps = 0;
        for (int ip = 0; ip < actions.size();) {
            Assert.assertTrue(++steps < 1000, "Shared-tail block did not terminate");
            Action action = actions.get(ip);
            if (action.isExit()) {
                break;
            }
            local.jump = null;
            Assert.assertTrue(action.execute(local), action.toString());
            ip = local.jump == null ? ip + 1 : Action.adr2ip(actions, local.jump);
        }
        Assert.assertTrue(local.stack.isEmpty(), "Shared-tail block left values on the stack");
        return local;
    }

    @Test(dataProvider = "conditionalSharedTailPaths")
    public void testConditionalSharedTailPreservesEveryPathWithoutDuplication(boolean initialized, Object counter, boolean splitStack) throws Exception {
        String sharedTail = (splitStack ? "loadCount: GetVariable\nJump incrementCount\nincrementCount: " : "GetVariable\n")
                + "Increment\nSetVariable\nPush \"result\", \"SHARED\"\nSetVariable\nJump after\n";
        String pcode = "Push \"sharedCount\", 0, \"result\", \"SKIPPED\"\nSetVariable\nSetVariable\nJump test\n"
                + "shared: Push \"sharedCount\", \"sharedCount\"\n" + (splitStack ? "Jump loadCount\n" : sharedTail)
                + "test: Push \"initialized\"\nGetVariable\nIf already\n"
                + "Push \"counter\", 0\nSetVariable\nPush \"initialized\", true\nSetVariable\nJump shared\n"
                + "already: Push \"counter\"\nGetVariable\nPush 1\nGreater\nIf after\nJump shared\n"
                + (splitStack ? sharedTail : "")
                + "after: Push \"counter\", \"counter\"\nGetVariable\nIncrement\nSetVariable\n";
        SWF swf = new SWF();
        List<Action> original = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), original, 0);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertTrue(result.contains("while(true)") && result.contains("break;"), result);
        Assert.assertEquals(result.split("\"SHARED\"", -1).length - 1, 1, result);
        List<Action> compiled = new ActionScript2Parser(swf, new DoActionTag(swf)).actionsFromString(result, Utf8Helper.charsetName);
        LocalDataArea before = runConditionalSharedTail(original, initialized, counter);
        LocalDataArea after = runConditionalSharedTail(compiled, initialized, counter);
        Assert.assertEquals(after.localVariables, before.localVariables);
    }

    @Test
    public void testTellTargetWithRuntimeRegisterValue() throws Exception {
        String result = decompilePcode("Push \"_root\"\nGetVariable\nStoreRegister 1\nPop\n"
                + "Push register1\nSetTarget2\nGotoFrame 1\nSetTarget \"\"\nPush \"after\"\nTrace\n");
        Assert.assertTrue(result.contains("tellTarget(_loc1_)"), result);
        Assert.assertTrue(result.contains("_root"), result);
        Assert.assertTrue(result.contains("gotoAndStop(2)"), result);
        Assert.assertTrue(result.contains("trace(\"after\")"), result);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    @DataProvider(name = "overlappingFunctionTails")
    public Object[][] overlappingFunctionTails() {
        return new Object[][]{{false}, {true}};
    }

    @Test(dataProvider = "overlappingFunctionTails", timeOut = 10000)
    public void testBackwardJumpRedeclaringFunctionRemainsExternal(boolean function2) throws Exception {
        int timeout = Configuration.decompilationTimeoutSingleMethod.get();
        Configuration.decompilationTimeoutSingleMethod.set(2);
        try {
            String definition = function2
                    ? "DefineFunction2 \"sample\", 0, 1, false, false, false, false, false, false, false, false, false {\n"
                    : "DefineFunction \"sample\", 0 {\n";
            String result = decompilePcode("header: " + definition
                    + "Push \"condition\"\nGetVariable\nIf header\n"
                    + "Push \"body\"\nTrace\n}\nPush \"after\"\nTrace\nEnd\n");
            Assert.assertEquals(result.split("function sample\\(", -1).length - 1, 1, result);
            Assert.assertTrue(result.contains("§§goto(addr0000);"), result);
            Assert.assertTrue(result.contains("addr0000:"), result);
            Assert.assertTrue(result.contains("trace(\"body\");") && result.contains("trace(\"after\");"), result);
            Assert.assertFalse(result.contains("Decompilation error"), result);
        } finally {
            Configuration.decompilationTimeoutSingleMethod.set(timeout);
        }
    }

    @Test(dataProvider = "overlappingFunctionTails")
    public void testBackwardJumpBeforeFunctionWithoutRedeclaration(boolean function2) throws Exception {
        String definition = function2
                ? "DefineFunction2 \"sample\", 0, 1, false, false, false, false, false, false, false, false, false {\n"
                : "DefineFunction \"sample\", 0 {\n";
        String result = decompilePcode("Jump header\nshared: Push \"prefix\"\nTrace\nPush undefined\nReturn\n"
                + "header: " + definition + "Jump shared\n}\nPush \"after\"\nTrace\nEnd\n");
        Assert.assertTrue(result.contains("function sample()"), result);
        Assert.assertTrue(result.contains("trace(\"prefix\");") && result.contains("trace(\"after\");"), result);
        Assert.assertFalse(result.contains("§§"), result);
    }

    @Test(dataProvider = "overlappingFunctionTails")
    public void testOverlappingDeadActionKeepsFunctionAssignment(boolean function2) throws Exception {
        SWF swf = new SWF();
        String definition = function2
                ? "DefineFunction2 \"\", 0, 1, false, false, false, false, false, false, false, false, false {\n"
                : "DefineFunction \"\", 0 {\n";
        List<Action> compiled = ASMParser.parse(0, true,
                "Push \"obj\"\nGetVariable\nPush \"method\"\n" + definition
                + "Push false\nIf garbage\nPush undefined\nReturn\ngarbage: Unknown_c0\n}\n"
                + "SetMember\nPush \"after\"\nTrace\n", swf.version, false, swf.getCharset(), swf.getSwfProfile());
        byte[] bytes = Action.actionsToBytes(compiled, true, swf.version);
        int scriptLength = bytes.length;
        // The unreachable action claims data extending across the function's
        // declared end and the SetMember that assigns the function.
        int unknownOffset = (int) compiled.stream().filter(action -> action instanceof ActionUnknown)
                .findFirst().get().getAddress();
        bytes = java.util.Arrays.copyOf(bytes, bytes.length + 64);
        bytes[unknownOffset + 1] = 64;
        DoActionTag tag = new DoActionTag(swf);
        ActionList actions = ActionListReader.readActionListTimeout(tag, new ArrayList<>(),
                new SWFInputStream(swf, bytes), swf.version, 0, scriptLength, "", 1);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), tag, actions, "", writer, swf.getCharset());
        writer.finishHilights();
        String result = writer.toString();
        Assert.assertTrue(result.contains("obj.method = function()"), result);
        Assert.assertTrue(result.contains("trace(\"after\")"), result);
        Assert.assertFalse(result.contains("§§"), result);
    }

    @DataProvider(name = "waitForFrameNumbers")
    public Object[][] waitForFrameNumbers() {
        return new Object[][]{{0}, {22}, {65535}};
    }

    @Test(dataProvider = "waitForFrameNumbers")
    public void testWaitForFrameUsesOneBasedNumber(int frame) throws Exception {
        String result = decompilePcode("WaitForFrame " + frame + ", 2\nGotoFrame 22\nPlay\n");
        Assert.assertEquals(result, "ifFrameLoaded(" + (frame + 1) + ")\n{\n   gotoAndPlay(23);\n}");
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    @DataProvider(name = "frameLoadedChecks")
    public Object[][] frameLoadedChecks() {
        return new Object[][]{{"WaitForFrame 22, 2\n"}, {"Push 23\nWaitForFrame2 2\n"}};
    }

    @Test(dataProvider = "frameLoadedChecks")
    public void testFrameLoadedBooleanBranch(String wait) throws Exception {
        String result = decompilePcode("Push false\n" + wait
                + "Pop\nPush true\nNot\nIf done\nGotoFrame 22\nPlay\ndone:\n");
        Assert.assertEquals(result, "ifFrameLoaded(23)\n{\n   gotoAndPlay(23);\n}");
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    @DataProvider(name = "frameLoadedLiteralValues")
    public Object[][] frameLoadedLiteralValues() {
        return new Object[][]{
            {4, "\"0\"", "\"1\"", true},
            {4, "0", "1", true},
            {4, "\"0\"", "\"-2\"", true},
            {5, "0", "1", true},
            {5, "\"\"", "\"1\"", true},
            {5, "\"0\"", "\"1\"", false},
            {4, "\"0\"", "\"0\"", false},
            {5, "0", "0", false}
        };
    }

    @Test(dataProvider = "frameLoadedLiteralValues")
    public void testFrameLoadedLiteralBranchRespectsSwfVersion(int version, String unloaded, String loaded, boolean fold) throws Exception {
        String result = decompilePcode("Push " + unloaded + "\nWaitForFrame 22, 2\nPop\nPush " + loaded
                + "\nNot\nIf done\nGotoFrame 22\nPlay\ndone:\n", version);
        if (fold) {
            Assert.assertEquals(result, "ifFrameLoaded(23)\n{\n   gotoAndPlay(23);\n}");
            Assert.assertEquals(compileAndDecompile(result), result);
        } else {
            Assert.assertTrue(result.contains("§§"), result);
            Assert.assertTrue(result.contains("gotoAndPlay(23)"), result);
        }
    }

    @Test
    public void testFrameLoadedBooleanBranchKeepsFollowingCommands() throws Exception {
        String result = decompilePcode("Push false\nWaitForFrame 22, 2\nPop\nPush true\n"
                + "Not\nIf done\nGotoFrame 22\nPlay\ndone:\nPush \"after\"\nTrace\n");
        Assert.assertEquals(result, "ifFrameLoaded(23)\n{\n   gotoAndPlay(23);\n}\ntrace(\"after\");");
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    @Test
    public void testOtherFrameLoadedStackValuesAreNotFolded() throws Exception {
        String result = decompilePcode("Push false\nWaitForFrame 22, 2\nPop\nPush false\n"
                + "Not\nIf done\nGotoFrame 22\nPlay\ndone:\n");
        Assert.assertTrue(result.contains("§§"), result);
        Assert.assertTrue(result.contains("gotoAndPlay(23)"), result);
    }

    private String decompileClassWithSavedStackValue(boolean consumeValue) throws Exception {
        SWF swf = new SWF();
        DoInitActionTag tag = new DoInitActionTag(swf);
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        swf.addTag(sprite);
        tag.spriteId = sprite.spriteId;
        ExportAssetsTag exports = new ExportAssetsTag(swf);
        exports.tags.add(sprite.spriteId);
        exports.names.add(SWF.AS2_PKG_PREFIX + "Test");
        swf.addTag(exports);
        swf.addTag(tag);
        List<Action> actions = new ActionScript2Parser(swf, tag).actionsFromString(
                "class Test { function Test() {} static function run() { trace(\"method\"); } }", Utf8Helper.charsetName);
        actions.add(0, new ActionPush(new Object[]{false}, swf.getCharset()));
        if (consumeValue) {
            actions.add(new ActionTrace());
        }
        return decompile(swf, tag, actions);
    }

    @DataProvider(name = "classInitializerScopes")
    public Object[][] classInitializerScopes() {
        return new Object[][]{{"", "cleanup"}, {"", "_temp_1"}, {"this", "cleanup"}, {"arguments", "cleanup"}};
    }

    @Test(dataProvider = "classInitializerScopes")
    public void testClassFieldInitializerWithSetupAndCleanup(String setupArgument, String cleanup) throws Exception {
        SWF swf = new SWF();
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        swf.addTag(sprite);
        ExportAssetsTag exports = new ExportAssetsTag(swf);
        exports.tags.add(sprite.spriteId);
        exports.names.add(SWF.AS2_PKG_PREFIX + "Test");
        swf.addTag(exports);
        DoInitActionTag tag = new DoInitActionTag(swf);
        tag.spriteId = sprite.spriteId;
        swf.addTag(tag);
        List<Action> actions = ASMParser.parse(0, true,
                "Push \"_global\"\nGetVariable\nPush \"Test\"\nGetMember\nNot\nNot\nIf done\n"
                + "Push \"_global\"\nGetVariable\nPush \"Test\"\nDefineFunction \"\", 0 {\n}\n"
                + "StoreRegister 1\nSetMember\nPush register1, \"prototype\"\nGetMember\nStoreRegister 2\nPop\n"
                + "Push register1, \"value\"\n"
                + (setupArgument.isEmpty() ? "Push 0\n" : "Push \"" + setupArgument + "\"\nGetVariable\nPush 1\n")
                + "Push \"prepare\"\nCallFunction\nPop\n"
                + "Push 0, \"read\"\nCallFunction\nPush 0, \"" + cleanup + "\"\nCallFunction\nPop\nSetMember\n"
                + "Push 1, null, register2, 3, \"ASSetPropFlags\"\nCallFunction\nPop\ndone:\n",
                swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, tag, actions);
        if (setupArgument.equals("arguments")) {
            Assert.assertFalse(result.contains("class Test"), result);
            Assert.assertTrue(result.contains("prepare(arguments)"), result);
            return;
        }
        Assert.assertTrue(result.contains("class Test"), result);
        Assert.assertTrue(result.contains("static var value"), result);
        Assert.assertFalse(result.contains("§§"), result);
        for (String name : new String[]{"prepare", "read", cleanup}) {
            Assert.assertEquals(result.split(name + "\\(" + (name.equals("prepare") ? setupArgument : "") + "\\)", -1).length - 1, 1, result);
        }
        Assert.assertTrue(result.indexOf("prepare(") < result.indexOf("read()"), result);
        Assert.assertTrue(result.indexOf("read()") < result.indexOf(cleanup + "()"), result);
        Assert.assertTrue(result.matches("(?s).*var (_temp_[0-9]+_*) = read\\(\\);.*" + cleanup + "\\(\\);.*return \\1;.*"), result);
        Assert.assertTrue(result.contains(".call(this)"), result);
        new ActionScript2Parser(swf, tag).actionsFromString(result, Utf8Helper.charsetName);
    }

    @Test
    public void testUnpoppedValueAfterClassDetection() throws Exception {
        String result = decompileClassWithSavedStackValue(false);
        Assert.assertTrue(result.contains("class Test"), result);
        Assert.assertTrue(result.contains("false; //unpopped"), result);
        Assert.assertFalse(result.contains("§§"), result);
    }

    @Test
    public void testConsumedValueAfterClassDetectionStaysOnStack() throws Exception {
        String result = decompileClassWithSavedStackValue(true);
        Assert.assertTrue(result.contains("class Test"), result);
        Assert.assertTrue(result.contains("§§push(false)"), result);
        Assert.assertTrue(result.contains("trace(§§pop())"), result);
        Assert.assertFalse(result.contains("unpopped"), result);
    }

    @DataProvider(name = "functionsInLoops")
    public Object[][] functionsInLoops() {
        return new Object[][]{{false, false}, {true, false}, {false, true}, {true, true}};
    }

    @Test(dataProvider = "functionsInLoops")
    public void testFunctionAssignmentsStayInsideLoop(boolean function2, boolean pretest) throws Exception {
        String function = function2
                ? "DefineFunction2 \"\", 0, 1, false, false, false, false, false, false, false, false, false {\n"
                : "DefineFunction \"\", 0 {\n";
        String condition = "Push \"i\"\nGetVariable\nPush \"count\"\nGetVariable\nLess2\n";
        String pcode = "Push \"i\", 0\nDefineLocal\nloop:\n"
                + (pretest ? condition + "Not\nIf done\n" : "")
                + "Push \"first\"\nGetVariable\nPush \"onRelease\"\n" + function
                + "Push \"first callback\"\nTrace\n}\nSetMember\n"
                + "Push \"between\"\nTrace\nPush \"second\"\nGetVariable\nPush \"onRelease\"\n" + function
                + "Push \"second callback\"\nTrace\n}\nSetMember\n"
                + "Push \"i\", \"i\"\nGetVariable\nIncrement\nSetVariable\n"
                + (pretest ? "Jump loop\n" : condition + "If loop\n")
                + "done: Push \"after\"\nTrace\n";
        String expected = "var i = 0;\n" + (pretest ? "while(i < count)" : "do") + "\n{\n"
                + "   first.onRelease = function()\n   {\n      trace(\"first callback\");\n   };\n"
                + "   trace(\"between\");\n"
                + "   second.onRelease = function()\n   {\n      trace(\"second callback\");\n   };\n"
                + "   i++;\n}\n" + (pretest ? "" : "while(i < count);\n") + "trace(\"after\");";
        String result = decompilePcode(pcode);
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @DataProvider(name = "shortCircuitLoopConditions")
    public Object[][] shortCircuitLoopConditions() {
        return new Object[][]{{false, false}, {true, false}, {false, true}, {true, true}};
    }

    @Test(dataProvider = "shortCircuitLoopConditions")
    public void testDoWhileKeepsConditionEvaluatedBeforePrecontinue(boolean and, boolean nested) throws Exception {
        String branch = "PushDuplicate\n" + (and ? "Not\n" : "") + "If join\nPop\n";
        String pcode = "start: Push \"value\", 0, \"next\"\nCallFunction\nSetVariable\n"
                + "Push 0, \"left\"\nCallFunction\n" + branch
                + (nested ? "Push 0, \"middle\"\nCallFunction\n" + branch : "")
                + "Push 0, \"right\"\nCallFunction\njoin: If start\nPush \"after\"\nTrace\n";
        String operator = and ? " && " : " || ";
        String condition = "left()" + operator + (nested ? "(middle()" + operator + "right())" : "right()");
        String expected = "do\n{\n   value = next();\n}\nwhile(" + condition + ");\ntrace(\"after\");";
        String result = decompilePcode(pcode);
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    private String countAlwaysBreakPath(String counter, String marker) {
        return "Push \"" + counter + "\", \"" + counter + "\"\nGetVariable\nPush 1\nAdd2\nSetVariable\n"
                + "Push \"marker\", \"" + marker + "\"\nSetVariable\n";
    }

    private String alwaysBreakWithExitPcode(String exit) {
        return "Push \"entry\"\nGetVariable\nNot\nIf shared\n"
                + "Push \"first\"\nGetVariable\nNot\nIf early\n"
                + "Push \"second\"\nGetVariable\nNot\nIf early\n"
                + countAlwaysBreakPath("selectedCounter", "SELECTED")
                + "Push \"call\"\nGetVariable\nNot\nIf leave\n"
                + countAlwaysBreakPath("calledCounter", "CALLED")
                + "leave: Jump shared\nearly: " + countAlwaysBreakPath("earlyCounter", "EARLY")
                + "Push 0\n" + exit + "\nshared: " + countAlwaysBreakPath("sharedCounter", "SHARED")
                + "Push 0\nReturn\n";
    }

    @DataProvider(name = "alwaysBreakExitPaths")
    public Object[][] alwaysBreakExitPaths() {
        List<Object[]> paths = new ArrayList<>();
        for (int bits = 0; bits < 16; bits++) {
            paths.add(new Object[]{(bits & 1) != 0, (bits & 2) != 0, (bits & 4) != 0, (bits & 8) != 0});
        }
        return paths.toArray(new Object[paths.size()][]);
    }

    private LocalDataArea runAlwaysBreakPath(List<Action> actions, boolean entry, boolean first, boolean second, boolean call) {
        Action.setActionsAddresses(actions, 0);
        LocalDataArea local = new LocalDataArea(null);
        local.localVariables.put("entry", entry);
        local.localVariables.put("first", first);
        local.localVariables.put("second", second);
        local.localVariables.put("call", call);
        for (String name : new String[]{"selectedCounter", "calledCounter", "earlyCounter", "sharedCounter"}) {
            local.localVariables.put(name, 0L);
        }
        int steps = 0;
        for (int ip = 0; ip < actions.size();) {
            Assert.assertTrue(++steps < 1000, "Always-break block did not terminate");
            Action action = actions.get(ip);
            local.jump = null;
            Assert.assertTrue(action.execute(local), action.toString());
            if (action.isExit()) {
                break;
            }
            ip = local.jump == null ? ip + 1 : Action.adr2ip(actions, local.jump);
        }
        Assert.assertTrue(local.stack.isEmpty(), "Always-break block left values on the stack");
        return local;
    }

    @Test(dataProvider = "alwaysBreakExitPaths")
    public void testAlwaysBreakWithSeparateReturnDoesNotDuplicateCode(boolean entry, boolean first, boolean second, boolean call) throws Exception {
        SWF swf = new SWF();
        List<Action> original = ASMParser.parse(0, true, alwaysBreakWithExitPcode("Return"), swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), original, 0);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertTrue(result.contains("while(true)") && result.contains("break;"), result);
        for (String marker : new String[]{"SELECTED", "CALLED", "EARLY", "SHARED"}) {
            Assert.assertEquals(result.split("\"" + marker + "\"", -1).length - 1, 1, result);
        }
        Assert.assertEquals(result.split("return 0;", -1).length - 1, 2, result);
        List<Action> compiled = new ActionScript2Parser(swf, new DoActionTag(swf)).actionsFromString(result, Utf8Helper.charsetName);
        LocalDataArea before = runAlwaysBreakPath(original, entry, first, second, call);
        LocalDataArea after = runAlwaysBreakPath(compiled, entry, first, second, call);
        Assert.assertEquals(after.returnValue, before.returnValue);
        Assert.assertEquals(after.localVariables, before.localVariables);
    }

    @Test
    public void testAlwaysBreakWithSeparateThrowDoesNotDuplicateCode() throws Exception {
        String result = decompilePcode(alwaysBreakWithExitPcode("Throw"));
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertTrue(result.contains("while(true)") && result.contains("break;"), result);
        Assert.assertEquals(result.split("\"SHARED\"", -1).length - 1, 1, result);
        Assert.assertEquals(result.split("throw 0;", -1).length - 1, 1, result);
    }

    private String absoluteValuePcode(String label) {
        return "Push \"x\"\nGetVariable\nPush \"0\"\nLess\nIf " + label + "Negative\n"
                + "Push \"x\"\nGetVariable\nJump " + label + "Done\n"
                + label + "Negative: Push \"0\", \"x\"\nGetVariable\nSubtract\n" + label + "Done:\n";
    }

    @DataProvider(name = "savedComparisonValues")
    public Object[][] savedComparisonValues() {
        List<Object[]> values = new ArrayList<>();
        for (long x : new long[]{-3, -1, 1, 3}) {
            for (long y : new long[]{-3, -1, 1, 3}) {
                values.add(new Object[]{x, y});
            }
        }
        return values.toArray(new Object[values.size()][]);
    }

    @Test(dataProvider = "savedComparisonValues")
    public void testSavedComparisonAcrossTernaryPreservesEvaluationOrder(long x, long y) throws Exception {
        String pcode = "Push \"result\", 0\nSetVariable\nPush \"x\", \"value\"\nGetVariable\nSetVariable\n"
                + absoluteValuePcode("first") + "Push \"2\"\nLess\n"
                + "Push \"x\", \"y\"\nGetVariable\nSetVariable\n"
                + absoluteValuePcode("second") + "Push \"2\"\nLess\nAnd\nNot\nIf done\n"
                + "Push \"result\", 1\nSetVariable\ndone:\n";
        SWF swf = new SWF();
        List<Action> original = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), original, 0);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertTrue(result.contains("var _temp_1 = (x >= \"0\" ? x : - x) < \"2\";"), result);
        Assert.assertTrue(result.indexOf("var _temp_1") < result.indexOf("x = y;"), result);
        List<Action> compiled = new ActionScript2Parser(swf, new DoActionTag(swf)).actionsFromString(result, Utf8Helper.charsetName);
        long expected = Math.abs(x) < 2 && Math.abs(y) < 2 ? 1 : 0;
        Assert.assertEquals(runComparisonAssignments(original, x, y), expected);
        Assert.assertEquals(runComparisonAssignments(compiled, x, y), expected);
    }

    @Test
    public void testMultipleSavedValuesAcrossTernary() throws Exception {
        String pcode = "Push \"value\"\nGetVariable\nPush \"value\"\nGetVariable\nPush 10\nAdd2\n"
                + "Push \"x\", \"y\"\nGetVariable\nSetVariable\n" + absoluteValuePcode("abs")
                + "Add2\nAdd2\nPush \"result\"\nStackSwap\nSetVariable\n";
        SWF swf = new SWF();
        List<Action> original = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), original, 0);
        Assert.assertFalse(result.contains("§§"), result);
        List<Action> compiled = new ActionScript2Parser(swf, new DoActionTag(swf)).actionsFromString(result, Utf8Helper.charsetName);
        Assert.assertEquals(runComparisonAssignments(original, 7L, -3L), 27.0);
        Assert.assertEquals(runComparisonAssignments(compiled, 7L, -3L), 27.0);
    }

    @Test
    public void testTernaryConsumingSavedValueDoesNotRecoverItAgain() throws Exception {
        String pcode = "Push \"a\"\nGetVariable\nPush \"b\"\nGetVariable\nPush \"x\", 0\nSetVariable\n"
                + "Push \"enabled\"\nGetVariable\nIf yes\nPush 1\nAdd2\nJump done\n"
                + "yes: Push 2\nAdd2\ndone: Add2\nTrace\n";
        String result = decompilePcode(pcode);
        Assert.assertTrue(result.contains("§§pop()"), result);
        Assert.assertFalse(result.contains("var _temp_"), result);
    }

    @Test(dataProvider = "copiedEnumerationKeys")
    public void testForInWithCopiedKeyAndDrain(String enumerate, boolean nullFirst) throws Exception {
        String pcode = copiedEnumerationLoop(enumerate, nullFirst, "Push \"key\"\nGetVariable\nTrace\n", "enum")
                + "Push \"after\"\nTrace\n";
        String expected = "for(key in obj)\n{\n   trace(key);\n}\ntrace(\"after\");";
        String result = decompilePcode(pcode);
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test
    public void testCopiedEnumerationInsideConditionalWith() throws Exception {
        String pcode = "Push \"scope\"\nGetVariable\nWith {\nPush \"enabled\"\nGetVariable\nNot\nIf outside\n"
                + copiedEnumerationLoop("Push \"obj\"\nGetVariable\nEnumerate2\n", false, "Push \"key\"\nGetVariable\nTrace\n", "enum")
                + "outside:\n}\n";
        String expected = "with(scope)\n{\n   if(enabled)\n   {\n      for(key in obj)\n      {\n         trace(key);\n      }\n   }\n}";
        String result = decompilePcode(pcode);
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @DataProvider(name = "copiedEnumerationControl")
    public Object[][] copiedEnumerationControl() {
        return new Object[][]{{"Drain", "break;"}, {"Loop", "continue;"}};
    }

    @Test
    public void testNestedCopiedEnumerationsReuseRegister() throws Exception {
        String inner = copiedEnumerationLoop("Push \"children\"\nGetVariable\nEnumerate2\n", false,
                "Push \"key\"\nGetVariable\nTrace\n", "inner").replace("\"key\"", "\"child\"");
        String pcode = copiedEnumerationLoop("Push \"obj\"\nGetVariable\nEnumerate2\n", false, inner, "outer");
        String expected = "for(key in obj)\n{\n   for(child in children)\n   {\n      trace(child);\n   }\n}";
        String result = decompilePcode(pcode);
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test(dataProvider = "copiedEnumerationControl")
    public void testCopiedEnumerationBreakAndContinue(String target, String control) throws Exception {
        String body = "Push \"key\"\nGetVariable\nPush \"skip\"\nEquals2\nIf enum" + target
                + "\nPush \"key\"\nGetVariable\nTrace\n";
        String result = decompilePcode(copiedEnumerationLoop("Push \"obj\"\nGetVariable\nEnumerate2\n", false, body, "enum"));
        Assert.assertTrue(result.contains("for(key in obj)"), result);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertEquals(result.split("trace\\(key\\)", -1).length - 1, 1, result);
        if (target.equals("Drain")) {
            Assert.assertTrue(result.contains(control), result);
        }
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    @Test
    public void testCopiedEnumerationRegisterReadAfterLoopPreventsElision() throws Exception {
        String result = decompilePcode(copiedEnumerationLoop("Push \"obj\"\nGetVariable\nEnumerate2\n", false,
                "Push \"key\"\nGetVariable\nTrace\n", "enum") + "Push register0\nTrace\n");
        Assert.assertFalse(result.contains("for(key in obj)"), result);
        Assert.assertTrue(result.contains("trace(_loc0_);"), result);
    }

    @Test
    public void testCopiedEnumerationDrainWithSideEffectsIsPreserved() throws Exception {
        String pcode = copiedEnumerationLoop("Push \"obj\"\nGetVariable\nEnumerate2\n", false,
                "Push \"key\"\nGetVariable\nTrace\n", "enum")
                .replace("Jump enumDrain", "Push \"cleanup\"\nTrace\nJump enumDrain");
        String result = decompilePcode(pcode);
        Assert.assertFalse(result.contains("for(key in obj)"), result);
        Assert.assertTrue(result.contains("trace(\"cleanup\");"), result);
    }

    @DataProvider(name = "shortCircuitRegisterAssignments")
    public Object[][] shortCircuitRegisterAssignments() {
        return new Object[][]{
            {"Not\n", "&&", "Push \"o\"\n", "SetVariable\n", "o"},
            {"", "||", "Push \"o\"\n", "SetVariable\n", "o"},
            {"Not\n", "&&", "Push \"target\"\nGetVariable\nPush \"value\"\n", "SetMember\n", "target.value"},
            {"", "||", "Push \"target\"\nGetVariable\nPush \"value\"\n", "SetMember\n", "target.value"}
        };
    }

    private String shortCircuitRegisterAssignment(String invert, String target, String assignment, String followingCondition) {
        return "Push \"seed\"\nStoreRegister 0\nPop\n"
                + "Push \"ready\"\nGetVariable\nPushDuplicate\n" + invert + "If join\nPop\n"
                + target + "Push 0, \"collide\"\nCallFunction\nStoreRegister 0\n" + assignment
                + "Push register0\njoin: " + followingCondition
                + "Not\nIf done\nPush register0\nTrace\ndone: Push register0\nTrace\n";
    }

    @Test
    public void testCs6ShortCircuitCommaAssignment() throws Exception {
        String pcode = "Push \"a\", 1\nDefineLocal\nPush \"b\", 2\nDefineLocal\n"
                + "Push \"a\"\nGetVariable\nPush 1\nLess2\nNot\nPushDuplicate\nNot\nIf join\nPop\n"
                + "Push \"b\", 3\nStoreRegister 0\nSetVariable\nPush register0\nPop\n"
                + "Push \"a\"\nGetVariable\nPush 1\nEquals2\n"
                + "join: Not\nIf done\nPush \"okay\"\nTrace\ndone:\n";
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions, 0);
        Assert.assertEquals(result, "var a = 1;\nvar b = 2;\nif(a >= 1 && ((b = 3,a == 1)))\n{\n   trace(\"okay\");\n}");
    }

    @DataProvider(name = "shortCircuitCommaAssignments")
    public Object[][] shortCircuitCommaAssignments() {
        List<Object[]> paths = new ArrayList<>();
        for (boolean and : new boolean[]{false, true}) {
            for (boolean left : new boolean[]{false, true}) {
                for (boolean right : new boolean[]{false, true}) {
                    for (boolean member : new boolean[]{false, true}) {
                        for (int deobfuscation : new int[]{0, 1}) {
                            paths.add(new Object[]{and, left, right, member, deobfuscation});
                        }
                    }
                }
            }
        }
        return paths.toArray(new Object[paths.size()][]);
    }

    @DataProvider(name = "shortCircuitVoidCalls")
    public Object[][] shortCircuitVoidCalls() {
        return new Object[][]{{"Stop", "stop()"}, {"Play", "play()"}, {"Push \"marker\"\nTrace", "trace(\"marker\")"}};
    }

    private String shortCircuitCommaVoidCall(String instruction) {
        return "Push \"a\", 1\nDefineLocal\nPush \"b\", 2\nDefineLocal\n"
                + "Push \"a\"\nGetVariable\nPush 1\nLess2\nNot\nPushDuplicate\nNot\nIf join\nPop\n"
                + "Push \"b\", 3\nStoreRegister 0\nSetVariable\nPush register0\nPop\n"
                + instruction + "\nPush undefined, undefined\nPop\n"
                + "Push \"a\"\nGetVariable\nPush 1\nEquals2\n"
                + "join: Not\nIf done\nPush \"okay\"\nTrace\ndone:\n";
    }

    @Test(dataProvider = "shortCircuitVoidCalls")
    public void testShortCircuitCommaWithVoidCallAndUnusedStackValue(String instruction, String call) throws Exception {
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, shortCircuitCommaVoidCall(instruction), swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions, 0);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertTrue(result.contains("a >= 1 && ((b = 3," + call + ",undefined/*unpopped*/,undefined,a == 1))"), result);
        Assert.assertEquals(result.split(Pattern.quote(call), -1).length - 1, 1, result);
    }

    @Test(dataProvider = "shortCircuitVoidCalls")
    public void testShortCircuitCommaDoesNotDiscardStackValueReadLater(String instruction, String call) throws Exception {
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, shortCircuitCommaVoidCall(instruction) + "Trace\n", swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions, 0);
        Assert.assertTrue(result.contains("&&"), result);
        Assert.assertTrue(result.contains("§§push(undefined)"), result);
        Assert.assertTrue(result.contains("trace(§§pop())"), result);
        Assert.assertFalse(result.contains("undefined/*unpopped*/"), result);
    }

    @Test(dataProvider = "shortCircuitCommaAssignments")
    public void testShortCircuitCommaPreservesAssignmentsAndRegister(boolean and, boolean left, boolean right, boolean member, int deobfuscation) throws Exception {
        String initialize = member ? "Push \"o\", \"b\", 2, 1\nInitObject\nSetVariable\n" : "Push \"b\", 2\nSetVariable\n";
        String target = member ? "Push \"o\"\nGetVariable\nPush \"b\"\n" : "Push \"b\"\n";
        String read = member ? "Push \"o\"\nGetVariable\nPush \"b\"\nGetMember\n" : "Push \"b\"\nGetVariable\n";
        String pcode = "Push \"result\", 0\nSetVariable\nPush 2\nStoreRegister 0\nPop\n" + initialize
                + "Push \"value\"\nGetVariable\nPushDuplicate\n" + (and ? "Not\n" : "") + "If join\nPop\n"
                + target + "Push 3\nStoreRegister 0\n" + (member ? "SetMember\n" : "SetVariable\n")
                + "Push register0\nPop\nPush \"y\"\nGetVariable\n"
                + "join: Not\nIf done\nPush \"result\", 1\nSetVariable\n"
                + "done: Push \"result\", \"result\"\nGetVariable\nPush 10\nMultiply\n"
                + read + "Add2\nPush register0\nAdd2\nSetVariable\n";
        SWF swf = new SWF();
        List<Action> original = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), original, deobfuscation);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertTrue(result.contains(and ? " && " : " || "), result);
        List<Action> compiled = new ActionScript2Parser(swf, new DoActionTag(swf)).actionsFromString(result, Utf8Helper.charsetName);
        boolean evaluatedRight = and ? left : !left;
        boolean condition = and ? left && right : left || right;
        double expected = (condition ? 10 : 0) + (evaluatedRight ? 6 : 4);
        Assert.assertEquals(((Number) runComparisonAssignments(original, left, right)).doubleValue(), expected);
        Assert.assertEquals(((Number) runComparisonAssignments(compiled, left, right)).doubleValue(), expected);
    }

    @DataProvider(name = "savedShortCircuitOperands")
    public Object[][] savedShortCircuitOperands() {
        List<Object[]> values = new ArrayList<>();
        for (boolean and : new boolean[]{false, true}) {
            for (boolean left : new boolean[]{false, true}) {
                for (boolean changed : new boolean[]{false, true}) {
                    for (boolean right : new boolean[]{false, true}) {
                        values.add(new Object[]{and, left, changed, right});
                    }
                }
            }
        }
        return values.toArray(new Object[values.size()][]);
    }

    @Test(dataProvider = "savedShortCircuitOperands")
    public void testShortCircuitKeepsOperandSavedBeforeAssignment(boolean and, boolean left, boolean changed, boolean right) throws Exception {
        String pcode = "Push \"result\", 0\nSetVariable\nPush \"value\"\nGetVariable\n"
                + "Push \"value\", " + changed + "\nSetVariable\nPushDuplicate\n" + (and ? "Not\n" : "")
                + "If join\nPop\nPush \"y\"\nGetVariable\njoin: Not\nIf done\n"
                + "Push \"result\", 1\nSetVariable\ndone:\n";
        SWF swf = new SWF();
        List<Action> original = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), original, 0);
        Assert.assertTrue(result.contains("var _temp_1 = value;"), result);
        Assert.assertTrue(result.indexOf("var _temp_1") < result.indexOf("value = " + changed + ";"), result);
        Assert.assertTrue(result.contains("_temp_1 " + (and ? "&&" : "||") + " y"), result);
        List<Action> compiled = new ActionScript2Parser(swf, new DoActionTag(swf)).actionsFromString(result, Utf8Helper.charsetName);
        long expected = (and ? left && right : left || right) ? 1 : 0;
        Assert.assertEquals(runComparisonAssignments(original, left, right), expected);
        Assert.assertEquals(runComparisonAssignments(compiled, left, right), expected);
    }

    @Test
    public void testNestedShortCircuitReadsPropertyBeforeRestoringResolver() throws Exception {
        String disable = "Push \"_global\"\nGetVariable\nPush \"__resolve\"\nDelete\nPop\n";
        String restore = "Push \"_global\"\nGetVariable\nPush \"__resolve\", \"_global\"\nGetVariable\n"
                + "Push \"__debugResolve\"\nGetMember\nSetMember\n";
        String pcode = "DefineFunction \"check\", 2, \"p\", \"pn\" {\n"
                + disable + "Push undefined, \"p\"\nGetVariable\nPush \"lockedIndex\"\nGetMember\nEquals2\n"
                + restore + "PushDuplicate\nIf join\nPop\n"
                + disable + "Push undefined, \"pn\"\nGetVariable\nPush \"lockedIndex\"\nGetMember\nEquals2\n"
                + restore + "PushDuplicate\nIf join\nPop\n"
                + "Push \"p\"\nGetVariable\nPush \"lockedIndex\"\nGetMember\n"
                + "Push \"pn\"\nGetVariable\nPush \"lockedIndex\"\nGetMember\nEquals2\nNot\n"
                + "join: If done\nPush -1\nReturn\ndone: Push undefined\nReturn\n}\n";
        boolean removeDebugResolve = Configuration.deobfuscateAs12RemoveGlobalDebugResolve.get();
        String result;
        Configuration.deobfuscateAs12RemoveGlobalDebugResolve.set(false);
        try {
            result = decompilePcode(pcode);
        } finally {
            Configuration.deobfuscateAs12RemoveGlobalDebugResolve.set(removeDebugResolve);
        }
        // Folding into a comma expression must preserve the read between
        // disabling the resolver and restoring it.
        Assert.assertTrue(result.contains("delete _global.__resolve,_temp_2 = undefined == pn.lockedIndex,"
                + "_global.__resolve = _global.__debugResolve,_temp_2 || p.lockedIndex != pn.lockedIndex"), result);
        Assert.assertFalse(result.contains("undefined == pn.lockedIndex ||"), result);
    }

    @Test(dataProvider = "shortCircuitRegisterAssignments")
    public void testShortCircuitRegisterAssignment(String invert, String operator, String target, String assignment, String name) throws Exception {
        SWF swf = new SWF();
        String pcode = shortCircuitRegisterAssignment(invert, target, assignment, "");
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        String expected = "var _loc0_ = \"seed\";\nif(ready " + operator + " (" + name + " = _loc0_ = collide()))\n"
                + "{\n   trace(_loc0_);\n}\ntrace(_loc0_);";
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test(dataProvider = "shortCircuitRegisterAssignments")
    public void testShortCircuitSingleReadRemovesOnlyItsRegisterWrite(String invert, String operator, String target, String assignment, String name) throws Exception {
        SWF swf = new SWF();
        String pcode = shortCircuitRegisterAssignment(invert, target, assignment, "")
                .replace("Push \"ready\"", "Push register0\nTrace\nPush \"ready\"")
                .replace("If done\nPush register0\nTrace\ndone: Push register0\nTrace", "If done\nPush \"yes\"\nTrace\ndone: Push \"after\"\nTrace");
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        String expected = "var _loc0_ = \"seed\";\ntrace(_loc0_);\nif(ready " + operator + " (" + name + " = collide()))\n"
                + "{\n   trace(\"yes\");\n}\ntrace(\"after\");";
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test
    public void testShortCircuitRegisterReadAfterOverwriteUsesTheNewWrite() throws Exception {
        SWF swf = new SWF();
        String pcode = shortCircuitRegisterAssignment("Not\n", "Push \"o\"\n", "SetVariable\n", "")
                .replace("If done\nPush register0\nTrace", "If done\nPush \"yes\"\nTrace")
                .replace("done: Push register0", "done: Push \"new value\"\nStoreRegister 0\nPop\nPush register0");
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        String expected = "var _loc0_ = \"seed\";\nif(ready && (o = collide()))\n"
                + "{\n   trace(\"yes\");\n}\n_loc0_ = \"new value\";\ntrace(_loc0_);";
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test
    public void testShortCircuitRegisterReadAcrossLoopBackEdgeKeepsWrite() throws Exception {
        SWF swf = new SWF();
        String pcode = shortCircuitRegisterAssignment("Not\n", "Push \"o\"\n", "SetVariable\n", "")
                .replace("Push \"ready\"", "loop: Push \"ready\"")
                + "Push \"again\"\nGetVariable\nIf loop\n";
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        Assert.assertTrue(result.contains("o = _loc0_ = collide()"), result);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    @DataProvider(name = "registerContainers")
    public Object[][] registerContainers() {
        String read = "Push register0\nTrace\n";
        return new Object[][]{
            {"Push \"obj\"\nGetVariable\nWith {\n", "}\n", "", false},
            {"Push \"obj\"\nGetVariable\nWith {\n", "}\n", read, true},
            {"Try {\n", "} Finally {\nPush \"finished\"\nTrace\n}\n", "", false},
            {"Try {\n", "} Finally {\n" + read + "}\n", "", true},
            {"Try \"e\" {\n", "} Catch {\nPush \"failed\"\nTrace\n}\n", "", false},
            {"Try \"e\" {\n", "} Catch {\n" + read + "}\n", "", true},
            {"Try \"e\" {\nPush \"failure\"\nThrow\n} Catch {\n", "}\n", "", false},
            {"Try \"e\" {\nPush \"failure\"\nThrow\n} Catch {\n", "}\n", read, true},
            {"Try {\nPush \"tried\"\nTrace\n} Finally {\n", "}\n", "", false},
            {"Try {\nPush \"tried\"\nTrace\n} Finally {\n", "}\n", read, true},
            {"Push \"obj\"\nGetVariable\nWith {\nTry {\n", "} Finally {\nPush \"finished\"\nTrace\n}\n}\n", "", false},
            {"Try {\nPush \"obj\"\nGetVariable\nWith {\n", "}\n} Finally {\n" + read + "}\n", "", true},
            {"DefineFunction \"test\", 0 {\nTry {\n", "Push undefined\nReturn\n} Finally {\n" + read + "}\n}\n", "", true}
        };
    }

    @Test(dataProvider = "registerContainers")
    public void testShortCircuitRegisterUsageAcrossContainer(String prefix, String suffix, String after, boolean keepRegister) throws Exception {
        SWF swf = new SWF();
        String body = shortCircuitRegisterAssignment("Not\n", "Push \"o\"\n", "SetVariable\n", "")
                .replace("If done\nPush register0\nTrace\ndone: Push register0\nTrace", "If done\nPush \"yes\"\nTrace\ndone: Push \"after\"\nTrace");
        String pcode = prefix + body + suffix + after;
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        Assert.assertEquals(result.contains("o = _loc0_ = collide()"), keepRegister, result);
        Assert.assertTrue(result.contains(keepRegister ? "o = _loc0_ = collide()" : "o = collide()"), result);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertEquals(result.split("collide\\(\\)", -1).length - 1, 1, result);
        Assert.assertEquals(compileAndDecompile(result), result);
    }

    @Test
    public void testShortCircuitRegisterAssignmentWithFollowingCondition() throws Exception {
        SWF swf = new SWF();
        String pcode = shortCircuitRegisterAssignment("Not\n", "Push \"o\"\n", "SetVariable\n",
                "PushDuplicate\nNot\nIf conditionDone\nPop\nPush \"o\"\nGetVariable\nPush \"valid\"\nGetMember\nconditionDone: ");
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        String expected = "var _loc0_ = \"seed\";\nif(ready && (o = _loc0_ = collide()) && o.valid)\n"
                + "{\n   trace(_loc0_);\n}\ntrace(_loc0_);";
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test
    public void testShortCircuitDynamicVariableAssignmentKeepsRegisterResult() throws Exception {
        SWF swf = new SWF();
        String pcode = shortCircuitRegisterAssignment("Not\n", "Push \"name\"\nGetVariable\n", "SetVariable\n", "");
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        String expected = "var _loc0_ = \"seed\";\nif(ready && ((set(name,_loc0_ = collide()),_loc0_)))\n"
                + "{\n   trace(_loc0_);\n}\ntrace(_loc0_);";
        Assert.assertEquals(result, expected);
    }

    @DataProvider(name = "repeatedShortCircuitConditions")
    public Object[][] repeatedShortCircuitConditions() {
        return new Object[][]{{"", "||"}, {"Not\n", "&&"}};
    }

    @Test(dataProvider = "repeatedShortCircuitConditions")
    public void testRepeatedShortCircuitCondition(String invert, String operator) throws Exception {
        String condition = "Push \"key\"\nGetVariable\nPush \"success\"\nStringEquals\nNot\n";
        String pcode = "Push \"before\"\nTrace\n" + condition + condition + invert
                + "If join\nPop\nPush \"EOF\"\nGetVariable\nPush \"true\"\nStringEquals\nNot\n"
                + "join: Not\nIf otherwise\nPush \"yes\"\nTrace\nJump done\n"
                + "otherwise: Push \"no\"\nTrace\ndone:\n";
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        String expected = "trace(\"before\");\nif(key ne \"success\" " + operator + " EOF ne \"true\")\n"
                + "{\n   trace(\"yes\");\n}\nelse\n{\n   trace(\"no\");\n}";
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test
    public void testRepeatedCallsKeepBothEvaluations() throws Exception {
        String call = "Push 0, \"check\"\nCallFunction\n";
        String pcode = call + call + "If join\nPop\nPush false\n"
                + "join: Not\nIf done\nPush \"yes\"\nTrace\ndone:\n";
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        Assert.assertEquals(result.split("check\\(\\)", -1).length - 1, 2, result);
    }

    @Test
    public void testRepeatedConditionAfterVariableChange() throws Exception {
        String condition = "Push \"key\"\nGetVariable\nPush \"success\"\nStringEquals\nNot\n";
        String pcode = condition + "Push \"key\", \"replacement\"\nGetVariable\nSetVariable\n" + condition
                + "If join\nPop\nPush false\njoin: Not\nIf done\nPush \"yes\"\nTrace\ndone:\n";
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        Assert.assertEquals(result.split("key (?:eq|ne) \"success\"", -1).length - 1, 2, result);
        Assert.assertTrue(result.contains("key = replacement;"), result);
    }

    @DataProvider(name = "duplicatedConditionalPolarity")
    public Object[][] duplicatedConditionalPolarity() {
        return new Object[][]{{"", "Not\n", "!"}, {"Not\n", "", ""}};
    }

    @Test(dataProvider = "duplicatedConditionalPolarity")
    public void testDuplicatedConditionIfElse(String firstNot, String secondNot, String negation) throws Exception {
        String pcode = "Push 0, \"check\"\nCallFunction\nPushDuplicate\n"
                + firstNot + "If second\n"
                + "Push \"condition\", false\nSetVariable\nPush \"first\"\nTrace\n"
                + "second: " + secondNot + "If done\nPush \"second\"\nTrace\n"
                + "done: Push \"after\"\nTrace\n";
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        String expected = "if(" + negation + "check())\n{\n"
                + "   condition = false;\n   trace(\"first\");\n}\nelse\n{\n"
                + "   trace(\"second\");\n}\ntrace(\"after\");";
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test
    public void testNestedDuplicatedConditionIfElse() throws Exception {
        String pcode = "Push 0, \"hasVersion\"\nCallFunction\nPushDuplicate\nIf fallback\n"
                + "Push \"version\"\nTrace\n"
                + "fallback: Not\nIf done\n"
                + "Push 0, \"hasSystem\"\nCallFunction\nPushDuplicate\nIf last\n"
                + "Push \"6\"\nTrace\n"
                + "last: Not\nIf done\nPush \"5\"\nTrace\n"
                + "done: Push \"after\"\nTrace\n";
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result = decompile(swf, new DoActionTag(swf), actions);
        String expected = "if(!hasVersion())\n{\n   trace(\"version\");\n}\n"
                + "else if(!hasSystem())\n{\n   trace(\"6\");\n}\n"
                + "else\n{\n   trace(\"5\");\n}\ntrace(\"after\");";
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test
    public void testStringSwitchCasesContinuingOuterLoop() throws Exception {
        String result = compileAndDecompile("function testLoop() {"
                + "var result = this.createValue();"
                + "while (true) {"
                + "switch (this.getValue()) {"
                + "case \"a\":"
                + "this.advance();"
                + "if (this.getValue().slice(0, 2) !== \"xy\") {"
                + "handleError();"
                + "}"
                + "result = [\"a\", result, this.advance().slice(2)];"
                + "continue;"
                + "case \"b\":"
                + "this.advance();"
                + "result = [\"a\", result, this.readValue()];"
                + "this.finish(\"b\");"
                + "continue;"
                + "case \"c\":"
                + "result = [this.advance(), result, this.readList()];"
                + "this.finish(\"c\");"
                + "continue;"
                + "}"
                + "break;"
                + "}"
                + "return result;"
                + "}");
        Assert.assertTrue(result.contains("function testLoop()"), result);
        Assert.assertTrue(result.contains("case \"a\":"), result);
        Assert.assertTrue(result.contains("case \"b\":"), result);
        Assert.assertTrue(result.contains("case \"c\":"), result);
        Assert.assertTrue(result.contains("this.readValue()"), result);
        Assert.assertTrue(result.contains("this.readList()"), result);
        Assert.assertEquals(result.split("continue;", -1).length - 1, 3, result);
        Assert.assertFalse(Pattern.compile("\\bloop\\d+\\s*:").matcher(result).find(), result);
        Assert.assertFalse(Pattern.compile("(?m)^\\s*addr[0-9a-f]+\\s*:").matcher(result).find(), result);
        Assert.assertFalse(Pattern.compile("\\b(?:break|continue)\\s+\\w+").matcher(result).find(), result);
        Assert.assertFalse(result.contains("§§goto"), result);
    }

    @Test
    public void testNestedSwitchBreaksInForIn() throws Exception {
        // Compilation merges the inner and outer breaks into the same jump target.
        String source = "var t = {}; trace(\"A\"); for (var i in t) {"
                + "trace(\"b\"); switch (i) {"
                + "case \"string\": case \"number\": case \"boolean\":"
                + "switch (i) { case null: case \"inherit\": case \"\": break;"
                + "default: trace(\"B\"); break; } break; } } trace(\"finish\");";
        String expected = "var t = {};\ntrace(\"A\");\nfor(var i in t)\n{\n"
                + "   trace(\"b\");\n   switch(i)\n   {\n"
                + "      case \"string\":\n      case \"number\":\n      case \"boolean\":\n"
                + "         switch(i)\n         {\n            case null:\n"
                + "            case \"inherit\":\n            case \"\":\n"
                + "               break;\n            default:\n               trace(\"B\");\n"
                + "         }\n   }\n}\ntrace(\"finish\");";
        String result = compileAndDecompile(source);
        Assert.assertEquals(result, expected);
        Assert.assertEquals(compileAndDecompile(result), expected);
    }

    @Test
    public void testNestedForInWithSwitch() throws Exception {
        // Exhausting the inner enumeration jumps to the outer loop's next key.
        String pcode = "DefineFunction2 \"select\", 2, 5, false, false, true, false, true, false, true, false, false, 1, \"stacks\", 2, \"button\" {\n"
                + "Push register1\nEnumerate2\n"
                + "outer: StoreRegister 0\nPush null\nEquals2\nIf end\n"
                + "Push register0\nStoreRegister 4\nPop\n"
                + "Push register1, register4\nGetMember\nPush \"items\"\nGetMember\nEnumerate2\n"
                + "inner: StoreRegister 0\nPush null\nEquals2\nIf nextOuter\n"
                + "Push register0\nStoreRegister 3\nPop\n"
                + "Push register1, register4\nGetMember\nPush \"type\"\nGetMember\n"
                + "StoreRegister 0\nPush \"LIST\"\nStrictEquals\nIf lists\n"
                + "Push register0, \"TOGGLE\"\nStrictEquals\nIf toggles\nJump nextInner\n"
                + "lists: Push register2, register3\nEquals2\nPushDuplicate\nNot\nIf listGuard\nPop\n"
                + "Push register2, register1, register4\nGetMember\nPush \"selected\"\nGetMember\nEquals2\nNot\n"
                + "listGuard: Not\nIf nextList\n"
                + "Push register3\nTrace\n"
                + "listDrainInner: Push null\nEquals2\nNot\nIf listDrainInner\n"
                + "listDrainOuter: Push null\nEquals2\nNot\nIf listDrainOuter\nPush true\nReturn\n"
                + "nextList: Jump nextInner\n"
                + "toggles: Push register2, register3\nEquals2\nNot\nIf nextInner\n"
                + "Push register4\nTrace\n"
                + "toggleDrainInner: Push null\nEquals2\nNot\nIf toggleDrainInner\n"
                + "toggleDrainOuter: Push null\nEquals2\nNot\nIf toggleDrainOuter\nPush true\nReturn\n"
                + "nextInner: Jump inner\nnextOuter: Jump outer\nend: Push false\nReturn\n}\n";
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        String result;
        boolean autoDeobfuscate = Configuration.autoDeobfuscate.get();
        Configuration.autoDeobfuscate.set(true);
        try {
            result = decompile(swf, new DoActionTag(swf), actions);
        } finally {
            Configuration.autoDeobfuscate.set(autoDeobfuscate);
        }
        Assert.assertTrue(result.contains("for(var _loc4_ in stacks)"), result);
        Assert.assertTrue(result.contains("for(var _loc3_ in stacks[_loc4_].items)"), result);
        Assert.assertTrue(result.contains("case \"LIST\":"), result);
        Assert.assertTrue(result.contains("case \"TOGGLE\":"), result);
        Assert.assertFalse(result.contains("§§enumerat"), result);
        Assert.assertTrue(compileAndDecompile(result).contains("for(var _loc3_ in stacks[_loc4_].items)"), result);
    }

    @DataProvider(name = "guardedVoidExitLoops")
    public Object[][] guardedVoidExitLoops() {
        return new Object[][]{{true}, {false}};
    }

    @Test(dataProvider = "guardedVoidExitLoops")
    public void testForInWithSharedVoidExitAndFallthrough(boolean guarded) throws Exception {
        String pcode = "DefineFunction \"check\", 0 {\n"
                + (guarded ? "Push \"enabled\"\nGetVariable\nNot\nIf afterLoop\n" : "")
                + "Push \"obj\"\nGetVariable\nEnumerate2\n"
                + "loop: StoreRegister 0\nPush null\nEquals2\nIf afterLoop\n"
                + "Push \"key\", register0\nSetVariable\n"
                + "Push \"hit\"\nGetVariable\nNot\nIf next\n"
                + "Push \"side\"\nGetVariable\nNot\nIf second\n"
                + "Push \"first\"\nTrace\nJump drain\n"
                + "second: Push \"second\"\nTrace\n"
                + "drain: Push null\nEquals2\nNot\nIf drain\nJump done\n"
                + "next: Push \"border\", null\nSetVariable\nJump loop\n"
                + "afterLoop: Push \"after\"\nTrace\ndone:\n}\n";
        String result = decompilePcode(pcode);
        Assert.assertTrue(result.contains("for(key in obj)"), result);
        Assert.assertFalse(result.contains("§§"), result);
        Assert.assertEquals(result.split("trace\\(\"after\"\\)", -1).length - 1, 1, result);
        Assert.assertTrue(result.contains("border = null;"), result);
        Assert.assertEquals(compileAndDecompile(result).replace("return undefined;", "return;"), result);
    }

}
