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
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
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

    private String decompile(SWF swf, DoActionTag tag, List<Action> compiledActions) throws Exception {
        byte[] actionBytes = Action.actionsToBytes(compiledActions, true, swf.version);
        ActionList actions = ActionListReader.readActionListTimeout(tag, new ArrayList<>(),
                new SWFInputStream(swf, actionBytes), swf.version, 0, actionBytes.length, "", 1);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), tag, actions, "", writer, swf.getCharset());
        writer.finishHilights();
        return writer.toString().trim().replace("\r\n", "\n");
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
        SWF swf = new SWF();
        List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset(), swf.getSwfProfile());
        return decompile(swf, new DoActionTag(swf), actions);
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

}
