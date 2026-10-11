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
import com.jpexs.decompiler.flash.action.ActionScriptObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.profile.castlecrashers.ActionPushDefineLocal;
import com.jpexs.decompiler.flash.action.profile.castlecrashers.ActionPushGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.profiles.CastleCrashersSwfProfile;
import java.util.Arrays;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CastleCrashersExecuteTest {

    private Action parse(String instruction) throws Exception {
        return ASMParser.parse(0, true, instruction + "\n", 6, true,
                "UTF-8", new CastleCrashersSwfProfile()).get(0);
    }

    @Test
    public void testGetVariableConstantPools() throws Exception {
        ActionPushGetVariable action = (ActionPushGetVariable) parse("CC_PushGetVariable \"unused\"");
        action.name = new ConstantIndex(0);
        action.setConstantPool(Arrays.asList("fallback"));
        LocalDataArea lda = new LocalDataArea(null);
        lda.constantPool = Arrays.asList("active");
        lda.localVariables.put("active", 10L);
        lda.localVariables.put("fallback", 20L);
        lda.push("sentinel");

        assertTrue(action.execute(lda));
        assertEquals(lda.pop(), 10L);
        lda.constantPool = null;
        assertTrue(action.execute(lda));
        assertEquals(lda.pop(), 20L);
        assertEquals(lda.pop(), "sentinel");
        assertTrue(lda.stack.isEmpty());
    }

    @Test
    public void testMissingVariable() throws Exception {
        LocalDataArea lda = new LocalDataArea(null);
        assertTrue(parse("CC_PushGetVariable \"missing\"").execute(lda));
        assertSame(lda.pop(), Undefined.INSTANCE);
        assertTrue(lda.stack.isEmpty());
    }

    @DataProvider(name = "pushedValues")
    public Object[][] pushedValues() {
        return new Object[][]{
            {"null", Null.INSTANCE},
            {"undefined", Undefined.INSTANCE},
            {"true", true},
            {"1.5", 1.5d},
            {"register1", 17L},
            {"register2", Undefined.INSTANCE},
            {"constant0", "active"}
        };
    }

    @Test(dataProvider = "pushedValues")
    public void testGetVariable2PreservesFirstValue(String operand, Object expected) throws Exception {
        LocalDataArea lda = new LocalDataArea(null);
        lda.constantPool = Arrays.asList("active");
        lda.localRegisters.put(1, 17L);
        lda.localVariables.put("score", 100L);
        lda.push("sentinel");

        assertTrue(parse("CC_PushGetVariable2 " + operand + ", \"score\"").execute(lda));
        assertEquals(lda.stack.size(), 3);
        assertEquals(lda.pop(), 100L);
        assertEquals(lda.pop(), expected);
        assertEquals(lda.pop(), "sentinel");
    }

    @Test
    public void testGetMember() throws Exception {
        LocalDataArea lda = new LocalDataArea(null);
        lda.constantPool = Arrays.asList("/health");
        ActionScriptObject object = new ActionScriptObject();
        object.setMember("/health", 100L);
        lda.push("sentinel");
        lda.push(object);

        assertTrue(parse("CC_PushGetMember constant0").execute(lda));
        assertEquals(lda.pop(), 100L);
        assertEquals(lda.pop(), "sentinel");
    }

    @Test
    public void testMissingMember() throws Exception {
        LocalDataArea lda = new LocalDataArea(null);
        lda.push(new ActionScriptObject());
        assertTrue(parse("CC_PushGetMember \"missing\"").execute(lda));
        assertSame(lda.pop(), Undefined.INSTANCE);
        assertTrue(lda.stack.isEmpty());
    }

    @Test
    public void testSetMember() throws Exception {
        LocalDataArea lda = new LocalDataArea(null);
        lda.constantPool = Arrays.asList("/health");
        lda.localRegisters.put(1, 100L);
        ActionScriptObject object = new ActionScriptObject();
        lda.push("sentinel");
        lda.push(object);

        assertTrue(parse("CC_PushSetMember constant0, register1").execute(lda));
        assertEquals(object.getMember("/health"), 100L);
        assertEquals(lda.stack.size(), 1);
        assertEquals(lda.pop(), "sentinel");
    }

    @DataProvider(name = "memberInstructions")
    public Object[][] memberInstructions() {
        return new Object[][]{
            {"CC_PushGetMember \"health\""},
            {"CC_PushSetMember \"health\", 100"}
        };
    }

    @Test(dataProvider = "memberInstructions")
    public void testMissingObjectDoesNotChangeStack(String instruction) throws Exception {
        LocalDataArea lda = new LocalDataArea(null);
        assertFalse(parse(instruction).execute(lda));
        assertTrue(lda.stack.isEmpty());
        assertTrue(lda.localVariables.isEmpty());
    }

    @Test
    public void testDefineLocalCreatesAndOverwritesVariable() throws Exception {
        ActionPushDefineLocal action = (ActionPushDefineLocal) parse("CC_PushDefineLocal \"unused\", register1");
        action.name = new ConstantIndex(0);
        action.setConstantPool(Arrays.asList("score"));
        LocalDataArea lda = new LocalDataArea(null);
        lda.push("sentinel");
        lda.localRegisters.put(1, 100L);

        assertTrue(action.execute(lda));
        assertEquals(lda.localVariables.get("score"), 100L);
        lda.localRegisters.put(1, 200L);
        assertTrue(action.execute(lda));
        assertEquals(lda.localVariables.get("score"), 200L);
        assertEquals(lda.localVariables.size(), 1);
        assertEquals(lda.stack.size(), 1);
        assertEquals(lda.pop(), "sentinel");
    }
}
