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
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.deobfuscation.ActionDeobfuscator;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionWith;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2DeobfuscatorTest extends ActionScript2TestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(true);
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2/as2.swf")), false);
    }

    private String recompile(String str) throws ActionParseException, IOException, CompilationException, InterruptedException, TimeoutException {
        SWF swf = new SWF();
        swf.version = SWF.DEFAULT_VERSION;
        ActionScript2Parser par = new ActionScript2Parser(swf, new DoActionTag(swf));
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        List<Action> actions = par.actionsFromString(str, Utf8Helper.charsetName);
        byte[] hex = Action.actionsToBytes(actions, true, SWF.DEFAULT_VERSION);
        DoActionTag doA = new DoActionTag(swf);
        ActionList list = ActionListReader.readActionListTimeout(doA, new ArrayList<>(), new SWFInputStream(swf, hex), SWF.DEFAULT_VERSION, 0, hex.length, "", 1);
        Action.actionsToSource(new HashMap<>(), null, list, "", writer, Utf8Helper.charsetName);
        writer.finishHilights();
        return writer.toString();
    }

    private String decompilePCode(String str) throws Exception {
        List<Action> actions = ASMParser.parse(0, true, str, swf.version, false, swf.getCharset(), swf.getSwfProfile());

        DoActionTag doa = getFirstActionTag();
        doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
        writer.finishHilights();
        return writer.toString().trim().replace("\r\n", "\n");
    }

    @Test
    public void testFoldArrayLiteral() throws Exception {
        String res = decompilePCode("Push \"sample\"\n"
                + "Push undefined\n"
                + "Push 40\n"
                + "Push 2\n"
                + "Add\n"
                + "Push 2\n"
                + "InitArray\n"
                + "SetVariable\n"
                + "Push \"sample\"\n"
                + "GetVariable\n"
                + "Trace");
        Assert.assertEquals(res, "sample = [42,undefined];\ntrace(sample);");
    }

    @DataProvider(name = "discardedConstantPools")
    public Object[][] discardedConstantPools() {
        return new Object[][]{{Arrays.asList("wrongName")}, {Arrays.asList("wrongName", "wrongValue")}};
    }

    @Test(dataProvider = "discardedConstantPools")
    public void testDiscardedPoolDoesNotBecomeLiteralString(List<String> discardedPool) throws Exception {
        ActionList actions = ASMParser.parse(0, true, "ConstantPool \"value\", \"resolved\"\n"
                + "Push \"external\"\nGetVariable\nTrace\n"
                + "Push constant0, constant1\nPushDuplicate\nPop\nSetVariable\n"
                + "Push constant0\nGetVariable\nTrace\nEnd\n",
                swf.version, false, swf.getCharset(), swf.getSwfProfile());
        // This is the intermediate state after an obfuscation pool is removed:
        // the surviving pool is correct, but Push metadata still names the old one.
        for (Action action : actions) {
            if (action instanceof ActionPush) {
                ((ActionPush) action).constantPool = discardedPool;
            }
        }
        new ActionDeobfuscator().actionListParsed(actions, swf);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), null, actions, "", writer, swf.getCharset());
        writer.finishHilights();
        Assert.assertEquals(writer.toString().trim().replace("\r\n", "\n"),
                "trace(external);\nvalue = \"resolved\";\ntrace(value);");
    }

    @Test
    public void testInvalidPoolIndexIsNotReplacedWithString() throws Exception {
        String result = decompilePCode("ConstantPool \"value\"\n"
                + "Push constant0, constant7\nSetVariable\nEnd\n");
        Assert.assertEquals(result, "value = §§constant(7);");
    }

    @Test
    public void testEntryDispatcherKeepsExternalVariableRead() throws Exception {
        StringBuilder pcode = new StringBuilder("Push \"state\", 7\nDefineLocal\nJump dispatch\n");
        for (int i = 0; i < 100; i++) {
            pcode.append("decoy").append(i).append(": Push \"external\"\nGetVariable\nIf decoy")
                    .append((i + 1) % 100).append("\nJump dispatch\n");
        }
        pcode.append("dispatch: Push \"state\"\nGetVariable\nPush 7\nEquals2\nIf selected\n")
                .append("Jump decoy0\nselected: Push \"external\"\nGetVariable\nTrace\nEnd\n");
        Assert.assertEquals(decompilePCode(pcode.toString()), "var state = 7;\ntrace(external);");
    }

    @Test
    public void testFoldObjectWithNestedArray() throws Exception {
        String res = decompilePCode("Push \"sample\"\n"
                + "Push \"second\"\n"
                + "Push false\n"
                + "Push \"first\"\n"
                + "Push undefined\n"
                + "Push 40\n"
                + "Push 2\n"
                + "Add\n"
                + "Push 2\n"
                + "InitArray\n"
                + "Push 2\n"
                + "InitObject\n"
                + "SetVariable\n"
                + "Push \"sample\"\n"
                + "GetVariable\n"
                + "Trace");
        Assert.assertEquals(res, "sample = {second:false,first:[42,undefined]};\ntrace(sample);");
    }

    @Test
    public void testKeepSharedArrayReference() throws Exception {
        String res = decompilePCode("Push \"a\"\n"
                + "Push 40\n"
                + "Push 2\n"
                + "Add\n"
                + "Push 1\n"
                + "InitArray\n"
                + "SetVariable\n"
                + "Push \"b\"\n"
                + "Push \"a\"\n"
                + "GetVariable\n"
                + "SetVariable\n"
                + "Push \"b\"\n"
                + "GetVariable\n"
                + "Trace");
        assertTrue(res.contains("b = a;"), res);
    }

    @Test
    public void testKeepFunctionReturningArray() throws Exception {
        decompilePCode("DefineFunction \"-\" 0 {\n"
                + "Push 0\n"
                + "InitArray\n"
                + "Return\n"
                + "}\n"
                + "Push \"result\"\n"
                + "Push 0\n"
                + "Push \"-\"\n"
                + "CallFunction\n"
                + "SetVariable\n"
                + "Push \"result\"\n"
                + "GetVariable\n"
                + "Trace");
        assertTrue(getFirstActionTag().getActions().stream().anyMatch(a -> a instanceof ActionDefineFunction));
    }

    @DataProvider(name = "provideBasicTrueExpressions")
    public Object[][] provideBasicTrueExpressions() {
        return new Object[][]{
            {"1!=5"}, {"5==5"}, {"1<4"}, {"5>4"}, {"5*6==30"}
        };
    }

    @DataProvider(name = "provideBasicFalseExpressions")
    public Object[][] provideBasicFalseExpressions() {
        return new Object[][]{
            {"1==5"}, {"5!=5"}, {"1>4"}, {"5<4"}, {"5*7==12"}
        };
    }

    @Test(dataProvider = "provideBasicTrueExpressions")
    public void testRemoveBasicTrueExpressions(String expression) throws ActionParseException, IOException, CompilationException, InterruptedException, TimeoutException {
        String res = recompile("if(" + expression + "){"
                + "trace(\"OK\");"
                + "} else {"
                + "trace(\"FAIL\");"
                + "}");
        if (res.contains("\"FAIL\"")) {
            fail("OnFalse clause was not removed: " + res);
        }
        if (!res.contains("\"OK\"")) {
            fail("OnTrue clause was removed: " + res);
        }
    }

    @Test(dataProvider = "provideBasicFalseExpressions")
    public void testRemoveBasicFalseExpressions(String expression) throws Exception {
        String res = recompile("if(" + expression + "){"
                + "trace(\"FAIL\");"
                + "} else {"
                + "trace(\"OK\");"
                + "}");
        if (res.contains("\"FAIL\"")) {
            fail("OnTrue clause was not removed:" + res);
        }
        if (!res.contains("\"OK\"")) {
            fail("OnFalse clause was removed:" + res);
        }
    }

    // todo: honfika @Test
    public void testRemoveKnownVariables() throws Exception {
        String res = recompile("var a = true; var b = false;"
                + "if(a){"
                + "trace(\"OK1\");"
                + "}else{"
                + "trace(\"FAIL1\");"
                + "}"
                + "if(b){"
                + "trace(\"FAIL2\");"
                + "}else{"
                + "trace(\"OK2\");"
                + "}");
        if (!res.contains("\"OK1\"")) {
            fail("if true OnTrue removed");
        }
        if (!res.contains("\"OK2\"")) {
            fail("if false OnFalse removed");
        }
        if (res.contains("\"FAIL1\"")) {
            fail("if true OnFalse not removed");
        }
        if (res.contains("\"FAIL2\"")) {
            fail("if false OnTrue not removed");
        }
        if (res.contains("var ")) {
            fail("variables for obfuscation not removed");
        }
        if (res.contains("if")) {
            fail("if clauses not removed");
        }
    }

    // todo: honfika @Test
    public void testNotRemoveParams() throws Exception {
        String res = recompile("function tst(p1,p2){"
                + "var a = 2;"
                + "var b = 3 * a;"
                + "if(b>1){"
                + "trace(\"OK1\");"
                + "}else{"
                + "trace(\"FAIL1\");"
                + "}"
                + "var c = p1*5;"
                + "if(c){"
                + "trace(\"OK2\");"
                + "}else{"
                + "trace(\"OK3\");"
                + "}"
                + "}");
        if (!res.contains("\"OK1\"")) {
            fail("basic if true onTrue removed");
        }
        if (res.contains("\"FAIL1\"")) {
            fail("basic if true onFalse not removed");
        }
        if (!res.contains("\"OK2\"")) {
            fail("if parameter onTrue removed");
        }
        if (!res.contains("\"OK3\"")) {
            fail("if parameter onFalse removed");
        }
    }

    @Test
    public void testEvalExpressionAfterWhile() throws Exception {
        String res = recompile("var a = 5;"
                + "while(true){"
                + "if(a==73){"
                + "a = 15;"
                + "}"
                + "if(a==1){"
                + "trace(\"FAIL1\");"
                + "}"
                + "if(a==5){"
                + "a=50;"
                + "}"
                + "if(a == 201){"
                + "break;"
                + "}"
                + "a++;"
                + "if(a == 53){"
                + "a = a + 20;"
                + "}"
                + "if(a>500){"
                + "trace(\"FAIL2\");"
                + "}"
                + "if(a==16){"
                + "a = 200;"
                + "}"
                + "}"
                + ""
                + "if(a == 201){"
                + "trace(\"OK\");"
                + "}else{"
                + "trace(\"FAIL3\");"
                + "}");
        if (res.contains("\"FAIL1\"")) {
            fail("unreachable if onTrue not removed");
        }
        if (res.contains("\"FAIL2\"")) {
            fail("unreachable if onTrue 2 not removed");
        }
        if (res.contains("\"FAIL3\"")) {
            fail("unreachable if onTrue 3 not removed");
        }
        if (!res.contains("\"OK\"")) {
            fail("reachable if onTrue removed");
        }
    }

    @Test
    public void testRemoveJumpsToTheNextAction() {
        String actionsString = "ConstantPool \"a\" \"b\" \"c\"\n"
                + "Push false register1\n"
                + "StoreRegister 2\n"
                + "Pop\n"
                + "Push register2\n"
                + "StoreRegister 0\n"
                + "Push \"a\"\n"
                + "StrictEquals\n"
                + "If loc005a\n"
                + "Push register0 \"b\"\n"
                + "StrictEquals\n"
                + "If loc0068\n"
                + "Jump loc0048;\n"
                + "loc0048:Push register0 \"c\"\n"
                + "StrictEquals\n"
                + "If loc0076\n"
                + "Jump loc0084\n"
                + "loc005a:Push 1\n"
                + "Trace\n"
                + "Jump loc0084\n"
                + "loc0068:Push 2\n"
                + "Trace\n"
                + "Jump loc0084\n"
                + "loc0076:Push 3\n"
                + "Trace\n"
                + "Jump loc0084\n"
                + "loc0084:";
        try {
            List<Action> actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset(), swf.getSwfProfile());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
            writer.finishHilights();
            String actualResult = writer.toString();

            assertTrue(actualResult.contains("case \"c\":"));
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    @Test
    public void testRemoveGetTime() {
        String actionsString = "ConstantPool \"a\"\n"
                + "GetTime\n"
                + "If loc1\n"
                + "Push \"FAIL\"\n"
                + "Trace\n"
                + "loc1:Push \"OK\"\n"
                + "Trace\n"
                + "loc2:";
        try {
            List<Action> actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset(), swf.getSwfProfile());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
            writer.finishHilights();
            String actualResult = writer.toString();

            assertTrue(!actualResult.contains("FAIL"));
            assertTrue(actualResult.contains("OK"));
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    @Test
    public void testRemoveGetTimeWithJumpTo() {
        String actionsString = "ConstantPool \"a\"\n"
                + "Jump loc3\n"
                + "loc4:Push 1\n"
                + "Trace\n"
                + "Jump loc5\n"
                + "loc3:GetTime\n"
                + "If loc1\n"
                + "Push \"FAIL\"\n"
                + "Trace\n"
                + "loc1:Push \"OK\"\n"
                + "Trace\n"
                + "loc2:Jump loc4\n"
                + "loc5:";
        try {
            List<Action> actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset(), swf.getSwfProfile());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
            writer.finishHilights();
            String actualResult = writer.toString();

            assertTrue(!actualResult.contains("FAIL"));
            assertTrue(actualResult.contains("OK"));
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    @Test
    public void testRemoveGetTimeAndIncrement() {
        String actionsString = "ConstantPool \"a\"\n"
                + "GetTime\n"
                + "Increment\n"
                + "If loc1\n"
                + "Push \"FAIL\"\n"
                + "Trace\n"
                + "loc1:Push \"OK\"\n"
                + "Trace\n"
                + "loc2:";
        try {
            List<Action> actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset(), swf.getSwfProfile());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
            writer.finishHilights();
            String actualResult = writer.toString();

            assertTrue(!actualResult.contains("FAIL"));
            assertTrue(actualResult.contains("OK"));
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    @Test
    public void testRemoveGetTimeAndIncrementWithJumpTo() {
        String actionsString = "ConstantPool \"a\"\n"
                + "Jump loc3\n"
                + "loc4: Push 1\n"
                + "Trace\n"
                + "Jump loc5\n"
                + "loc3:GetTime\n"
                + "Increment\n"
                + "If loc1\n"
                + "Push \"FAIL\"\n"
                + "Trace\n"
                + "loc1:Push \"OK\"\n"
                + "Trace\n"
                + "loc2:Jump loc4\n"
                + "loc5:\n";
        try {
            List<Action> actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset(), swf.getSwfProfile());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
            writer.finishHilights();
            String actualResult = writer.toString();

            assertTrue(!actualResult.contains("FAIL"));
            assertTrue(actualResult.contains("OK"));
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    @Test
    public void testJumpAfterFunctionEnd() {
        String actionsString = "ConstantPool \"a\", \"b\", \"c\", \"d\"\n"
                + "Jump loc0026\n"
                + "loc0012:Push \"once\"\n"
                + "Trace\n"
                + "Jump loc0058\n"
                + "loc0021:Jump loc0053\n"
                + "loc0026:DefineFunction \"f\", 0 {\n"
                + "Push \"a\"\n"
                + "Trace\n"
                + "Push register1\n"
                + "Push 2\n"
                + "Equals\n"
                + "If loc004d\n"
                + "Jump loc0021\n"
                + "loc004d:Push \"b\"\n"
                + "Trace\n"
                + "}\n"
                + "loc0053:Jump loc0012\n"
                + "loc0058:Push \"c\"\n"
                + "Trace";
        try {
            List<Action> actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset(), swf.getSwfProfile());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
            writer.finishHilights();
            String actualResult = writer.toString();

            Pattern patOnce = Pattern.compile("\"once\"");
            Matcher m = patOnce.matcher(actualResult);
            int count = 0;
            while (m.find()) {
                count++;
            }

            Assert.assertEquals(count, 1, "The string \"once\" should only appear once.");
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    @DataProvider
    public Object[][] bypassedContainerHeaders() {
        return new Object[][]{
            {"With {\n", "}\n", "", ActionWith.class},
            {"DefineFunction \"bypassed\" 0 {\n", "}\n", "", ActionDefineFunction.class},
            {"DefineFunction2 \"bypassed\" 0 1 false false false false false false false false false {\n", "}\n", "", ActionDefineFunction2.class},
            {"Try \"e\" {\n", "}\nCatch {\n", "}\nFinally {\nJump done\n}\n", ActionTry.class},
            {"Try \"e\" {\nJump correct\n}\nCatch {\n", "}\nFinally {\n", "}\n", ActionTry.class},
            {"Try \"e\" {\nJump correct\n}\nCatch {\nJump correct\n}\nFinally {\n", "}\n", "", ActionTry.class}
        };
    }

    @Test(dataProvider = "bypassedContainerHeaders")
    public void testJumpPastContainerBodyEnteredFromScriptEntry(String header, String boundary, String trailingSections, Class<? extends Action> containerClass) throws Exception {
        String pcode = "Jump entry\n"
                + "containerHeader: " + header
                + "entry: Push \"state\", 7\n"
                + "DefineLocal\n"
                + "dispatch: Push \"state\"\n"
                + "GetVariable\n"
                + "Push 9\n"
                + "Equals\n"
                + "If containerHeader\n"
                + "Push true\n"
                + "Push \"state\"\n"
                + "GetVariable\n"
                + "Push 7\n"
                + "Equals\n"
                + "If afterContainer\n"
                + "Jump dispatch\n"
                + boundary
                + "afterContainer: If correct\n"
                + "Push \"WRONG\"\n"
                + "Trace\n"
                + "Jump done\n"
                + "correct: Push \"OK\"\n"
                + "Trace\n"
                + trailingSections
                + "done: End\n";
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        DoActionTag source = getFirstActionTag();
        source.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
        ActionList deobfuscated = source.getActions();
        Assert.assertFalse(deobfuscated.stream().anyMatch(containerClass::isInstance), "The bypassed container must be removed");

        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        source.getActionScriptSource(writer, deobfuscated);
        writer.finishHilights();
        Assert.assertEquals(writer.toString().trim().replace("\r\n", "\n"), "var state = 7;\ntrace(\"OK\");");
    }

    @Test
    public void testKeepExecutedWithScope() throws Exception {
        String result = recompile("var scope = {value: 1}; with(scope) { trace(value); } trace(2);");
        assertTrue(result.contains("with(scope)"), result);
        assertTrue(result.contains("trace(value);"), result);
        assertTrue(result.contains("trace(2);"), result);
    }

    @Test
    public void testKeepExecutedTryCatchFinally() throws Exception {
        String result = recompile("try { throw 'error'; } catch (e) { trace(e); } finally { trace('finally'); } trace('after');");
        assertTrue(result.contains("throw \"error\";"), result);
        assertTrue(result.contains("catch") && result.contains("trace(e);"), result);
        assertTrue(result.contains("finally") && result.contains("trace(\"finally\");"), result);
        assertTrue(result.contains("trace(\"after\");"), result);
    }

    @Test
    public void testRemoveForInReturnsWithJumpsEnd() {
        String actionsString = "Jump locA\n"
                + "locC: Not\n"
                + "Jump locD\n"
                + "locB: Equals2\n"
                + "Jump locC\n"
                + "locA: Push null\n"
                + "Jump locB\n"
                + "locD: If locA\n"
                + "Push 1\n"
                + "Trace\n";
        try {
            List<Action> actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset(), swf.getSwfProfile());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
            writer.finishHilights();
            String actualResult = writer.toString().trim();

            Assert.assertEquals(actualResult, "trace(1);");
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    @Test
    public void testRemoveAnd() throws Exception {
        String pcode = "ConstantPool \"v\"\n"
                + "Push \"v\"\n"
                + "Push 1\n"
                + "Push 2\n"
                + "Less2\n"
                + "PushDuplicate\n"
                + "Not\n"
                + "If loc0036\n"
                + "Pop\n"
                + "Push 2\n"
                + "Push 8\n"
                + "Less2\n"
                + "loc0036:DefineLocal\n";
        String actualResult = decompilePCode(pcode);
        Assert.assertEquals(actualResult, "var v = true;");
    }

    @Test
    public void testRemoveOr() throws Exception {
        String pcode = "ConstantPool \"v\"\n"
                + "Push \"v\"\n"
                + "Push 1\n"
                + "Push 2\n"
                + "Less2\n"
                + "PushDuplicate\n"
                + "If loc0035\n"
                + "Pop\n"
                + "Push 2\n"
                + "Push 8\n"
                + "Less2\n"
                + "loc0035:DefineLocal\n";
        String actualResult = decompilePCode(pcode);
        Assert.assertEquals(actualResult, "var v = true;");
    }

    @Test
    public void testPopMustStayIntact() throws Exception {
        String expected = "test < 100 ? (test > -100 ? 0 : f()) : g();";
        String actual = recompile(expected);
        actual = actual.trim();
        Assert.assertEquals(actual, expected);
    }
}
