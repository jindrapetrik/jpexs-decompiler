/*
 * Copyright (C) 2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License, version 3 or later.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionDefineFunctionPushRegistersCleaner;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf4.ActionWaitForFrame2;
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionInitObject;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.action.swf5.ActionStackSwap;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf5.ActionWith;
import com.jpexs.decompiler.flash.action.swf6.ActionEnumerate2;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ActionDefineFunctionRegistersTest {

    private SWF newSwf(int version) {
        SWF swf = new SWF();
        swf.version = version;
        return swf;
    }

    private List<Action> compile(String source, SWF swf) throws Exception {
        return new ActionScript2Parser(swf, new DoActionTag(swf)).actionsFromString(source, Utf8Helper.charsetName);
    }

    private List<Action> readRaw(List<Action> actions, SWF swf) throws Exception {
        byte[] bytes = Action.actionsToBytes(actions, true, swf.version);
        SWFInputStream input = new SWFInputStream(swf, bytes);
        List<Action> result = new ArrayList<>();
        List<String> pool = null;
        while (input.available() > 0) {
            long address = input.getPos();
            Action action = input.readAction();
            if (action == null) {
                break;
            }
            action.setAddress(address);
            if (action instanceof ActionConstantPool) {
                pool = ((ActionConstantPool) action).constantPool;
            } else if (action instanceof ActionPush) {
                ((ActionPush) action).constantPool = pool;
            }
            result.add(action);
            input.seek(address + action.getBytesLength());
        }
        return result;
    }

    @DataProvider(name = "exits")
    public Object[][] exits() {
        return new Object[][]{
            {"var n = 0; switch (selector) { case 1: n = 5; break; default: n = 7; }", 1L, Undefined.INSTANCE},
            {"switch (selector) { case 1: return 23; default: return 42; }", 1L, 23L},
            {"switch (selector) { case 1: return 23; default: return 42; }", 2L, 42L},
            {"switch (selector) { case 1: return; }", 1L, Undefined.INSTANCE},
            {"switch (selector) { case 1: return 23; }", 2L, Undefined.INSTANCE},
            {"switch (selector) { case 1: break; } return selector + 9;", 1L, 10L},
            {"switch (selector) { case 1: switch (selector) { case 1: return 17; } } return 8;", 1L, 17L},
            {"var i = 0; while (i < 4) { switch (i) { case 2: if (selector == 1) return i; } i++; } return 9;", 1L, 2L},
            {"var i = 0; while (i < 4) { switch (i) { case 2: if (selector == 1) return i; } i++; } return 9;", 2L, 9L},
            {"function inner(x) { switch (x) { case 1: return 11; } return 4; } switch (selector) { case 1: return inner(selector); } return 8;", 1L, 11L},
            {"switch (selector) { case 1: with (scope) { return 31; } } return 8;", 1L, 31L},
            {"var obj = {a:1, b:2}; for (var key in obj) { if (selector == 1) return 12; } return 8;", 1L, 12L},
            {"var obj = {a:1, b:2}; for (var key in obj) { if (selector == 1) return 12; } return 8;", 2L, 8L},
            {"switch (selector) { case 1: function inner(x) { switch (x) { case 1: return 11; } } break; }", 1L, Undefined.INSTANCE}
        };
    }

    @Test(dataProvider = "exits")
    public void preservesCallerRegistersAndReturnValue(String body, long selector, Object expected) throws Exception {
        for (int version : new int[]{5, 6}) {
            SWF swf = newSwf(version);
            List<Action> actions = readRaw(compile("function test(selector) {" + body + "}", swf), swf);
            LocalDataArea lda = new LocalDataArea(null);
            for (int i = 0; i < 4; i++) {
                lda.localRegisters.put(i, "caller register " + i);
            }
            Map<Integer, Object> originalRegisters = new HashMap<>(lda.localRegisters);
            lda.stack.push("caller stack");
            Object result = invoke(actions, "test", Arrays.<Object>asList(selector), lda);
            if (expected instanceof Number) {
                Assert.assertEquals(EcmaScript.toNumber(result), EcmaScript.toNumber(expected));
            } else {
                Assert.assertSame(result, expected);
            }
            Assert.assertEquals(lda.localRegisters, originalRegisters);
            Assert.assertEquals(lda.stack, Arrays.asList("caller stack"));
        }
    }

    /** Execute serialized instructions with AVM1's shared operand stack and registers. */
    private Object invoke(List<Action> actions, String name, List<Object> args, LocalDataArea lda) {
        for (Action action : actions) {
            if (action instanceof ActionConstantPool) {
                lda.constantPool = ((ActionConstantPool) action).constantPool;
                break;
            }
        }
        ActionDefineFunction function = null;
        for (Action action : actions) {
            if (action instanceof ActionDefineFunction && ((ActionDefineFunction) action).functionName.equals(name)) {
                function = (ActionDefineFunction) action;
                break;
            }
        }
        Assert.assertNotNull(function, name);
        long start = function.getAddress() + function.getHeaderSize();
        long end = start + function.codeSize;
        Map<String, Object> callerVariables = lda.localVariables;
        lda.localVariables = new HashMap<>(callerVariables);
        lda.localVariables.put("scope", new Object());
        for (int i = 0; i < function.paramNames.size(); i++) {
            lda.localVariables.put(function.paramNames.get(i), i < args.size() ? args.get(i) : Undefined.INSTANCE);
        }
        Map<Long, Action> instructions = new HashMap<>();
        for (Action action : actions) {
            instructions.put(action.getAddress(), action);
        }
        Object result = runBlock(actions, instructions, lda, start, end).value;
        lda.localVariables = callerVariables;
        return result;
    }

    private static class BlockResult {
        final boolean returning;
        final Object value;

        BlockResult(boolean returning, Object value) {
            this.returning = returning;
            this.value = value;
        }
    }

    private BlockResult runBlock(List<Action> actions, Map<Long, Action> instructions, LocalDataArea lda, long start, long end) {
        long ip = start;
        int steps = 0;
        while (ip < end) {
            Assert.assertTrue(++steps < 10000, "Function did not terminate");
            Action action = instructions.get(ip);
            Assert.assertNotNull(action, "Invalid jump target " + ip);
            long next = ip + action.getBytesLength();
            lda.jump = null;
            if (action instanceof ActionDefineFunction) {
                next += ((ActionDefineFunction) action).codeSize;
            } else if (action instanceof ActionStoreRegister) {
                // The library evaluator pops here; AVM1 StoreRegister leaves the value on the stack.
                lda.localRegisters.put(((ActionStoreRegister) action).registerNumber, lda.stack.peek());
            } else if (action instanceof ActionCallFunction) {
                String called = lda.popAsString();
                int count = (int) (double) lda.popAsNumber();
                List<Object> callArgs = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    callArgs.add(lda.pop());
                }
                lda.push(invoke(actions, called, callArgs, lda));
            } else if (action instanceof ActionInitObject) {
                int count = (int) (double) lda.popAsNumber();
                Map<String, Object> object = new HashMap<>();
                for (int i = 0; i < count; i++) {
                    Object value = lda.pop();
                    object.put(lda.popAsString(), value);
                }
                lda.push(object);
            } else if (action instanceof ActionEnumerate2) {
                Map<?, ?> object = (Map<?, ?>) lda.pop();
                lda.push(Null.INSTANCE);
                for (Object key : object.keySet()) {
                    lda.push(key);
                }
            } else if (action instanceof ActionWith) {
                lda.pop();
            } else if (action instanceof ActionWaitForFrame2) {
                if (EcmaScript.toNumber(lda.pop()) > 0) {
                    int targetIndex = actions.indexOf(action) + 1 + ((ActionWaitForFrame2) action).skipCount;
                    next = actions.get(targetIndex).getAddress();
                }
            } else if (action instanceof ActionTry) {
                ActionTry actionTry = (ActionTry) action;
                List<Long> sizes = actionTry.getContainerSizes();
                BlockResult tried = runBlock(actions, instructions, lda, next, next + actionTry.getTrySize());
                next += actionTry.getTrySize() + (actionTry.catchBlockFlag ? sizes.get(1) : 0);
                if (actionTry.finallyBlockFlag) {
                    long finallySize = sizes.get(sizes.size() - 1);
                    BlockResult finished = runBlock(actions, instructions, lda, next, next + finallySize);
                    if (finished.returning) {
                        return finished;
                    }
                    next += finallySize;
                }
                if (tried.returning) {
                    return tried;
                }
            } else {
                Assert.assertTrue(action.execute(lda), "Cannot execute " + action);
            }
            if (action instanceof ActionReturn) {
                return new BlockResult(true, lda.returnValue);
            }
            ip = lda.jump == null ? next : lda.jump;
        }
        return new BlockResult(false, Undefined.INSTANCE);
    }

    @Test(dataProvider = "exits")
    public void cleanerRemovesRegisterProtection(String body, long selector, Object expected) throws Exception {
        SWF swf = newSwf(6);
        String source = "function test(selector) {" + body + "}";
        List<Action> compiled = compile(source, swf);
        DoActionTag tag = new DoActionTag(swf);
        Configuration.autoDeobfuscate.set(false);
        tag.setActionBytes(Action.actionsToBytes(compiled, true, swf.version));
        // Test the cleaner against every serialized instruction, including
        // cleanup after an unconditional return that readers may prune.
        ActionList cleaned = new ActionList(readRaw(compiled, swf), Utf8Helper.charsetName);
        new ActionDefineFunctionPushRegistersCleaner().actionListParsed(cleaned, swf);
        Assert.assertFalse(cleaned.stream().anyMatch(a -> a instanceof ActionStackSwap));
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), tag, cleaned, "", writer, Utf8Helper.charsetName);
        writer.finishHilights();
        List<Action> recompiled = readRaw(compile(writer.toString(), swf), swf);
        LocalDataArea lda = new LocalDataArea(null);
        Object result = invoke(recompiled, "test", Arrays.<Object>asList(selector), lda);
        if (expected instanceof Number) {
            Assert.assertEquals(EcmaScript.toNumber(result), EcmaScript.toNumber(expected));
        } else {
            Assert.assertSame(result, expected);
        }
        Assert.assertTrue(lda.stack.empty());
    }

    @Test
    public void doesNotWrapFunctionsWithoutRegisterWrites() throws Exception {
        List<Action> actions = readRaw(compile("function test(selector) { return selector; }", newSwf(6)), newSwf(6));
        Assert.assertFalse(actions.stream().anyMatch(a -> a instanceof ActionStoreRegister || a instanceof ActionStackSwap));
    }

    private String pcode(DoActionTag tag, ScriptExportMode mode) throws Exception {
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        tag.getASMSource(mode, writer, null);
        writer.finishHilights();
        return writer.toString();
    }

    @DataProvider(name = "isolatedCleaning")
    public Object[][] isolatedCleaning() {
        return new Object[][]{
            {6, "function test(selector) { switch (selector) { case 1: return 23; default: return 42; } }"},
            {6, "function test(selector) { function inner(x) { switch (x) { case 1: return 11; } return 4; } switch (selector) { case 1: return inner(selector); } return 8; }"},
            {7, "function test() { try { return 31; } finally { switch (1) { case 1: break; } } }"}
        };
    }

    @Test(dataProvider = "isolatedCleaning")
    public void decompilationDoesNotCleanCachedPcode(int version, String source) throws Exception {
        Configuration.autoDeobfuscate.set(false);
        SWF swf = newSwf(version);
        DoActionTag tag = new DoActionTag(swf);
        tag.setActions(compile(source, swf));
        ActionList actions = tag.getActions();
        String originalPcode = pcode(tag, ScriptExportMode.PCODE);
        String originalPcodeHex = pcode(tag, ScriptExportMode.PCODE_HEX);
        byte[] originalBytes = Action.actionsToBytes(actions, true, version);
        byte[] storedBytes = tag.getActionBytes().getRangeData();
        Assert.assertTrue(actions.stream().filter(a -> a instanceof ActionPush)
                .anyMatch(a -> !((ActionPush) a).values.isEmpty()
                && ((ActionPush) a).values.get(0) instanceof RegisterNumber),
                "Reading P-code must preserve the register-saving prologue");

        ActionList cleaned = Helper.deepCopy(actions);
        new ActionDefineFunctionPushRegistersCleaner().actionListParsed(cleaned, swf);
        Assert.assertTrue(cleaned.size() < actions.size());
        HighlightedTextWriter expectedWriter = new HighlightedTextWriter(new CodeFormatting(), false);
        tag.getActionScriptSource(expectedWriter, cleaned);
        expectedWriter.finishHilights();
        String expectedSource = expectedWriter.toString();
        for (int pass = 0; pass < 2; pass++) {
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), true);
            tag.getActionScriptSource(writer, null);
            writer.finishHilights();
            String actualSource = writer.toString();
            Assert.assertEquals(actualSource, expectedSource);
            int returnPosition = actualSource.replace("\r", "").indexOf("return ");
            Assert.assertTrue(returnPosition >= 0);
            boolean mappedReturn = false;
            List<Long> highlightedOffsets = new ArrayList<>();
            for (Highlighting highlight : writer.instructionHilights) {
                if (highlight.startPos <= returnPosition && returnPosition < highlight.startPos + highlight.len) {
                    long offset = highlight.getProperties().offset;
                    highlightedOffsets.add(offset);
                    mappedReturn |= actions.stream().anyMatch(a -> a instanceof ActionReturn && a.getAddress() == offset);
                }
            }
            Assert.assertTrue(mappedReturn, "Return highlighting must point to the original P-code address: " + highlightedOffsets + "; source: " + actualSource);
            Assert.assertSame(tag.getActions(), actions);
            Assert.assertEquals(Action.actionsToBytes(actions, true, version), originalBytes);
            Assert.assertEquals(tag.getActionBytes().getRangeData(), storedBytes);
            Assert.assertEquals(pcode(tag, ScriptExportMode.PCODE), originalPcode);
            Assert.assertEquals(pcode(tag, ScriptExportMode.PCODE_HEX), originalPcodeHex);
        }
    }

    @Test
    public void frameLoadedSkipCountIncludesReturnCleanup() throws Exception {
        String body = "switch (1) { case 1: break; } ifFrameLoaded(selector) { return 12; } return 8;";
        preservesCallerRegistersAndReturnValue(body, 0L, 12L);
        preservesCallerRegistersAndReturnValue(body, 1L, 8L);
        SWF swf = newSwf(6);
        List<Action> raw = readRaw(compile("function test(selector) {" + body + "}", swf), swf);
        ActionWaitForFrame2 wait = (ActionWaitForFrame2) raw.stream()
                .filter(a -> a instanceof ActionWaitForFrame2).findFirst().get();
        Assert.assertEquals(wait.skipCount, 5);
        ActionList cleaned = new ActionList(raw, Utf8Helper.charsetName);
        new ActionDefineFunctionPushRegistersCleaner().actionListParsed(cleaned, swf);
        Assert.assertEquals(wait.skipCount, 2);
        Assert.assertEquals(EcmaScript.toNumber(invoke(cleaned, "test", Arrays.<Object>asList(0L), new LocalDataArea(null))), 12.0);
        Assert.assertEquals(EcmaScript.toNumber(invoke(cleaned, "test", Arrays.<Object>asList(1L), new LocalDataArea(null))), 8.0);
    }

    @Test
    public void doesNotWrapDefineFunction2() throws Exception {
        List<Action> actions = readRaw(compile("function test(selector) { switch (selector) { case 1: return 2; } }", newSwf(7)), newSwf(7));
        Assert.assertTrue(actions.stream().anyMatch(a -> a instanceof ActionDefineFunction2));
        Assert.assertFalse(actions.stream().anyMatch(a -> a instanceof ActionStackSwap));
        Assert.assertFalse(actions.stream().filter(a -> a instanceof ActionPush)
                .anyMatch(a -> ((ActionPush) a).values.size() > 1
                && ((ActionPush) a).values.stream().allMatch(v -> v instanceof RegisterNumber)));
    }

    @DataProvider(name = "finallyReturns")
    public Object[][] finallyReturns() {
        return new Object[][]{
            {"try { return 31; } finally { switch (1) { case 1: break; } }", 31L},
            {"try { switch (1) { case 1: return 31; } } finally { switch (2) { case 2: return 42; } }", 42L},
            {"try { switch (1) { case 1: break; } } finally { switch (2) { case 2: break; } }", Undefined.INSTANCE},
            {"try { try { return 31; } finally { switch (1) { case 1: break; } } } finally { switch (2) { case 2: break; } }", 31L}
        };
    }

    @Test(dataProvider = "finallyReturns")
    public void restoresRegistersAfterFinally(String body, Object expected) throws Exception {
        SWF swf = newSwf(7);
        List<Action> compiled = compile("function test() {" + body + "}", swf);
        Assert.assertTrue(compiled.stream().anyMatch(a -> a instanceof ActionDefineFunction));
        List<Action> raw = readRaw(compiled, swf);
        LocalDataArea lda = new LocalDataArea(null);
        for (int i = 0; i < 4; i++) {
            lda.localRegisters.put(i, "caller register " + i);
        }
        Map<Integer, Object> originalRegisters = new HashMap<>(lda.localRegisters);
        lda.stack.push("caller stack");
        Object result = invoke(raw, "test", new ArrayList<>(), lda);
        if (expected instanceof Number) {
            Assert.assertEquals(EcmaScript.toNumber(result), EcmaScript.toNumber(expected));
        } else {
            Assert.assertSame(result, expected);
        }
        Assert.assertEquals(lda.localRegisters, originalRegisters);
        Assert.assertEquals(lda.stack, Arrays.asList("caller stack"));

        DoActionTag tag = new DoActionTag(swf);
        tag.setActionBytes(Action.actionsToBytes(compiled, true, swf.version));
        ActionList actions = tag.getActions();
        long originalTryCount = raw.stream().filter(a -> a instanceof ActionTry).count();
        Assert.assertEquals(actions.stream().filter(a -> a instanceof ActionTry).count(), originalTryCount);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), tag, actions, "", writer, Utf8Helper.charsetName);
        writer.finishHilights();
        List<Action> recompiled = readRaw(compile(writer.toString(), swf), swf);
        Object roundTripResult = invoke(recompiled, "test", new ArrayList<>(), lda);
        if (expected instanceof Number) {
            Assert.assertEquals(EcmaScript.toNumber(roundTripResult), EcmaScript.toNumber(expected));
        } else {
            Assert.assertSame(roundTripResult, expected);
        }
        Assert.assertEquals(lda.localRegisters, originalRegisters);
        Assert.assertEquals(lda.stack, Arrays.asList("caller stack"));
    }
}
